package com.tonywww.titan_moon.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_moon.registry.TMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 巨型沙脊特征：托林沙海中高墙般的狭长山脊。
 */
public class MegayardangFeature extends Feature<NoneFeatureConfiguration> {

    public MegayardangFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        boolean alongX = random.nextBoolean();
        int half = 5 + random.nextInt(4);
        int maxHeight = 4 + random.nextInt(4);
        BlockState sand = TMBlocks.THOLIN_SAND.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int l = -half; l <= half; l++) {
            double t = 1.0D - (double) Math.abs(l) / (double) (half + 1);
            int h = Math.max(1, (int) (maxHeight * t));
            for (int w = -1; w <= 1; w++) {
                int dx = alongX ? l : w;
                int dz = alongX ? w : l;
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                // 从该列真实地表往上垒（而非固定 origin.getY()）：沙脊横跨多列、各列地表高低不同，
                // 用 origin 相对高度会在低处半空留下悬浮沙块；贴合各列地表才始终连地。
                int terrainTop = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz) - 1;
                for (int k = 1; k <= h; k++) {
                    pos.set(wx, terrainTop + k, wz);
                    if (level.getBlockState(pos).isAir()) {   // 只在地表之上的空气里垒，不埋入既有实体
                        setBlock(level, pos, sand);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
