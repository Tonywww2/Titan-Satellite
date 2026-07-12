package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 巨型沙脊特征（PA-2 桩）。M1（PB-3）填充：托林沙海中高墙般的狭长山脊。
 */
public class MegayardangFeature extends Feature<NoneFeatureConfiguration> {

    public MegayardangFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        boolean alongX = random.nextBoolean();
        int half = 5 + random.nextInt(4);
        int maxHeight = 4 + random.nextInt(4);
        BlockState sand = TSBlocks.THOLIN_SAND.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int l = -half; l <= half; l++) {
            double t = 1.0D - (double) Math.abs(l) / (double) (half + 1);
            int h = Math.max(1, (int) (maxHeight * t));
            for (int w = -1; w <= 1; w++) {
                int dx = alongX ? l : w;
                int dz = alongX ? w : l;
                for (int dy = 0; dy < h; dy++) {
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    setBlock(level, pos, sand);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
