package com.tonywww.titan_satellite.block;

import com.tonywww.titan_satellite.blockentity.TholinComposterBlockEntity;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
//? if neoforge {
/*import net.minecraft.world.ItemInteractionResult;
*///?}
import org.jetbrains.annotations.Nullable;

/**
 * 托林堆肥槽方块（带方块实体）。架在 {@code tholin_mycelium} 正上方，吞生物残渣产托林粉末
 * （逻辑见 {@link TholinComposterBlockEntity}）。右键手持残渣可塞入；漏斗/管道亦可自动化。
 */
public class TholinComposterBlock extends BaseEntityBlock {

    public TholinComposterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if neoforge {
    /*@Override
    public com.mojang.serialization.MapCodec<TholinComposterBlock> codec() {
        return simpleCodec(TholinComposterBlock::new);
    }
    *///?}

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TholinComposterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, TSBlockEntities.THOLIN_COMPOSTER.get(), TholinComposterBlockEntity::serverTick);
    }

    /** 尝试把手持残渣塞入输入槽；返回是否消耗了物品。 */
    private boolean handleInsert(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server
                && server.getBlockEntity(pos) instanceof TholinComposterBlockEntity be) {
            ItemStack held = player.getItemInHand(hand);
            if (be.tryInsert(held)) {
                server.playSound(null, pos, net.minecraft.sounds.SoundEvents.COMPOSTER_FILL,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 1.0F);
                return true;
            }
            player.displayClientMessage(Component.translatable("block.titan_satellite.tholin_composter.hint"), true);
        }
        return false;
    }

    //? if forge {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        handleInsert(level, pos, player, hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (handleInsert(level, pos, player, hand)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TholinComposterBlockEntity be) {
                be.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
