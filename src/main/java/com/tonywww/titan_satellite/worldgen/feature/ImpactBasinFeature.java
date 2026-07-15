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
 * 陨星撞击盆地（宏伟地物）：远大于普通陨石坑的超级盆地——宽碗形凹陷 + 抬升坑缘 +
 * 中央半埋的陨铁矿核（可开采 {@code meteor_fragment}）+ 向外若干辐射溅射脊线。
 *
 * <p>遵 repo 地物防悬浮约定：碗体/坑缘/脊线每一块都相对<b>该列真实地表</b>放置，绝不 origin 相对、
 * 绝不半空落方块（{@code OCEAN_FLOOR_WG}=实心顶，{@code WORLD_SURFACE_WG}=含地物顶用于铲悬浮）。
 * 半径较大，超出可写区的远列会被原版丢弃（非致命）。
 */
public class ImpactBasinFeature extends Feature<NoneFeatureConfiguration> {

    public ImpactBasinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 16 + random.nextInt(7);           // 半径 16-22（远大于 giant_crater 9-14）
        int coreRadius = 2 + random.nextInt(2);        // 中央陨铁矿核半径 2-3
        double spokePhase = random.nextDouble() * Math.PI * 2.0D;
        int spokes = 5 + random.nextInt(3);            // 辐射脊线条数 5-7

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState surface = TSBlocks.WEATHERED_TITAN_STONE.get().defaultBlockState();
        BlockState deep = TSBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState core = TSBlocks.METEOR_FRAGMENT.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        int minY = level.getMinBuildHeight();
        // 仅允许读写 origin 所在区块的 3x3 邻域：半径大(16-22)易越界，越界列既不读 getHeight(避免越界读崩溃)也不写
        int minChunkX = (origin.getX() >> 4) - 1;
        int maxChunkX = (origin.getX() >> 4) + 1;
        int minChunkZ = (origin.getZ() >> 4) - 1;
        int maxChunkZ = (origin.getZ() >> 4) + 1;

        int outer = (int) (radius * 1.5D);             // 溅射脊线延伸到 1.5R
        for (int dx = -outer - 1; dx <= outer + 1; dx++) {
            for (int dz = -outer - 1; dz <= outer + 1; dz++) {
                double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                if (horiz > outer + 1) {
                    continue;
                }
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                if ((wx >> 4) < minChunkX || (wx >> 4) > maxChunkX || (wz >> 4) < minChunkZ || (wz >> 4) > maxChunkZ) {
                    continue;                          // 越界丢弃：远区块不读不写，杜绝越界读崩溃 + far-chunk 写报错
                }
                int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz) - 1;
                int surfaceTop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz) - 1;

                if (horiz <= radius - 3) {
                    // 宽碗形凹陷：中心最深、向外变浅
                    int bowl = (int) ((radius - 3 - horiz) * 0.72D) + 1;
                    int floorY = Math.max(origin.getY() - bowl, minY + 5);   // 基岩守卫：盆底不低于基岩带
                    if (terrainTop <= floorY) {
                        for (int y = surfaceTop; y > terrainTop; y--) {
                            pos.set(wx, y, wz);
                            if (!level.getBlockState(pos).isAir()) {
                                setBlock(level, pos, air);
                            }
                        }
                        continue;
                    }
                    for (int y = surfaceTop; y > floorY; y--) {
                        setBlock(level, pos.set(wx, y, wz), air);
                    }
                    setBlock(level, pos.set(wx, floorY, wz), deep);
                    placed = true;
                    // 中央半埋陨铁矿核：碗底中心堆一团 meteor_fragment
                    if (horiz <= coreRadius) {
                        int coreH = coreRadius - (int) horiz;
                        for (int cy = 0; cy <= coreH; cy++) {
                            setBlock(level, pos.set(wx, floorY + cy, wz), core);
                        }
                    }
                } else if (horiz <= radius + 0.5D) {
                    // 抬升坑缘环：贴该列真实地表垒一圈唇
                    double t = 1.0D - Math.abs(horiz - (radius - 0.5D)) / 2.0D;
                    if (t > 0.0D) {
                        int rimH = 1 + (int) (t * 4.0D);
                        for (int y = surfaceTop; y > terrainTop; y--) {
                            pos.set(wx, y, wz);
                            if (!level.getBlockState(pos).isAir()) {
                                setBlock(level, pos, air);
                            }
                        }
                        for (int dy = 1; dy <= rimH; dy++) {
                            setBlock(level, pos.set(wx, terrainTop + dy, wz), surface);
                        }
                        placed = true;
                    }
                } else {
                    // 辐射溅射脊线：沿若干条幅射方向在坑缘外垒低脊
                    double angle = Math.atan2(dz, dx);
                    double spokeMask = Math.cos((angle - spokePhase) * spokes);
                    if (spokeMask > 0.6D) {
                        double fade = 1.0D - (horiz - radius) / (outer - radius);
                        int ridgeH = (int) (spokeMask * fade * 3.0D);
                        if (ridgeH >= 1) {
                            for (int dy = 1; dy <= ridgeH; dy++) {
                                pos.set(wx, terrainTop + dy, wz);
                                if (level.getBlockState(pos).isAir()) {
                                    setBlock(level, pos, surface);
                                    placed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }
}
