package com.tonywww.titan_satellite.blockentity;

import com.tonywww.titan_satellite.event.MethaneExtractionEvents;
import com.tonywww.titan_satellite.event.WaveController;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSFluids;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
//? if forge {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
*///?}
import org.jetbrains.annotations.Nullable;

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
    /** 流体槽容量（mB）。 */
    public static final int TANK_CAPACITY = 16000;
    /** 运行期每 tick 产出的液态甲烷（mB）。 */
    private static final int FLUID_PER_TICK = 8;
    /** 运行期每隔多少 tick 产出一次材料。 */
    private static final int ITEM_INTERVAL = 240;
    /** 每累计抽取多少 mB 提升 1 点生态压力（波次强度）。 */
    private static final int EXTRACTION_PER_INTENSITY = 4000;

    private Phase phase = Phase.IDLE;
    private int progress = 0;
    private int waveIndex = 0;
    private int integrity = MAX_INTEGRITY;
    /** 上一 tick 的红石供电状态（用于上升沿检测，防止持续/重复开启）。 */
    private boolean powered = false;
    /** 本次开采累计抽取量（mB，用于生态压力→波次强度）。 */
    private int extractedTotal = 0;

    /** 产出液体槽：只出不进（外部只能抽取，产出经 {@link OutputOnlyTank#fillInternal}）。 */
    private final OutputOnlyTank methaneTank = new OutputOnlyTank(TANK_CAPACITY,
            fs -> fs.getFluid() == TSFluids.LIQUID_METHANE.get());
    //? if forge {
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> methaneTank);
    //?}
    /** 材料产出缓冲：先入此处，再推送至正上方容器。 */
    private final ItemStackHandler outputBuffer = new ItemStackHandler(6);

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
        // 波次调度：波次强度随累计抽取量（生态压力）提升
        if (waveIndex < WAVE_COUNT && progress >= waveThreshold(waveIndex + 1)) {
            waveIndex++;
            WaveController.beginWave(level, pos, waveIndex, waveIndex + extractedTotal / EXTRACTION_PER_INTENSITY);
        }
        // 产出：液态甲烷入流体槽（累计抽取量）；材料按间隔入缓冲，并推送至正上方容器（自动化输出）
        methaneTank.fillInternal(new FluidStack(TSFluids.LIQUID_METHANE.get(), FLUID_PER_TICK), IFluidHandler.FluidAction.EXECUTE);
        extractedTotal += FLUID_PER_TICK;
        if (progress % ITEM_INTERVAL == 0) {
            produceItem(level, pos, 1);
        }
        pushOutputs(level, pos);
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
            produceItem(level, pos, 2 + waveIndex);   // 终局额外产出，一并推送至上方容器
            pushOutputs(level, pos);
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
            case IDLE -> startExtraction(level, pos, player);
            case RUNNING -> player.displayClientMessage(Component.literal(
                    "Extracting... " + (progress * 100 / MAX_PROGRESS) + "%  integrity " + integrity), true);
            default -> {
                reset();
                player.displayClientMessage(Component.literal("Pump reset."), true);
            }
        }
    }

    /** 红石信号变化：上升沿（无→有）且处于 IDLE 时启动一次；持续供电不会重复触发。 */
    public void onNeighborSignalChanged(ServerLevel level, BlockPos pos, boolean signal) {
        if (signal && !powered && phase == Phase.IDLE) {
            startExtraction(level, pos, null);
        }
        if (signal != powered) {
            powered = signal;
            setChanged();
        }
    }

    /** 启动开采（玩家右键 / 红石上升沿共用）。{@code activator} 为 {@code null} 表示红石等非玩家来源。 */
    private boolean startExtraction(ServerLevel level, BlockPos pos, @Nullable Player activator) {
        BlockPos corePos = pos.below();
        if (!level.getBlockState(corePos).is(TSBlocks.METHANE_POOL_CORE.get())) {
            if (activator != null) {
                activator.displayClientMessage(Component.literal("Place the pump on a Methane Pool Core to extract."), true);
            }
            return false;
        }
        if (WaveController.tryStart(level, pos, corePos, activator)) {
            phase = Phase.RUNNING;
            progress = 0;
            waveIndex = 0;
            integrity = MAX_INTEGRITY;
            extractedTotal = 0;
            setChanged();
            if (activator != null) {
                activator.displayClientMessage(Component.literal("Methane extraction started - protect the pump!"), true);
            }
            return true;
        }
        return false;
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
        extractedTotal = 0;
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

    public FluidTank getMethaneTank() {
        return methaneTank;
    }

    /** 产出材料入内部缓冲；缓冲已满则溢出为掉落物（不丢失）。 */
    private void produceItem(ServerLevel level, BlockPos pos, int count) {
        ItemStack out = new ItemStack(TSItems.PRECISION_COMPONENTS.get(), count);
        for (int i = 0; i < outputBuffer.getSlots() && !out.isEmpty(); i++) {
            out = outputBuffer.insertItem(i, out, false);
        }
        if (!out.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, out);
        }
    }

    /** 把缓冲里的材料推送进正上方方块的物品容器（自动化输出，无容器则暂存）。 */
    private void pushOutputs(ServerLevel level, BlockPos pos) {
        BlockEntity above = level.getBlockEntity(pos.above());
        if (above == null) {
            return;
        }
        //? if forge {
        IItemHandler dest = above.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).orElse(null);
        //?} else {
        /*IItemHandler dest = Capabilities.ItemHandler.BLOCK.getCapability(level, pos.above(), null, above, Direction.DOWN);
        *///?}
        if (dest == null) {
            return;
        }
        for (int i = 0; i < outputBuffer.getSlots(); i++) {
            ItemStack stack = outputBuffer.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            outputBuffer.setStackInSlot(i, ItemHandlerHelper.insertItem(dest, stack, false));
        }
    }

    /** 泵被破坏时掉落缓冲中的材料。 */
    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < outputBuffer.getSlots(); i++) {
            ItemStack stack = outputBuffer.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                outputBuffer.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    //? if forge {
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCap.invalidate();
    }
    //?} else {
    /*// NeoForge 能力系统：经 mod 总线 RegisterCapabilitiesEvent 登记流体处理器（由主类 addListener 挂接）。
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                TSBlockEntities.METHANE_PUMP.get(), (be, side) -> be.methaneTank);
    }
    *///?}

    //? if forge {
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Phase", phase.name());
        tag.putInt("Progress", progress);
        tag.putInt("WaveIndex", waveIndex);
        tag.putInt("Integrity", integrity);
        tag.putInt("Extracted", extractedTotal);
        tag.putBoolean("Powered", powered);
        tag.put("Tank", methaneTank.writeToNBT(new CompoundTag()));
        tag.put("Output", outputBuffer.serializeNBT());
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
        powered = tag.getBoolean("Powered");
        extractedTotal = tag.getInt("Extracted");
        methaneTank.readFromNBT(tag.getCompound("Tank"));
        if (tag.contains("Output")) {
            outputBuffer.deserializeNBT(tag.getCompound("Output"));
        }
    }
    //?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Phase", phase.name());
        tag.putInt("Progress", progress);
        tag.putInt("WaveIndex", waveIndex);
        tag.putInt("Integrity", integrity);
        tag.putInt("Extracted", extractedTotal);
        tag.putBoolean("Powered", powered);
        tag.put("Tank", methaneTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Output", outputBuffer.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            phase = Phase.valueOf(tag.getString("Phase"));
        } catch (IllegalArgumentException e) {
            phase = Phase.IDLE;
        }
        progress = tag.getInt("Progress");
        waveIndex = tag.getInt("WaveIndex");
        integrity = tag.contains("Integrity") ? tag.getInt("Integrity") : MAX_INTEGRITY;
        powered = tag.getBoolean("Powered");
        extractedTotal = tag.getInt("Extracted");
        methaneTank.readFromNBT(registries, tag.getCompound("Tank"));
        if (tag.contains("Output")) {
            outputBuffer.deserializeNBT(registries, tag.getCompound("Output"));
        }
    }
    *///?}

    /** 只出不进的流体槽：外部 {@code fill} 被拒，产出经 {@link #fillInternal}。 */
    public static class OutputOnlyTank extends FluidTank {
        public OutputOnlyTank(int capacity, java.util.function.Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return 0;
        }

        public int fillInternal(FluidStack resource, IFluidHandler.FluidAction action) {
            return super.fill(resource, action);
        }
    }
}
