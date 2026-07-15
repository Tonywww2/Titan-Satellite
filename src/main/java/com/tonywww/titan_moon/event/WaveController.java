package com.tonywww.titan_moon.event;

import com.tonywww.titan_moon.registry.TMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
//? if forge {
import net.minecraftforge.common.MinecraftForge;
//?} else {
/*import net.neoforged.neoforge.common.NeoForge;
*///?}

import java.util.List;

/**
 * 甲烷开采塔防的「集中副作用 facade」。
 *
 * <p>状态推进由 {@code SpecialMethanePumpBlockEntity} 驱动，但所有<b>共享副作用</b>
 * （抛事件、刷怪、产出、音画、退散）都上提到本类统一执行。
 *
 * <p>整套流程对外暴露 Forge 事件以便高度自定义：
 * <ul>
 *   <li>{@link MethaneExtractionEvents.Start}（可取消）——激活拦截；</li>
 *   <li>{@link MethaneExtractionWaveEvent}（冻结签名）——每波开始通知；</li>
 *   <li>{@link MethaneExtractionEvents.Success} / {@link MethaneExtractionEvents.Fail}——终局。</li>
 * </ul>
 * 每波刷怪数量经 {@link #baseWaveMobCount(int, int)}（可继承覆写以调强度）；波次怪的生命周期与
 * 属性强化由 {@code WaveSpawnMixin} 注入 vanilla {@code Mob.aiStep} 定制。
 */
@SuppressWarnings("deprecation")
public final class WaveController {

    private WaveController() {
    }

    /** 波次刷怪环形半径（格）。 */
    private static final int SPAWN_RADIUS = 10;
    /** 波次怪的持久化标记键（用于终局退散、与自然生成怪区分）。 */
    public static final String WAVE_MOB_TAG = "TitanWaveMob";
    /** 波次怪记忆的泵坐标键（用于增强其攻击泵的寻路欲望）。 */
    public static final String PUMP_POS_TAG = "TitanPumpPos";

    /**
     * 尝试启动开采：抛可取消的 {@link MethaneExtractionEvents.Start}。
     *
     * @return 是否成功启动（{@code false} 表示被监听者取消）。
     */
    public static boolean tryStart(ServerLevel level, BlockPos pumpPos, BlockPos corePos, Player activator) {
        //? if forge {
        if (MinecraftForge.EVENT_BUS.post(new MethaneExtractionEvents.Start(level, pumpPos, corePos, activator))) {
        //?} else {
        /*if (NeoForge.EVENT_BUS.post(new MethaneExtractionEvents.Start(level, pumpPos, corePos, activator)).isCanceled()) {
        *///?}
            return false;
        }
        level.playSound(null, pumpPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pumpPos.getX() + 0.5, pumpPos.getY() + 1.0, pumpPos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.02);
        return true;
    }

    /**
     * 开始一波：先抛<b>冻结</b>的 {@link MethaneExtractionWaveEvent}（对外通知），再执行默认刷怪。
     */
    public static void beginWave(ServerLevel level, BlockPos pumpPos, int waveIndex, int intensity) {
        //? if forge {
        MinecraftForge.EVENT_BUS.post(new MethaneExtractionWaveEvent(level, pumpPos, waveIndex, intensity));
        //?} else {
        /*NeoForge.EVENT_BUS.post(new MethaneExtractionWaveEvent(level, pumpPos, waveIndex, intensity));
        *///?}
        level.playSound(null, pumpPos, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.2F, 0.7F);
        int count = baseWaveMobCount(waveIndex, intensity);
        for (int i = 0; i < count; i++) {
            spawnWaveMob(level, pumpPos, waveIndex);
        }
    }

    /**
     * 每波默认刷怪数量（基于波序与强度）。
     *
     * <p>作为可覆写的扩展点：附属 / 整合包可继承本 facade 或监听 {@link MethaneExtractionWaveEvent}
     * 调整每波强度；波次怪的属性强化另由 {@code WaveSpawnMixin} 定制。
     */
    public static int baseWaveMobCount(int waveIndex, int intensity) {
        return 2 + intensity;
    }

    /** 在泵周围环形选点生成一只深渊怪，并打上波次标记。 */
    private static void spawnWaveMob(ServerLevel level, BlockPos pumpPos, int waveIndex) {
        EntityType<? extends Mob> type = pickMobType(level, waveIndex);
        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double dist = SPAWN_RADIUS * (0.6 + level.random.nextDouble() * 0.4);
        int x = pumpPos.getX() + (int) Math.round(Math.cos(angle) * dist);
        int z = pumpPos.getZ() + (int) Math.round(Math.sin(angle) * dist);
        BlockPos spawn = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, pumpPos.getY(), z));
        // 地表与泵高差过大（深洞 / 悬空）时退回泵所在 Y，避免刷到很远的地方。
        if (Math.abs(spawn.getY() - pumpPos.getY()) > 16) {
            spawn = new BlockPos(x, pumpPos.getY(), z);
        }
        Mob mob = type.create(level);
        if (mob == null) {
            return;
        }
        mob.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        //? if forge {
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null, null);
        //?} else {
        /*mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null);
        *///?}
        mob.getPersistentData().putBoolean(WAVE_MOB_TAG, true);
        mob.getPersistentData().putLong(PUMP_POS_TAG, pumpPos.asLong());
        level.addFreshEntity(mob);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5, 12, 0.3, 0.5, 0.3, 0.02);
    }

    /** 纯生物围攻：机械探测器已退出生态波次（仅存于先驱者前哨遗迹）。 */
    private static EntityType<? extends Mob> pickMobType(ServerLevel level, int waveIndex) {
        return TMEntities.AMMONIA_STALKER.get();
    }

    /**
     * 开采成功：抛 {@link MethaneExtractionEvents.Success}、产出默认奖励、退散残余波次怪。
     */
    public static void succeed(ServerLevel level, BlockPos pumpPos, BlockPos corePos, int wavesSurvived) {
        //? if forge {
        MinecraftForge.EVENT_BUS.post(new MethaneExtractionEvents.Success(level, pumpPos, corePos, wavesSurvived));
        //?} else {
        /*NeoForge.EVENT_BUS.post(new MethaneExtractionEvents.Success(level, pumpPos, corePos, wavesSurvived));
        *///?}
        level.playSound(null, pumpPos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, pumpPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                pumpPos.getX() + 0.5, pumpPos.getY() + 1.2, pumpPos.getZ() + 0.5, 60, 0.6, 0.8, 0.6, 0.15);
        // 材料产出改由甲烷泵在运行期 / 终局填充至上方容器（见 SpecialMethanePumpBlockEntity），此处仅保留庆祝表现与退散。
        disperseWaveMobs(level, pumpPos);
    }

    /**
     * 开采失败：抛 {@link MethaneExtractionEvents.Fail}、退散残余波次怪。
     */
    public static void fail(ServerLevel level, BlockPos pumpPos, BlockPos corePos, MethaneExtractionEvents.Fail.Reason reason) {
        //? if forge {
        MinecraftForge.EVENT_BUS.post(new MethaneExtractionEvents.Fail(level, pumpPos, corePos, reason));
        //?} else {
        /*NeoForge.EVENT_BUS.post(new MethaneExtractionEvents.Fail(level, pumpPos, corePos, reason));
        *///?}
        level.playSound(null, pumpPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pumpPos.getX() + 0.5, pumpPos.getY() + 0.8, pumpPos.getZ() + 0.5, 40, 0.4, 0.6, 0.4, 0.05);
        disperseWaveMobs(level, pumpPos);
    }

    /** 运行时环境音画脉冲（由泵每若干 tick 调用，营造轰鸣 / 能量波动）。 */
    public static void ambientPulse(ServerLevel level, BlockPos pumpPos) {
        level.playSound(null, pumpPos, SoundEvents.CONDUIT_AMBIENT, SoundSource.BLOCKS, 0.6F, 0.7F);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                pumpPos.getX() + 0.5, pumpPos.getY() + 1.0, pumpPos.getZ() + 0.5, 6, 0.2, 0.3, 0.2, 0.02);
    }

    /** 终局后让本场波次怪退散（清除带 {@link #WAVE_MOB_TAG} 标记的实体）。 */
    private static void disperseWaveMobs(ServerLevel level, BlockPos pumpPos) {
        AABB box = new AABB(pumpPos).inflate(SPAWN_RADIUS * 3.0);
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box, m -> m.getPersistentData().getBoolean(WAVE_MOB_TAG));
        for (Mob mob : mobs) {
            level.sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + 0.5, mob.getZ(), 6, 0.2, 0.3, 0.2, 0.01);
            mob.discard();
        }
    }
}
