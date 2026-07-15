package com.tonywww.titan_moon.worldgen.feature;

import com.mojang.serialization.Codec;
import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 破碎海绵特征：极地迷宫冰原地表下数格用 3D 值噪声挖出密布空洞的多孔冰体（破碎海绵）。
 *
 * <p>仅在噪声高值处挖空（保留原有冰壁），配合边缘衰减使中心多孔、边缘保留壁，形成海绵状孔隙。
 */
public class SpongeCaveFeature extends Feature<NoneFeatureConfiguration> {

    /** 极地迷宫冰原群系键——破碎海绵仅在此群系“内部”生成。 */
    private static final ResourceKey<Biome> POLAR_LABYRINTH =
            ResourceKey.create(Registries.BIOME, TitanMoon.rl("polar_labyrinth"));
    /** 洞体半径之外再向外要求的实心边距（格）：中心与该半径环上须全为极地群系，否则跳过。
     *  取值偏大以抵消群系采样按 4 格量化的误差、并为过渡断崖留足实心冰壁。 */
    private static final int EDGE_BUFFER = 8;
    /** 环形采样点数（越多越不易漏检切向断崖）。 */
    private static final int RING_SAMPLES = 16;
    /** 允许的最大地表落差（格）：一圈地表比中心低超过此值即判定邻近过渡断崖 / 陡坡，跳过本次生成。 */
    private static final int MAX_DROP = 8;
    /** 最低生成高度（格）：地表低于此高度即视为已跌入向低群系过渡的坡 / 崖上，跳过。
     *  极地台面基准 Y≈180（180 + 4*wobble）；调高→更保守（仅台面顶部生成），调低→更宽松。 */
    private static final int MIN_SURFACE_Y = 172;

    public SpongeCaveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 最低高度闸门：地表明显低于基准台面，说明处在向低群系过渡的坡 / 崖上，直接跳过。
        if (origin.getY() < MIN_SURFACE_Y) {
            return false;
        }

        int radius = 5 + random.nextInt(4);            // 半径 5-8
        int height = 8 + random.nextInt(8);            // 高度 8-15
        int topOffset = 2 + random.nextInt(2);         // 地表下 2-3 格起
        double threshold = 0.12D;                      // 孔隙阈值（越低孔越多）

        // 群系边缘 + 地形落差双重剔除：因极端高差，群系过渡处是陡峭断崖，海绵孔洞极易被切穿暴露。
        // 要求洞体中心及其“半径 + 边距”环上一圈：既全在极地群系内、地表又无明显下陷，否则放弃本次生成。
        if (!isSafeInterior(level, origin, radius + EDGE_BUFFER)) {
            return false;
        }

        BlockState air = Blocks.CAVE_AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        int minY = level.getMinBuildHeight();

        for (int dy = -topOffset; dy > -topOffset - height; dy--) {
            if (origin.getY() + dy < minY + 5) {
                break;                                 // 基岩守卫：不凿穿 Y0-4 基岩带
            }
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double horiz = Math.sqrt((double) (dx * dx + dz * dz));
                    if (horiz > radius) {
                        continue;
                    }
                    int wx = origin.getX() + dx;
                    int wy = origin.getY() + dy;
                    int wz = origin.getZ() + dz;
                    // 3D 噪声 + 边缘衰减：中心多孔、边缘保留冰壁
                    double n = noise3d(wx * 0.18D, wy * 0.18D, wz * 0.18D);
                    double falloff = 1.0D - horiz / radius;
                    if (n * falloff > threshold) {
                        pos.set(wx, wy, wz);
                        if (!level.getBlockState(pos).isAir()) {
                            setBlock(level, pos, air);
                            placed = true;
                        }
                    }
                }
            }
        }
        return placed;
    }

    /**
     * 洞体是否位于极地迷宫冰原“平坦内部”：中心在极地群系内，且以 r 为半径、{@link #RING_SAMPLES}
     * 个方向的一圈上——(1) 全部仍是极地群系；(2) 地表相对中心的下陷不超过 {@link #MAX_DROP}。
     * 任一不满足即视为邻近群系过渡断崖 / 陡坡，放弃生成。
     */
    private static boolean isSafeInterior(WorldGenLevel level, BlockPos origin, int r) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();
        if (!isPolar(level, cx, cy, cz)) {
            return false;
        }
        int centerSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cx, cz);
        for (int i = 0; i < RING_SAMPLES; i++) {
            double a = (Math.PI * 2.0D * i) / RING_SAMPLES;
            int sx = cx + (int) Math.round(Math.cos(a) * r);
            int sz = cz + (int) Math.round(Math.sin(a) * r);
            if (!isPolar(level, sx, cy, sz)) {
                return false;
            }
            if (centerSurface - level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz) > MAX_DROP) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPolar(WorldGenLevel level, int x, int y, int z) {
        //? if forge {
        return level.getBiome(new BlockPos(x, y, z)).is(POLAR_LABYRINTH);
        //?} else {
        /*// 参考原版 LevelReader#getNoiseBiome：特征生成期用 require=false 的 getChunk 取块，
        // 越出 WorldGenRegion（以中心块 Chebyshev 距离为界）时返回 null → 视为边缘并跳过；
        // 再直接从该块读 noise biome，避开 level.getBiome 的生物群系模糊缩放跨块访问——那正是
        // 越界读抛 "Requested chunk unavailable during world generation" 的根因（hasChunk 只查
        // 边界、挡不住缩放偏移到的相邻块）。isSafeInterior 里每个 getHeight 前都先 isPolar 同点，
        // 故此处返回 false / 保证块可用即同时护住后续 getHeight。
        net.minecraft.world.level.chunk.ChunkAccess chunk = level.getChunk(
                x >> 4, z >> 4, net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES, false);
        if (chunk == null) {
            return false;
        }
        return chunk.getNoiseBiome(net.minecraft.core.QuartPos.fromBlock(x),
                net.minecraft.core.QuartPos.fromBlock(y),
                net.minecraft.core.QuartPos.fromBlock(z)).is(POLAR_LABYRINTH);
        *///?}
    }

    // 确定性 3D 值噪声：整点哈希 + 三线性平滑插值，输出约 [0, 1]。
    private static double noise3d(double x, double y, double z) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        double fx = smooth(x - x0);
        double fy = smooth(y - y0);
        double fz = smooth(z - z0);
        double c000 = hash(x0, y0, z0);
        double c100 = hash(x0 + 1, y0, z0);
        double c010 = hash(x0, y0 + 1, z0);
        double c110 = hash(x0 + 1, y0 + 1, z0);
        double c001 = hash(x0, y0, z0 + 1);
        double c101 = hash(x0 + 1, y0, z0 + 1);
        double c011 = hash(x0, y0 + 1, z0 + 1);
        double c111 = hash(x0 + 1, y0 + 1, z0 + 1);
        double x00 = Mth.lerp(fx, c000, c100);
        double x10 = Mth.lerp(fx, c010, c110);
        double x01 = Mth.lerp(fx, c001, c101);
        double x11 = Mth.lerp(fx, c011, c111);
        double y0v = Mth.lerp(fy, x00, x10);
        double y1v = Mth.lerp(fy, x01, x11);
        return Mth.lerp(fz, y0v, y1v);
    }

    private static double smooth(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static double hash(int x, int y, int z) {
        int h = x * 374761393 + y * 668265263 + z * 1274126177;
        h = (h ^ (h >> 13)) * 1274126177;
        h = h ^ (h >> 16);
        return (h & 0xFFFF) / 65535.0D;
    }
}
