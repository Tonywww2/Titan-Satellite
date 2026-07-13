package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 巨型陨石坑特征（PA-2 桩）。M1（PB-3）填充：砸穿地层的巨大凹陷，底部露出微型甲烷湖。
 */
public class GiantCraterFeature extends Feature<NoneFeatureConfiguration> {

    public GiantCraterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int radius = 9 + random.nextInt(6);            // 半径 9-14
        boolean hasPool = random.nextInt(4) == 0;      // 25% 坑底露出微型甲烷湖
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState surface = TSBlocks.WEATHERED_TITAN_STONE.get().defaultBlockState();
        BlockState deep = TSBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                if (horiz > radius + 1) {
                    continue;
                }
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                if (horiz <= radius - 2) {
                    // 碗形凹陷：中心深、向外变浅
                    int bowl = (int) ((radius - 2 - horiz) * 0.9D) + 1;
                    // 从该列真实地表一路清空到碗底，而非仅清 origin+3：陨石坑会跨区块写入，
                    // 邻近区块可能已先跑完 vegetal（树枝结晶/霜枯灌木）；仅清 +3 会在坑上方留下
                    // 悬浮地物。用 WORLD_SURFACE_WG 取该列（含邻区已放置方块）真实顶端一并挖除。
                    int columnTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz) - 1;
                    int clearTop = Math.max(columnTop, origin.getY() + 3);
                    for (int y = clearTop; y > origin.getY() - bowl; y--) {
                        pos.set(wx, y, wz);
                        setBlock(level, pos, air);
                    }
                    pos.set(wx, origin.getY() - bowl, wz);
                    setBlock(level, pos, deep);            // 坑底露出深层岩（砸穿地层）
                    placed = true;
                    if (hasPool && horiz < (radius - 2) * 0.35D) {
                        pos.set(wx, origin.getY() - bowl + 1, wz);
                        setBlock(level, pos, methane);     // 坑底微型甲烷湖
                    }
                } else {
                    // 凸起坑缘环：抬升的环形边缘
                    double t = 1.0D - Math.abs(horiz - (radius - 0.5D)) / 1.5D;
                    if (t > 0.0D) {
                        int rimH = 1 + (int) (t * 3.0D);   // 峰高 1-4
                        for (int dy = 1; dy <= rimH; dy++) {
                            pos.set(wx, origin.getY() + dy, wz);
                            setBlock(level, pos, surface);
                        }
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
