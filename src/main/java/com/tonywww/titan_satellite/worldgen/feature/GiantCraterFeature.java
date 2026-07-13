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
                // 逐列取真实地表：terrainTop=实心岩顶（忽略无碰撞地物/流体），surfaceTop=含地物/流体顶。
                // 坑体一律相对**该列真实地表**放置、绝不在半空落方块——破碎/悬崖地形下才不会出现悬浮坑底/坑缘。
                int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz) - 1;
                int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz) - 1;
                if (horiz <= radius - 2) {
                    // 碗形凹陷：中心深、向外变浅
                    int bowl = (int) ((radius - 2 - horiz) * 0.9D) + 1;
                    int floorY = origin.getY() - bowl;
                    if (terrainTop <= floorY) {
                        // 该列真实地表已在碗底或更低（悬崖/深谷）：不铺碗底(否则悬浮)，仅铲掉其上方残留的悬浮地物
                        for (int y = surfaceTop; y > terrainTop; y--) {
                            pos.set(wx, y, wz);
                            if (!level.getBlockState(pos).isAir()) {
                                setBlock(level, pos, air);
                            }
                        }
                        continue;
                    }
                    // 把该列真实顶端（含邻区已放置地物）一路清空到碗底之上，再在碗底铺深层岩（砸穿地层）
                    for (int y = surfaceTop; y > floorY; y--) {
                        pos.set(wx, y, wz);
                        setBlock(level, pos, air);
                    }
                    pos.set(wx, floorY, wz);
                    setBlock(level, pos, deep);
                    placed = true;
                    if (hasPool && horiz < (radius - 2) * 0.35D) {
                        pos.set(wx, floorY + 1, wz);
                        setBlock(level, pos, methane);     // 坑底微型甲烷湖
                    }
                } else {
                    // 凸起坑缘环：贴合该列真实地表垒一圈矮唇（始终连地、不悬浮）
                    double t = 1.0D - Math.abs(horiz - (radius - 0.5D)) / 1.5D;
                    if (t > 0.0D) {
                        int rimH = 1 + (int) (t * 3.0D);   // 峰高 1-4
                        // 先铲掉地表以上残留（邻区悬浮地物），再从真实地表往上垒 rim
                        for (int y = surfaceTop; y > terrainTop; y--) {
                            pos.set(wx, y, wz);
                            if (!level.getBlockState(pos).isAir()) {
                                setBlock(level, pos, air);
                            }
                        }
                        for (int dy = 1; dy <= rimH; dy++) {
                            pos.set(wx, terrainTop + dy, wz);
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
