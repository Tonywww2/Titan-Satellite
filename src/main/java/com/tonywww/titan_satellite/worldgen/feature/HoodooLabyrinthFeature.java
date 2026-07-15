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
 * 石林迷城（宏伟地物）：荒芜高原上巨型风化石林组成的天然迷宫——一片密集的锥形风化石柱
 * （{@code weathered_titan_stone}），每根从<b>该列真实地表</b>逐层收窄向上，穿行如迷城。
 */
public class HoodooLabyrinthFeature extends Feature<NoneFeatureConfiguration> {

    public HoodooLabyrinthFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 12 + random.nextInt(7);           // 石林半径 12-18
        int count = 12 + random.nextInt(13);           // 12-24 根
        BlockState stone = TSBlocks.WEATHERED_TITAN_STONE.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int i = 0; i < count; i++) {
            int ox = random.nextInt(radius * 2 + 1) - radius;
            int oz = random.nextInt(radius * 2 + 1) - radius;
            if (ox * ox + oz * oz > radius * radius) {
                continue;
            }
            int wx = origin.getX() + ox;
            int wz = origin.getZ() + oz;
            int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz) - 1;
            int height = 4 + random.nextInt(9);        // 4-12 高
            int baseRad = 1 + random.nextInt(2);       // 1-2 基半径
            for (int dy = 0; dy < height; dy++) {
                double t = 1.0D - (double) dy / height;
                int r = (int) Math.round(baseRad * t);
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (dx * dx + dz * dz > r * r + 1) {
                            continue;
                        }
                        pos.set(wx + dx, terrainTop + 1 + dy, wz + dz);
                        if (level.getBlockState(pos).isAir()) {
                            setBlock(level, pos, stone);
                            placed = true;
                        }
                    }
                }
            }
        }
        return placed;
    }
}
