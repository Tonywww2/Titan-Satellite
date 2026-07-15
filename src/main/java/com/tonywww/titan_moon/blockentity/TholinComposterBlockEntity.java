package com.tonywww.titan_moon.blockentity;

import com.tonywww.titan_moon.config.TMConfig;
import com.tonywww.titan_moon.registry.TMBlockEntities;
import com.tonywww.titan_moon.registry.TMBlocks;
import com.tonywww.titan_moon.registry.TMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * 托林堆肥槽方块实体：被动分解者。仅当正下方为 {@code tholin_mycelium} 时，
 * 按间隔消耗输入槽的生物残渣/纤维 → 产出 {@code tholin_dust} 到输出槽（分解者闭环）。
 * 以 ITEM_HANDLER 能力暴露：外部仅可向输入槽注入残渣、从输出槽抽取托林粉末。
 */
public class TholinComposterBlockEntity extends BlockEntity {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int progress = 0;

    private final ComposterInv inv = new ComposterInv();
    //? if forge {
    private final LazyOptional<IItemHandler> itemCap = LazyOptional.of(() -> inv);
    //?}

    public TholinComposterBlockEntity(BlockPos pos, BlockState state) {
        super(TMBlockEntities.THOLIN_COMPOSTER.get(), pos, state);
    }

    /** 生物残渣/纤维判定（输入槽接受集）。 */
    public static boolean isBiomatter(ItemStack stack) {
        return stack.is(TMItems.THOLIN_FIBRE.get())
                || stack.is(TMItems.THOLIN_SILK_SAC.get())
                || stack.is(TMItems.CRYSTALLINE_TWIG.get())
                || stack.is(Items.ROTTEN_FLESH);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TholinComposterBlockEntity be) {
        if (level instanceof ServerLevel server) {
            be.tick(server, pos);
        }
    }

    private void tick(ServerLevel level, BlockPos pos) {
        // 依附检测：正下方须为托林菌网
        boolean onMycelium = level.getBlockState(pos.below()).is(TMBlocks.THOLIN_MYCELIUM.get());
        ItemStack input = inv.getStackInSlot(INPUT_SLOT);
        if (!onMycelium || input.isEmpty() || !canOutput()) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            pushOutputs(level, pos);
            return;
        }
        progress++;
        if (progress >= TMConfig.COMPOSTER_PROCESS_INTERVAL.get()) {
            progress = 0;
            input.shrink(1);
            inv.setStackInSlot(INPUT_SLOT, input);
            // 产出 1 托林粉末到输出槽（内部直写，绕过 isItemValid）
            ItemStack out = inv.getStackInSlot(OUTPUT_SLOT);
            if (out.isEmpty()) {
                inv.setStackInSlot(OUTPUT_SLOT, new ItemStack(TMItems.THOLIN_DUST.get(), 1));
            } else {
                out.grow(1);
                inv.setStackInSlot(OUTPUT_SLOT, out);
            }
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 4, 0.25, 0.1, 0.25, 0.0);
        }
        pushOutputs(level, pos);
        setChanged();
    }

    /** 输出槽是否还能容纳托林粉末。 */
    private boolean canOutput() {
        ItemStack out = inv.getStackInSlot(OUTPUT_SLOT);
        return out.isEmpty() || (out.is(TMItems.THOLIN_DUST.get()) && out.getCount() < out.getMaxStackSize());
    }

    /** 玩家手持残渣右键 → 塞入输入槽（便捷交互，返回是否消耗）。 */
    public boolean tryInsert(ItemStack held) {
        if (!isBiomatter(held)) {
            return false;
        }
        ItemStack rem = inv.insertItem(INPUT_SLOT, held.copy(), false);
        int inserted = held.getCount() - rem.getCount();
        if (inserted > 0) {
            held.shrink(inserted);
            setChanged();
            return true;
        }
        return false;
    }

    /** 把输出槽的托林粉末推入相邻容器（除正下方菌网外的各方向）。 */
    private void pushOutputs(ServerLevel level, BlockPos pos) {
        ItemStack out = inv.getStackInSlot(OUTPUT_SLOT);
        if (out.isEmpty()) {
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
            IItemHandler dest = neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
            //?} else {
            /*IItemHandler dest = Capabilities.ItemHandler.BLOCK.getCapability(level, pos.relative(dir), null, neighbor, dir.getOpposite());
            *///?}
            if (dest == null) {
                continue;
            }
            ItemStack remaining = ItemHandlerHelper.insertItem(dest, inv.getStackInSlot(OUTPUT_SLOT), false);
            inv.setStackInSlot(OUTPUT_SLOT, remaining);
            if (remaining.isEmpty()) {
                return;
            }
        }
    }

    /** 破坏时掉落两槽内容。 */
    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                inv.setStackInSlot(i, ItemStack.EMPTY);
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
        tag.put("Inv", inv.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        if (tag.contains("Inv")) {
            inv.deserializeNBT(tag.getCompound("Inv"));
        }
    }
    //?} else {
    /*public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                TMBlockEntities.THOLIN_COMPOSTER.get(), (be, side) -> be.inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", progress);
        tag.put("Inv", inv.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("Progress");
        if (tag.contains("Inv")) {
            inv.deserializeNBT(registries, tag.getCompound("Inv"));
        }
    }
    *///?}

    /** 2 槽：0=输入(仅残渣可外部注入)、1=输出(仅可外部抽取)。 */
    public static class ComposterInv extends ItemStackHandler {
        public ComposterInv() {
            super(2);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && isBiomatter(stack);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == INPUT_SLOT) {
                return ItemStack.EMPTY; // 输入槽不可外部抽取
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
