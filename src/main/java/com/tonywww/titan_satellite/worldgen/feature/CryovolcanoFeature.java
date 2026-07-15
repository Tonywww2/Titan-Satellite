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
 * 主冰火山「白喉」（宏伟地物 · 群系皇冠）：中央巨型冰火山锥 + 山巅氨水火山口湖 + 满山喷泉。
 *
 * <p>实心锥体逐列从<b>该列真实地表</b>往上填 {@code cryo_ice}（不悬浮）；峰顶挖出火山口注液氨；
 * 山腰点缀若干 {@code cryovolcanic_geyser}（击飞喷泉，可供玩家借力登顶）。半径较大，远列超出可写区被丢弃。
 */
public class CryovolcanoFeature extends Feature<NoneFeatureConfiguration> {

    public CryovolcanoFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int baseRadius = 12 + random.nextInt(5);       // 底半径 12-16
        int height = 18 + random.nextInt(9);           // 锥高 18-26
        int calderaDepth = 5 + random.nextInt(3);      // 火山口深 5-7
        double calderaR = baseRadius * 0.30D;
        int baseY = origin.getY();
        int calderaFloorY = baseY + height - calderaDepth;

        BlockState ice = TSBlocks.CRYO_ICE.get().defaultBlockState();
        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockState ammonia = TSBlocks.LIQUID_AMMONIA_BLOCK.get().defaultBlockState();
        BlockState geyser = TSBlocks.CRYOVOLCANIC_GEYSER.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dx = -baseRadius; dx <= baseRadius; dx++) {
            for (int dz = -baseRadius; dz <= baseRadius; dz++) {
                double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                if (horiz > baseRadius) {
                    continue;
                }
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz) - 1;
                int coneTopY = baseY + (int) ((1.0D - horiz / baseRadius) * height);
                if (coneTopY <= terrainTop) {
                    continue;
                }
                for (int y = terrainTop + 1; y <= coneTopY; y++) {
                    setBlock(level, pos.set(wx, y, wz), ice);
                    placed = true;
                }
                if (horiz < calderaR) {
                    for (int y = calderaFloorY + 1; y <= coneTopY; y++) {
                        setBlock(level, pos.set(wx, y, wz), air);
                    }
                    setBlock(level, pos.set(wx, calderaFloorY, wz), ammonia);
                    level.scheduleTick(pos.immutable(), ammonia.getFluidState().getType(), 5);
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            double ang = random.nextDouble() * Math.PI * 2.0D;
            double rr = baseRadius * (0.45D + random.nextDouble() * 0.35D);
            int gx = origin.getX() + (int) Math.round(Math.cos(ang) * rr);
            int gz = origin.getZ() + (int) Math.round(Math.sin(ang) * rr);
            int gTop = baseY + (int) ((1.0D - rr / baseRadius) * height);
            setBlock(level, pos.set(gx, gTop, gz), geyser);
        }
        return placed;
    }
}
