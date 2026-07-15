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
 * 丝网谷（宏伟地物）：托林沙海一处挂满丝网的洼地——地表铺 {@code tholin_mycelium} 菌网、
 * 近地空中垂挂蛛丝（{@code cobweb}），营造托林织体蛛群落的伏击场（织体蛛本就在沙海生成，此处为其聚居标志）。
 */
public class SilkShroudFeature extends Feature<NoneFeatureConfiguration> {

    public SilkShroudFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int radius = 8 + random.nextInt(5);            // 8-12
        int count = 36 + random.nextInt(28);
        BlockState web = Blocks.COBWEB.defaultBlockState();
        BlockState myc = TSBlocks.THOLIN_MYCELIUM.get().defaultBlockState();
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
            // 地表菌网
            pos.set(wx, terrainTop, wz);
            if (!level.getBlockState(pos).isAir()) {
                setBlock(level, pos, myc);
                placed = true;
            }
            // 近地悬挂蛛丝
            int wy = terrainTop + 1 + random.nextInt(3);
            pos.set(wx, wy, wz);
            if (level.getBlockState(pos).isAir()) {
                setBlock(level, pos, web);
                placed = true;
            }
        }
        return placed;
    }
}
