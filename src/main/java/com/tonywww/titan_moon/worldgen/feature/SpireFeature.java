package com.tonywww.titan_moon.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/**
 * 尖塔/石林特征（装饰）：以 origin 为基座向上堆一根逐层收窄的锥形柱
 * （eroded hoodoo / 冰刺 / 托林风柱 / 寒冰尖峰 / 风化石林）。
 *
 * <p>放置的方块由 {@link BlockStateConfiguration} 指定，供多群系用不同材质复用同一逻辑。
 * 仅覆盖空气，不削平既有地形；基座须落在实心地面上。
 */
public class SpireFeature extends Feature<BlockStateConfiguration> {

    public SpireFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BlockState state = context.config().state;

        // 基座须踩在实心地面上（origin 下方非空气非流体），origin 处须可被替换（空气或流体）——甲烷海中亦可生长
        BlockState below = level.getBlockState(origin.below());
        if (below.isAir() || !below.getFluidState().isEmpty() || !isReplaceable(level.getBlockState(origin))) {
            return false;
        }

        int height = 5 + random.nextInt(8);          // 5-12 高
        int baseRadius = 1 + random.nextInt(2);      // 1-2 基半径
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        for (int dy = 0; dy < height; dy++) {
            double t = 1.0 - (double) dy / height;    // 逐层收窄，顶部半径趋 0
            int radius = (int) Math.round(baseRadius * t);
            int wy = origin.getY() + dy;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius + 1) {
                        continue;
                    }
                    pos.set(origin.getX() + dx, wy, origin.getZ() + dz);
                    if (isReplaceable(level.getBlockState(pos))) {
                        setBlock(level, pos, state);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    /** 可被尖塔替换的方块：空气或流体（使尖塔能在甲烷海等液体中从海底生长而非浮于液面）。 */
    private static boolean isReplaceable(BlockState s) {
        return s.isAir() || !s.getFluidState().isEmpty();
    }
}
