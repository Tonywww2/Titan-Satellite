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
 * 冰层天坑特征（PG-4 增大为巨型）：极地迷宫冰原下巨大的冰窟窿（半径 4-7、深 14-26），直通下层地形。
 */
public class IceSinkholeFeature extends Feature<NoneFeatureConfiguration> {

    public IceSinkholeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int radius = 4 + random.nextInt(4);   // 巨型：半径 4-7
        int depth = 14 + random.nextInt(13);  // 深 14-26，直通下层
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState ice = TSBlocks.PACKED_METHANE_ICE.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int dy = 0; dy > -depth; dy--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (horiz <= radius - 0.5D) {
                        setBlock(level, pos, air);
                        placed = true;
                    } else if (horiz <= radius + 0.5D) {
                        setBlock(level, pos, ice);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
