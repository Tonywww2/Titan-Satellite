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
 * 悬冰大殿 / 冰晶穹顶（宏伟地物）：极地迷宫冰原里一座巨型中空冰穹——上半球为地表可见穹顶、
 * 下半球嵌入厚冰（故整壳接地不悬浮），内部中空；殿心立冰柱、地面散布冰刺与甲烷冰花、殿底积甲烷潭。
 */
public class IceCathedralFeature extends Feature<NoneFeatureConfiguration> {

    public IceCathedralFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 8 + random.nextInt(4);            // 半径 8-11
        BlockState shell = TSBlocks.CRYO_ICE.get().defaultBlockState();
        BlockState accent = TSBlocks.PACKED_METHANE_ICE.get().defaultBlockState();
        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockState bloom = TSBlocks.METHANE_ICE_BLOOM.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // 严格防悬浮：球心锤到该列真实实心地表(OCEAN_FLOOR_WG=实心顶)，而非 placed_feature 的
        // WORLD_SURFACE_WG(含流体面/噪声估算)——下半球嵌入实心冰、上半球为可见穹顶(设计意图)，绝不悬浮于流体面/半空。
        int centerY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ()) - 1;
        int floorY = centerY - (radius - 2);
        boolean placed = false;

        // 中空冰壳（整球，上半可见穹顶、下半嵌冰）
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    pos.set(origin.getX() + dx, centerY + dy, origin.getZ() + dz);
                    if (dist <= radius - 1.5D) {
                        setBlock(level, pos, air);
                        placed = true;
                    } else if (dist <= radius + 0.5D) {
                        setBlock(level, pos, random.nextInt(4) == 0 ? accent : shell);
                        placed = true;
                    }
                }
            }
        }

        // 殿底地板 + 中央甲烷潭
        int rFloor = radius - 2;
        for (int dx = -rFloor; dx <= rFloor; dx++) {
            for (int dz = -rFloor; dz <= rFloor; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= rFloor * rFloor) {
                    setBlock(level, pos.set(origin.getX() + dx, floorY, origin.getZ() + dz),
                            d2 <= 4 ? methane : accent);
                }
            }
        }

        // 殿心冰柱
        for (int y = floorY + 1; y <= centerY + radius - 2; y++) {
            setBlock(level, pos.set(origin.getX(), y, origin.getZ()), shell);
        }

        // 冰刺 + 甲烷冰花散布地面
        for (int i = 0; i < 8; i++) {
            int ox = random.nextInt(rFloor * 2 + 1) - rFloor;
            int oz = random.nextInt(rFloor * 2 + 1) - rFloor;
            if (ox * ox + oz * oz > rFloor * rFloor || (ox == 0 && oz == 0)) {
                continue;
            }
            if (random.nextBoolean()) {
                int spikeH = 2 + random.nextInt(4);
                for (int k = 1; k <= spikeH; k++) {
                    setBlock(level, pos.set(origin.getX() + ox, floorY + k, origin.getZ() + oz), accent);
                }
            } else {
                setBlock(level, pos.set(origin.getX() + ox, floorY + 1, origin.getZ() + oz), bloom);
            }
        }
        return placed;
    }
}
