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
 * 甲烷海特征：液态甲烷深渊大面积地表塌陷至液面以下、被液态甲烷淹没，形成开阔的甲烷之海。
 *
 * <p>大半径碗形下沉（中心深、边缘浅）；下沉区上方清空塌陷地表、液面及以下填液态甲烷。
 */
public class MethaneMareFeature extends Feature<NoneFeatureConfiguration> {

    public MethaneMareFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 半径上限 16：feature 写入边界≈以装饰区块为中心的 3x3(Chebyshev≤1)，radius≤16 时任意 origin 局部坐标
        // 都不会写到 2 个区块外，杜绝 1.21 的 "Detected setBlock in a far chunk" 截断/刷屏。甲烷海主体已由地形层
        // (blend_cont base 落液面下 + default_fluid)生成，mare 仅作局部加深点缀。
        int radius = 12 + random.nextInt(5);           // 半径 12-16（原 12-20，超 16 会越界写）
        int sinkDepth = 5 + random.nextInt(4);         // 中心下沉深度 5-8
        int clearAbove = 4;                            // 上方清空高度

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();
        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                if (horiz > radius) {
                    continue;
                }
                double edge = 1.0D - horiz / radius;                  // 中心 1 → 边缘 0
                int localSink = Math.max(1, (int) (sinkDepth * edge));
                int wx = origin.getX() + dx;
                int wz = origin.getZ() + dz;
                // 嵌入地形深处：不清空上方地表（避免高空开口/液体外溢），仅把实心替换为液态甲烷
                for (int dy = 0; dy > -localSink; dy--) {
                    int y = origin.getY() + dy;
                    if (y < minY + 5) {
                        break;                                   // 基岩守卫：不下探到基岩层(Y 0-4)
                    }
                    pos.set(wx, y, wz);
                    BlockState existing = level.getBlockState(pos);
                    if (!existing.isAir() && !existing.is(Blocks.BEDROCK)) {
                        setBlock(level, pos, methane);
                        level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }
}
