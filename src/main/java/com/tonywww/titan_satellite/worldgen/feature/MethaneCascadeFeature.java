package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 液甲烷倾瀑（宏伟地物）：在液态甲烷深渊的陡崖边缘，沿最大落差方向挂一道液态甲烷瀑布片
 * （崖顶喂给池 + 贴崖面竖直液帘 + 崖底积潭）。
 *
 * <p>仅在存在明显落差（≥5 格）的崖沿生成，否则放弃——保证是「倾瀑」而非平地水洼。液体直接以方块铺设
 * （不依赖世界生成期的流动），并 {@code scheduleTick} 让其稳定。
 */
public class MethaneCascadeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public MethaneCascadeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int reach = 3;
        int topY = origin.getY();

        // 找落差最大的方向（崖沿）
        int[] bestDir = null;
        int lowest = topY;
        for (int[] d : DIRS) {
            int nx = origin.getX() + d[0] * reach;
            int nz = origin.getZ() + d[1] * reach;
            int nTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, nx, nz) - 1;
            if (nTop < lowest) {
                lowest = nTop;
                bestDir = d;
            }
        }
        int drop = topY - lowest;
        if (bestDir == null || drop < 5) {
            return false;
        }
        int minY = level.getMinBuildHeight();
        int floorLimit = Math.max(lowest, minY + 5);   // 基岩守卫：液帘/积潭不低于基岩带

        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int perpX = bestDir[1];
        int perpZ = bestDir[0];
        int edgeX = origin.getX() + bestDir[0];
        int edgeZ = origin.getZ() + bestDir[1];
        boolean placed = false;

        for (int w = -1; w <= 1; w++) {
            pos.set(origin.getX() + perpX * w, topY, origin.getZ() + perpZ * w);
            if (!level.getBlockState(pos).isAir()) {
                setBlock(level, pos, methane);
                level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                placed = true;
            }
        }
        // 贴崖面竖直液帘（宽 3），从崖顶淌到崖底
        for (int y = topY; y >= floorLimit; y--) {
            for (int w = -1; w <= 1; w++) {
                pos.set(edgeX + perpX * w, y, edgeZ + perpZ * w);
                if (level.getBlockState(pos).isAir()) {
                    setBlock(level, pos, methane);
                    level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                    placed = true;
                }
            }
        }
        // 崖底积潭
        for (int w = -1; w <= 1; w++) {
            pos.set(edgeX + perpX * w, floorLimit, edgeZ + perpZ * w);
            if (!level.getBlockState(pos).isAir()) {
                setBlock(level, pos, methane);
                level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                placed = true;
            }
        }
        return placed;
    }
}
