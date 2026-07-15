package com.tonywww.titan_moon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 甲烷池核心方块：开采塔防的触发标记块。玩家在其正上方放置甲烷泵并激活，
 * 泵方块实体检测本块以驱动波次状态机（见 {@code SpecialMethanePumpBlockEntity}）。
 * 本块自身仅提供环境粒子表现。
 */
public class MethanePoolCoreBlock extends Block {

    public MethanePoolCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(),
                    0.0, 0.02, 0.0);
        }
    }
}
