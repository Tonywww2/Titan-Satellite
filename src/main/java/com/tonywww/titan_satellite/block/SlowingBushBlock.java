package com.tonywww.titan_satellite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 减速灌木（托林灌木 / 霜枯灌木）：无碰撞可穿过，但踩入时显著拖慢移动（仿甜浆果丛 / 蛛网的
 * {@code makeStuckInBlock}）。剪刀采集掉落见 {@code TSBlockLoot}（托林纤维）。
 */
@SuppressWarnings("deprecation")
public class SlowingBushBlock extends Block {

    /** 穿行时的运动衰减系数（x/z 0.8、y 0.75，越小越黏）。 */
    private static final Vec3 STUCK = new Vec3(0.8D, 0.75D, 0.8D);

    public SlowingBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, STUCK);
    }
}
