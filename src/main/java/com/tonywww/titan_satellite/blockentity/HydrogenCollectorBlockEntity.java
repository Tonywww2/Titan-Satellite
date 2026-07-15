package com.tonywww.titan_satellite.blockentity;

import com.tonywww.titan_satellite.config.TSConfig;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if forge {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
*///?}
import org.jetbrains.annotations.Nullable;

/**
 * 集氢罩方块实体：被动生产者。仅当正下方为 {@code hydrogen_bubble_mat} 时，
 * 无需能量按间隔产出 {@code liquid_hydrogen}（液氢）流体，入内部流体槽并推送至相邻流体容器
 * （亦以 FLUID_HANDLER 能力暴露，供管道/Mek 转化炉抽取；液氢经 forge:hydrogen/c:hydrogen tag
 * 可被 Mek 当作液氢转成氢气）。菌毯被移除即停。
 */
public class HydrogenCollectorBlockEntity extends BlockEntity {

    private int progress = 0;

    /** 液氢产出槽：仅液氢（内部产出 / 外部抽取）；容量由 {@link TSConfig#HYDROGEN_TANK_CAPACITY} 配置。 */
    private final FluidTank tank = new FluidTank(TSConfig.HYDROGEN_TANK_CAPACITY.get(),
            fs -> fs.getFluid() == TSFluids.LIQUID_HYDROGEN.get());
    //? if forge {
    private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(() -> tank);
    //?}

    public HydrogenCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(TSBlockEntities.HYDROGEN_COLLECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HydrogenCollectorBlockEntity be) {
        if (level instanceof ServerLevel server) {
            be.tick(server, pos);
        }
    }

    private void tick(ServerLevel level, BlockPos pos) {
        // 依附检测：正下方须为氢泡菌毯
        if (!level.getBlockState(pos.below()).is(TSBlocks.HYDROGEN_BUBBLE_MAT.get())) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }
        progress++;
        if (progress >= TSConfig.HYDROGEN_PRODUCE_INTERVAL.get()) {
            progress = 0;
            tank.fill(new FluidStack(TSFluids.LIQUID_HYDROGEN.get(), TSConfig.HYDROGEN_FLUID_PER_INTERVAL.get()), IFluidHandler.FluidAction.EXECUTE);
            level.sendParticles(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.0);
        }
        pushOutputs(level, pos);
        setChanged();
    }

    /** 把液氢推入相邻流体容器（除正下方菌毯外的各方向）。 */
    private void pushOutputs(ServerLevel level, BlockPos pos) {
        if (tank.getFluidAmount() <= 0) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            //? if forge {
            IFluidHandler dest = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).orElse(null);
            //?} else {
            /*IFluidHandler dest = Capabilities.FluidHandler.BLOCK.getCapability(level, pos.relative(dir), null, neighbor, dir.getOpposite());
            *///?}
            if (dest == null) {
                continue;
            }
            int filled = dest.fill(tank.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            }
            if (tank.getFluidAmount() <= 0) {
                return;
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        tank.readFromNBT(tag.getCompound("Tank"));
    }
    //?} else {
    /*public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                TSBlockEntities.HYDROGEN_COLLECTOR.get(), (be, side) -> be.tank);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        tank.readFromNBT(registries, tag.getCompound("Tank"));
    }
    *///?}
}
