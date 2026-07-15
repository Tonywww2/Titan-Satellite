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
 * 托林天生巨拱（宏伟地物）：横跨沙谷的巨型风蚀天生桥（{@code hardened_tholin}）。半圆拱券，
 * 相邻列以竖直连接保证曲线不断裂，两足垂直落到<b>各自列真实地表</b>接地（拱心悬于中央、为拱之本意）。
 */
public class TholinArchFeature extends Feature<NoneFeatureConfiguration> {

    public TholinArchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        boolean alongX = random.nextBoolean();
        int halfSpan = 6 + random.nextInt(5);          // 跨度 12-20
        int archHeight = 6 + random.nextInt(5);        // 拱高 6-10
        int halfW = 1;                                  // 拱宽 3
        int band = 2;                                   // 拱材竖向厚度
        int baseY = origin.getY();
        int perpX = alongX ? 0 : 1;
        int perpZ = alongX ? 1 : 0;

        BlockState tholin = TSBlocks.HARDENED_THOLIN.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int prevCrownY = Integer.MIN_VALUE;
        boolean placed = false;

        for (int l = -halfSpan; l <= halfSpan; l++) {
            double p = 1.0D - (double) (l * l) / (double) (halfSpan * halfSpan);
            double profile = p <= 0.0D ? 0.0D : Math.sqrt(p);
            int crownY = baseY + (int) (profile * archHeight);
            int cx = origin.getX() + (alongX ? l : 0);
            int cz = origin.getZ() + (alongX ? 0 : l);
            for (int w = -halfW; w <= halfW; w++) {
                int px = cx + perpX * w;
                int pz = cz + perpZ * w;
                for (int b = 0; b < band; b++) {
                    setBlock(level, pos.set(px, crownY - b, pz), tholin);
                    placed = true;
                }
                if (prevCrownY != Integer.MIN_VALUE) {
                    int lo = Math.min(crownY, prevCrownY);
                    int hi = Math.max(crownY, prevCrownY);
                    for (int y = lo; y <= hi; y++) {
                        setBlock(level, pos.set(px, y, pz), tholin);
                    }
                }
            }
            prevCrownY = crownY;
        }

        // 两足垂直接地
        for (int end = -halfSpan; end <= halfSpan; end += 2 * halfSpan) {
            int cx = origin.getX() + (alongX ? end : 0);
            int cz = origin.getZ() + (alongX ? 0 : end);
            for (int w = -halfW; w <= halfW; w++) {
                int px = cx + perpX * w;
                int pz = cz + perpZ * w;
                int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, px, pz) - 1;
                for (int y = baseY; y > terrainTop; y--) {
                    pos.set(px, y, pz);
                    if (level.getBlockState(pos).isAir()) {
                        setBlock(level, pos, tholin);
                    }
                }
            }
        }
        return placed;
    }
}
