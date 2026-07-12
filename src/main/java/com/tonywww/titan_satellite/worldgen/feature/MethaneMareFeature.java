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
 * 甲烷海特征（PG-4）：液态甲烷深渊大面积地表塌陷至液面以下、被液态甲烷淹没，形成开阔的甲烷之海。
 *
 * <p>大半径碗形下沉（中心深、边缘浅）；下沉区上方清空塌陷地表、液面及以下填液态甲烷。
 */
public class MethaneMareFeature extends Feature<NoneFeatureConfiguration> {

    public MethaneMareFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 12 + random.nextInt(9);           // 大半径 12-20
        int sinkDepth = 5 + random.nextInt(4);         // 中心下沉深度 5-8
        int clearAbove = 4;                            // 上方清空高度

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                if (horiz > radius) {
                    continue;
                }
                double edge = 1.0D - horiz / radius;                  // 中心 1 → 边缘 0
                int localSink = Math.max(1, (int) (sinkDepth * edge));
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                // 嵌入地形深处：不清空上方地表（避免高空开口/液体外溢），仅把实心替换为液态甲烷
                for (int dy = 0; dy > -localSink; dy--) {
                    pos.set(wx, origin.getY() + dy, wz);
                    if (!level.getBlockState(pos).isAir()) {
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
