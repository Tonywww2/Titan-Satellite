package com.tonywww.titan_satellite.block;

import com.tonywww.titan_satellite.blockentity.HydrogenCollectorBlockEntity;
import com.tonywww.titan_satellite.registry.TSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 集氢罩方块（带方块实体）。架在 {@code hydrogen_bubble_mat} 正上方即被动产氢
 * （逻辑见 {@link HydrogenCollectorBlockEntity}）。菌毯移除即停。
 */
public class HydrogenCollectorBlock extends BaseEntityBlock {

    public HydrogenCollectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if neoforge {
    /*@Override
    public com.mojang.serialization.MapCodec<HydrogenCollectorBlock> codec() {
        return simpleCodec(HydrogenCollectorBlock::new);
    }
    *///?}

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HydrogenCollectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, TSBlockEntities.HYDROGEN_COLLECTOR.get(), HydrogenCollectorBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof HydrogenCollectorBlockEntity be) {
                be.dropContents(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
}
