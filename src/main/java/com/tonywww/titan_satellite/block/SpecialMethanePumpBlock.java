package com.tonywww.titan_satellite.block;

import com.tonywww.titan_satellite.blockentity.SpecialMethanePumpBlockEntity;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 特制甲烷泵方块（带方块实体）。置于 {@code methane_pool_core} 上方，右键激活开采塔防：
 * 服务端 ticker 驱动 {@link SpecialMethanePumpBlockEntity} 状态机；运行中被破坏则判定失败。
 */
public class SpecialMethanePumpBlock extends BaseEntityBlock {

    public SpecialMethanePumpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if neoforge {
    /*@Override
    public com.mojang.serialization.MapCodec<SpecialMethanePumpBlock> codec() {
        return simpleCodec(SpecialMethanePumpBlock::new);
    }
    *///?}

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpecialMethanePumpBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, TSBlockEntities.METHANE_PUMP.get(), SpecialMethanePumpBlockEntity::serverTick);
    }

    //? if forge {
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel server
                && server.getBlockEntity(pos) instanceof SpecialMethanePumpBlockEntity pump) {
            pump.onActivatedBy(server, pos, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server
                && server.getBlockEntity(pos) instanceof SpecialMethanePumpBlockEntity pump) {
            pump.onActivatedBy(server, pos, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (level instanceof ServerLevel server
                && server.getBlockEntity(pos) instanceof SpecialMethanePumpBlockEntity pump) {
            pump.onNeighborSignalChanged(server, pos, level.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level instanceof ServerLevel server
                    && server.getBlockEntity(pos) instanceof SpecialMethanePumpBlockEntity pump) {
                pump.onDestroyed(server, pos);
                pump.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
