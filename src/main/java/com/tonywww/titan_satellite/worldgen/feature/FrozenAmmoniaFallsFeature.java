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
 * 冻氨巨瀑（T7.3 · 宏伟地物）：冰火山断崖沿最大落差方向倾泻的<b>冻结</b>氨瀑——崖顶氨潭 + 贴崖面
 * 竖直冻氨冰帘（{@code cryo_ice} + 氨晶点缀）。全为实心方块（无流动风险）；仅在明显崖沿生成。
 */
public class FrozenAmmoniaFallsFeature extends Feature<NoneFeatureConfiguration> {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public FrozenAmmoniaFallsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int reach = 3;
        int topY = origin.getY();

        int[] bestDir = null;
        int lowest = topY;
        for (int[] d : DIRS) {
            int nTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,
                    origin.getX() + d[0] * reach, origin.getZ() + d[1] * reach) - 1;
            if (nTop < lowest) {
                lowest = nTop;
                bestDir = d;
            }
        }
        if (bestDir == null || topY - lowest < 5) {
            return false;
        }

        BlockState frozen = TSBlocks.CRYO_ICE.get().defaultBlockState();
        BlockState accent = TSBlocks.AMMONIA_CRYSTAL.get().defaultBlockState();
        BlockState ammonia = TSBlocks.LIQUID_AMMONIA_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int perpX = bestDir[1];
        int perpZ = bestDir[0];
        int edgeX = origin.getX() + bestDir[0];
        int edgeZ = origin.getZ() + bestDir[1];
        boolean placed = false;

        // 崖顶氨潭
        for (int w = -1; w <= 1; w++) {
            pos.set(origin.getX() + perpX * w, topY, origin.getZ() + perpZ * w);
            if (!level.getBlockState(pos).isAir()) {
                setBlock(level, pos, ammonia);
                level.scheduleTick(pos.immutable(), ammonia.getFluidState().getType(), 5);
                placed = true;
            }
        }
        // 冻氨冰帘
        for (int y = topY; y >= lowest; y--) {
            for (int w = -1; w <= 1; w++) {
                pos.set(edgeX + perpX * w, y, edgeZ + perpZ * w);
                if (level.getBlockState(pos).isAir()) {
                    setBlock(level, pos, random.nextInt(5) == 0 ? accent : frozen);
                    placed = true;
                }
            }
        }
        return placed;
    }
}
