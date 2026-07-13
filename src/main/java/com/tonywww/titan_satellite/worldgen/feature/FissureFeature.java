package com.tonywww.titan_satellite.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 碎裂裂隙特征（PG-4）：液态甲烷深渊地表的细长深沟（迷你峡谷），底部积存液态甲烷。
 *
 * <p>沿随机水平方向延伸，V 形横截面（中心深、边缘浅），沿程深浅起伏营造碎裂感，底部数格填液态甲烷。
 */
public class FissureFeature extends Feature<NoneFeatureConfiguration> {

    public FissureFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int length = 6 + random.nextInt(6);            // 沟长 6-11（限制在安全写入范围内，避免跨区块）
        int maxHeight = 6 + random.nextInt(6);         // 竖向 6-11
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);

        // 仅允许写入 origin 所在区块的 3x3 邻域，避免世界生成期 setBlock 越界报错
        int minChunkX = (origin.getX() >> 4) - 1;
        int maxChunkX = (origin.getX() >> 4) + 1;
        int minChunkZ = (origin.getZ() >> 4) - 1;
        int maxChunkZ = (origin.getZ() >> 4) + 1;

        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockState methane = TSBlocks.LIQUID_METHANE_BLOCK.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinBuildHeight();                                      // 维度底部（基岩层从这里往上 0-4 格）
        boolean placed = false;

        // 嵌入地形深处：沿水平方向蜒蜒的深埋裂隙——下部积存液态甲烷 + 顶部封闭气腔，仅替换实心（不通地表、不外溢）
        for (int t = 0; t < length; t++) {
            double cx = origin.getX() + dirX * t;
            double cz = origin.getZ() + dirZ * t;
            int halfWidth = random.nextInt(2);                                     // 0-1，窄缝
            int segHeight = maxHeight - (int) (2.0D * Math.abs(Math.sin(t * 0.6D))); // 深浅沿程起伏（碎裂）

            for (int w = -halfWidth; w <= halfWidth; w++) {
                double px = cx - dirZ * w;
                double pz = cz + dirX * w;
                int localHeight = segHeight - Math.abs(w);
                if (localHeight < 2) {
                    continue;
                }
                int wx = Mth.floor(px);
                int wz = Mth.floor(pz);
                int tcx = wx >> 4;
                int tcz = wz >> 4;
                if (tcx < minChunkX || tcx > maxChunkX || tcz < minChunkZ || tcz > maxChunkZ) {
                    continue;                                                      // 越界丢弃，保持在安全写入区域
                }
                // 液体守卫：从裂隙顶往上探——若在遇到实心天花板之前先遇到液体（甲烷海/湖压顶）
                // → 该列被液体覆盖，不在液体之中/之下生成裂隙，整列跳过。
                // （直接读方块而非 _WG heightmap：后者在已加载世界不更新、无法验证）
                boolean submerged = false;
                for (int uy = origin.getY() + 1; uy <= origin.getY() + 8; uy++) {
                    BlockState above = level.getBlockState(pos.set(wx, uy, wz));
                    if (!above.getFluidState().isEmpty()) {
                        submerged = true;
                        break;
                    }
                    if (!above.isAir()) {
                        break;                                                     // 遇到实心天花板（干岩覆盖，非液体）→ 停
                    }
                }
                if (submerged) {
                    continue;
                }
                for (int dy = 0; dy > -localHeight; dy--) {
                    int y = origin.getY() + dy;
                    if (y < minY + 5) {
                        break;                                                     // 基岩守卫：不下探到基岩层(Y 0-4)及以下（dy 只会更低，直接停）
                    }
                    pos.set(wx, y, wz);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir() || !existing.getFluidState().isEmpty() || existing.is(Blocks.BEDROCK)) {
                        continue;                                                  // 只替换实心岩体：跳过空气/既有液体/基岩
                    }
                    if (dy > -2) {
                        setBlock(level, pos, air);       // 顶部封闭气腔
                    } else {
                        setBlock(level, pos, methane);   // 下部液态甲烷
                        level.scheduleTick(pos.immutable(), methane.getFluidState().getType(), 5);
                    }
                    placed = true;
                }
            }
        }
        return placed;
    }
}
