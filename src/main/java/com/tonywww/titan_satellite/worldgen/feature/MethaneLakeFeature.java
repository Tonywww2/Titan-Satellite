package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 甲烷湖特征：在深渊/陨坑底部挖出凹陷并填液态甲烷。
 */
public class MethaneLakeFeature extends Feature<NoneFeatureConfiguration> {

    public MethaneLakeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int radius = 3 + random.nextInt(3);            // 3-5
        int depth = 2 + random.nextInt(3);             // 2-4
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();
        boolean placed = false;
        // 嵌入地形深处：仅把实心方块替换为液态甲烷，形成被岩体包裹的封闭甲烷囊（不开口、不外溢）
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > radius * radius) {
                    continue;
                }
                int localDepth = distSq <= (radius - 1) * (radius - 1) ? depth : Math.max(1, depth - 1);
                for (int dy = 0; dy > -localDepth; dy--) {
                    int y = origin.getY() + dy;
                    if (y < minY + 5) {
                        break;                                   // 基岩守卫：不下探到基岩层(Y 0-4)
                    }
                    pos.set(origin.getX() + dx, y, origin.getZ() + dz);
                    BlockState existing = level.getBlockState(pos);
                    if (!existing.isAir() && !existing.is(Blocks.BEDROCK)) {
                        setBlock(level, pos, methane);
                        level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
