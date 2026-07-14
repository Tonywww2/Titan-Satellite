package com.tonywww.titan_satellite.blockentity;

import com.tonywww.titan_satellite.registry.TSBlockEntities;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if forge {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
*///?}
import org.jetbrains.annotations.Nullable;

/**
 * 集氢罩方块实体：被动生产者。仅当正下方为 {@code hydrogen_bubble_mat} 时，
 * 无需能量按间隔把菌毯释放的 H₂ 攒成 {@code hydrogen_capsule}，入内部缓冲并推送至相邻容器
 * （亦以 ITEM_HANDLER 能力暴露，供漏斗/管道抽取）。菌毯被移除即停。
 */
public class HydrogenCollectorBlockEntity extends BlockEntity {

    /** 每产出 1 个氢气瓶所需 tick（≈10 秒）。 */
    public static final int PRODUCE_INTERVAL = 200;

    private int progress = 0;

    /** 产出缓冲：仅氢气瓶（内部产出 / 外部抽取；限定物品避免异物注入）。 */
    private final ItemStackHandler output = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(TSItems.HYDROGEN_CAPSULE.get());
        }
    };
    //? if forge {
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> output);
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
        if (progress >= PRODUCE_INTERVAL) {
            progress = 0;
            insertToBuffer(new ItemStack(TSItems.HYDROGEN_CAPSULE.get(), 1));
            level.sendParticles(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.0);
        }
        pushOutputs(level, pos);
        setChanged();
    }

    private void insertToBuffer(ItemStack stack) {
        for (int i = 0; i < output.getSlots() && !stack.isEmpty(); i++) {
            stack = output.insertItem(i, stack, false);
        }
        if (!stack.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5, stack);
        }
    }

    /** 把缓冲推入相邻容器（除正下方菌毯外的各方向）。 */
    private void pushOutputs(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) {
                continue;
            }
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) {
                continue;
            }
            //? if forge {
            IItemHandler dest = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            //?} else {
            /*IItemHandler dest = Capabilities.ItemHandler.BLOCK.getCapability(level, pos.relative(dir), null, neighbor, dir.getOpposite());
            *///?}
            if (dest == null) {
                continue;
            }
            for (int i = 0; i < output.getSlots(); i++) {
                ItemStack stack = output.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    output.setStackInSlot(i, ItemHandlerHelper.insertItem(dest, stack, false));
                }
            }
        }
    }

    /** 破坏时掉落缓冲内容。 */
    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < output.getSlots(); i++) {
            ItemStack stack = output.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                output.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    //? if forge {
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.put("Output", output.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        if (tag.contains("Output")) {
            output.deserializeNBT(tag.getCompound("Output"));
        }
    }
    //?} else {
    /*public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                TSBlockEntities.HYDROGEN_COLLECTOR.get(), (be, side) -> be.output);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.put("Output", output.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        if (tag.contains("Output")) {
            output.deserializeNBT(registries, tag.getCompound("Output"));
        }
    }
    *///?}
}
