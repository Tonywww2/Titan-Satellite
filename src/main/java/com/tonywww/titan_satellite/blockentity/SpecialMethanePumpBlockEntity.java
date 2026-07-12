package com.tonywww.titan_satellite.blockentity;

import com.tonywww.titan_satellite.event.MethaneExtractionEvents;
import com.tonywww.titan_satellite.event.WaveController;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 甲烷泵方块实体：驱动开采塔防状态机（服务端）。
 * {@code IDLE→(激活)RUNNING→SUCCESS/FAILED}；每到一波阈值经 {@link WaveController} 抛事件并刷怪，
 * 贴近泵的怪物会削减泵完整度，归零即被冲垮。所有共享副作用集中在 {@link WaveController}。
 */
public class SpecialMethanePumpBlockEntity extends BlockEntity {

    /** 状态机阶段。 */
    public enum Phase {IDLE, RUNNING, SUCCESS, FAILED}

    /** 满进度所需 tick（≈2 分钟）。 */
    public static final int MAX_PROGRESS = 2400;
    /** 总波数。 */
    public static final int WAVE_COUNT = 5;
    /** 泵完整度上限。 */
    public static final int MAX_INTEGRITY = 100;
    /** 怪物「攻击」泵的判定半径（格）。 */
    private static final double ATTACK_RADIUS = 2.5;

    private Phase phase = Phase.IDLE;
    private int progress = 0;
    private int waveIndex = 0;
    private int integrity = MAX_INTEGRITY;

    public SpecialMethanePumpBlockEntity(BlockPos pos, BlockState state) {
        super(TSBlockEntities.METHANE_PUMP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpecialMethanePumpBlockEntity be) {
        if (level instanceof ServerLevel server) {
            be.tick(server, pos);
        }
    }

    private void tick(ServerLevel level, BlockPos pos) {
        if (phase != Phase.RUNNING) {
            return;
        }
        BlockPos corePos = pos.below();
        // 核心被移除 -> 失败
        if (!level.getBlockState(corePos).is(TSBlocks.METHANE_POOL_CORE.get())) {
            WaveController.fail(level, pos, corePos, MethaneExtractionEvents.Fail.Reason.CORE_REMOVED);
            reset();
            return;
        }
        progress++;
        // 波次调度
        if (waveIndex < WAVE_COUNT && progress >= waveThreshold(waveIndex + 1)) {
            waveIndex++;
            WaveController.beginWave(level, pos, waveIndex, waveIndex);
        }
        // 完整度：贴近泵的怪物削减之，安全时缓恢
        AABB box = new AABB(pos).inflate(ATTACK_RADIUS);
        int attackers = level.getEntitiesOfClass(Monster.class, box, Monster::isAlive).size();
        if (attackers > 0) {
            integrity -= attackers;
            if (progress % 10 == 0) {
                level.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.1);
            }
            if (integrity <= 0) {
                WaveController.fail(level, pos, corePos, MethaneExtractionEvents.Fail.Reason.PUMP_OVERRUN);
                reset();
                return;
            }
        } else if (integrity < MAX_INTEGRITY && progress % 20 == 0) {
            integrity++;
        }
        // 环境音画
        if (progress % 40 == 0) {
            WaveController.ambientPulse(level, pos);
        }
        // 成功
        if (progress >= MAX_PROGRESS) {
            WaveController.succeed(level, pos, corePos, waveIndex);
            phase = Phase.SUCCESS;
            setChanged();
            return;
        }
        setChanged();
    }

    /** 每波触发的进度阈值（在 [0,MAX] 内均匀分布，末尾留缓冲）。 */
    private static int waveThreshold(int wave) {
        return (MAX_PROGRESS / (WAVE_COUNT + 1)) * wave;
    }

    /** 右键交互：IDLE→启动；RUNNING→播报进度；SUCCESS/FAILED→重置以便重试。 */
    public void onActivatedBy(ServerLevel level, BlockPos pos, Player player) {
        switch (phase) {
            case IDLE -> {
                BlockPos corePos = pos.below();
                if (!level.getBlockState(corePos).is(TSBlocks.METHANE_POOL_CORE.get())) {
                    player.displayClientMessage(Component.literal("Place the pump on a Methane Pool Core to extract."), true);
                    return;
                }
                if (WaveController.tryStart(level, pos, corePos, player)) {
                    phase = Phase.RUNNING;
                    progress = 0;
                    waveIndex = 0;
                    integrity = MAX_INTEGRITY;
                    setChanged();
                    player.displayClientMessage(Component.literal("Methane extraction started - protect the pump!"), true);
                }
            }
            case RUNNING -> player.displayClientMessage(Component.literal(
                    "Extracting... " + (progress * 100 / MAX_PROGRESS) + "%  integrity " + integrity), true);
            default -> {
                reset();
                player.displayClientMessage(Component.literal("Pump reset."), true);
            }
        }
    }

    /** 泵被破坏：运行中则判定失败。由方块 onRemove 调用。 */
    public void onDestroyed(ServerLevel level, BlockPos pos) {
        if (phase == Phase.RUNNING) {
            WaveController.fail(level, pos, pos.below(), MethaneExtractionEvents.Fail.Reason.PUMP_DESTROYED);
        }
    }

    private void reset() {
        phase = Phase.IDLE;
        progress = 0;
        waveIndex = 0;
        integrity = MAX_INTEGRITY;
        setChanged();
    }

    public Phase getPhase() {
        return phase;
    }

    public int getProgress() {
        return progress;
    }

    public int getWaveIndex() {
        return waveIndex;
    }

    public int getIntegrity() {
        return integrity;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Phase", phase.name());
        tag.putInt("Progress", progress);
        tag.putInt("WaveIndex", waveIndex);
        tag.putInt("Integrity", integrity);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (IllegalArgumentException e) {
            phase = Phase.IDLE;
        }
        progress = tag.getInt("Progress");
        waveIndex = tag.getInt("WaveIndex");
        integrity = tag.contains("Integrity") ? tag.getInt("Integrity") : MAX_INTEGRITY;
    }
}
