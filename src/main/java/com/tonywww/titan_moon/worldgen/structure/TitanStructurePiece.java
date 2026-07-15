package com.tonywww.titan_moon.worldgen.structure;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMBlocks;
import com.tonywww.titan_moon.registry.TMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
//? if neoforge {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
*///?}
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 土卫六结构块：按 {@link TitanStructure.Variant} 程序化建造托林晶洞 / 先驱前哨站。
 * 所有放置都用 {@link BoundingBox#isInside(net.minecraft.core.Vec3i)} 门控，故跨区块生成时
 * 每个区块只落自己那部分（{@code postProcess} 逐区块调用），整座结构正确拼合。
 */
@SuppressWarnings("deprecation")
public class TitanStructurePiece extends StructurePiece {

    //? if forge {
    private static final ResourceLocation GEODE_LOOT = TitanMoon.rl("chests/tholin_geode");
    private static final ResourceLocation OUTPOST_LOOT = TitanMoon.rl("chests/pioneer_outpost");
    //?} else {
    /*private static final ResourceKey<LootTable> GEODE_LOOT = ResourceKey.create(Registries.LOOT_TABLE, TitanMoon.rl("chests/tholin_geode"));
    private static final ResourceKey<LootTable> OUTPOST_LOOT = ResourceKey.create(Registries.LOOT_TABLE, TitanMoon.rl("chests/pioneer_outpost"));
    *///?}

    private final TitanStructure.Variant variant;
    private BlockPos origin;

    public TitanStructurePiece(BlockPos origin, TitanStructure.Variant variant) {
        super(TMStructures.TITAN_PIECE.get(), 0, makeBox(origin, variant));
        this.origin = origin;
        this.variant = variant;
    }

    public TitanStructurePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(TMStructures.TITAN_PIECE.get(), tag);
        this.variant = TitanStructure.Variant.valueOf(tag.getString("Variant"));
        this.origin = new BlockPos(tag.getInt("OX"), tag.getInt("OY"), tag.getInt("OZ"));
    }

    private static BoundingBox makeBox(BlockPos o, TitanStructure.Variant v) {
        return switch (v) {
            case GEODE -> new BoundingBox(o.getX() - 6, o.getY() - 6, o.getZ() - 6, o.getX() + 6, o.getY() + 6, o.getZ() + 6);
            case GREAT_HIVE -> new BoundingBox(o.getX() - 9, o.getY() - 9, o.getZ() - 9, o.getX() + 9, o.getY() + 9, o.getZ() + 9);
            case DERRICK -> new BoundingBox(o.getX() - 5, o.getY() - 8, o.getZ() - 5, o.getX() + 5, o.getY() + 14, o.getZ() + 5);
            case CRASHED_PROBE -> new BoundingBox(o.getX() - 4, o.getY() - 3, o.getZ() - 4, o.getX() + 4, o.getY() + 4, o.getZ() + 4);
            case BEACON -> new BoundingBox(o.getX() - 6, o.getY() - 2, o.getZ() - 6, o.getX() + 6, o.getY() + 16, o.getZ() + 6);
            default -> new BoundingBox(o.getX() - 4, o.getY() - 2, o.getZ() - 4, o.getX() + 4, o.getY() + 5, o.getZ() + 4);
        };
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putString("Variant", variant.name());
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        BoundingBox useBox = box;
        // 地表建筑：findGenerationPoint 只能用 noise 估算高度（结构定位早于地形 feature），会被
        // local_modifications 地形改造（陨石坑等）+ 估算精度偏差顶到半空。此处已是 surface_structures 步、
        // 该区块地形（含地形改造）已完全生成，直接向下扫描该列真实实心地表把整座建筑重锚下去，消除悬浮。
        // 用扫描而非 level.getHeight(*_WG)：_WG heightmap 在已加载区块（/place）未 prime 会报错；直接读方块两处都稳。
        // 地表建筑均在单区块内（box ≤13 宽、origin 居区块中心），origin 列恒在当前区块内可读。
        if (variant != TitanStructure.Variant.GEODE && variant != TitanStructure.Variant.GREAT_HIVE) {
            int actualTop = findSolidTop(level, origin.getX(), origin.getZ(), origin.getY());
            if (actualTop != Integer.MIN_VALUE && actualTop != origin.getY()) {
                this.origin = new BlockPos(origin.getX(), actualTop, origin.getZ());
                useBox = makeBox(this.origin, variant);
            }
        }
        switch (variant) {
            case GEODE -> buildGeode(level, box, random);
            case GREAT_HIVE -> buildGreatHive(level, box, random);
            case DERRICK -> buildDerrick(level, useBox, random);
            case CRASHED_PROBE -> buildCrashedProbe(level, useBox, random);
            case BEACON -> buildBeacon(level, useBox, random);
            default -> buildOutpost(level, useBox, random);
        }
    }

    /** 从估算高度上方向下扫描，返回该列最高实心（非空气非流体）方块 Y——即真实地表顶，
     *  含 local_modifications 的挖/填改造。不依赖 heightmap 是否 prime，worldgen 与 /place 均可靠。
     *  找不到（越出底部）返回 {@link Integer#MIN_VALUE}。 */
    private static int findSolidTop(WorldGenLevel level, int x, int z, int estimateY) {
        BlockPos.MutableBlockPos mp = new BlockPos.MutableBlockPos();
        int top = estimateY + 32;
        int bottom = level.getMinBuildHeight();
        for (int y = top; y > bottom; y--) {
            mp.set(x, y, z);
            BlockState s = level.getBlockState(mp);
            if (!s.isAir() && s.getFluidState().isEmpty()) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }

    // ---- 托林晶洞：地下中空晶球 ----
    private void buildGeode(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int r = 5;
        BlockState shell = TMBlocks.CRYO_ICE.get().defaultBlockState();
        BlockState crystal = TMBlocks.THOLIN_CRYSTAL.get().defaultBlockState();
        BlockState hollow = Blocks.CAVE_AIR.defaultBlockState();
        BlockState floor = TMBlocks.HARDENED_THOLIN.get().defaultBlockState(); // 生物有机壁/巢底
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > r + 0.5) {
                        continue;
                    }
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (dist > r - 0.7) {
                        put(level, shell, m, box);
                    } else if (dist > r - 1.7) {
                        put(level, crystal, m, box);
                    } else {
                        put(level, hollow, m, box);
                    }
                }
            }
        }

        int floorY = origin.getY() - (r - 2);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 6) {
                    put(level, floor, m.set(origin.getX() + dx, floorY, origin.getZ() + dz), box);
                }
            }
        }
        placeChest(level, new BlockPos(origin.getX(), floorY + 1, origin.getZ()), box, random, GEODE_LOOT);
        spawnIceWorm(level, new BlockPos(origin.getX() + 1, floorY + 1, origin.getZ()), box, random);
    }

    // ---- 先驱前哨站：地表废弃小屋 ----
    private void buildOutpost(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int half = 3;
        BlockState wall = TMBlocks.TITAN_STONE.get().defaultBlockState();
        BlockState base = TMBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fy = origin.getY();

        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, base, m.set(origin.getX() + dx, fy, origin.getZ() + dz), box);
                put(level, base, m.set(origin.getX() + dx, fy - 1, origin.getZ() + dz), box);
            }
        }
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -half + 1; dx <= half - 1; dx++) {
                for (int dz = -half + 1; dz <= half - 1; dz++) {
                    put(level, air, m.set(origin.getX() + dx, fy + wy, origin.getZ() + dz), box);
                }
            }
        }
        // 墙体（南侧留门洞：dz=+half, dx=0, wy 1..2）
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    if (Math.abs(dx) != half && Math.abs(dz) != half) {
                        continue;
                    }
                    if (dz == half && dx == 0 && wy <= 2) {
                        continue;
                    }
                    put(level, wall, m.set(origin.getX() + dx, fy + wy, origin.getZ() + dz), box);
                }
            }
        }
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, wall, m.set(origin.getX() + dx, fy + 4, origin.getZ() + dz), box);
            }
        }
        placeChest(level, new BlockPos(origin.getX() - 1, fy + 1, origin.getZ() - 1), box, random, OUTPOST_LOOT);
        spawnProbe(level, new BlockPos(origin.getX() + 1, fy + 1, origin.getZ() + 1), box, random);
    }

    // ---- 深钻者：半沉甲烷海的工业钻井平台 ----
    private void buildDerrick(WorldGenLevel level, BoundingBox box, RandomSource random) {
        BlockState frame = TMBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState deck = TMBlocks.WEATHERED_TITAN_STONE.get().defaultBlockState();
        BlockState core = TMBlocks.METHANE_POOL_CORE.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fy = origin.getY();
        int half = 3;
        int deckY = fy + 6;
        int[][] corners = {{-half, -half}, {half, -half}, {-half, half}, {half, half}};
        for (int[] c : corners) {
            for (int y = deckY; y >= fy - 6; y--) {
                put(level, frame, m.set(origin.getX() + c[0], y, origin.getZ() + c[1]), box);
            }
        }
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, deck, m.set(origin.getX() + dx, deckY, origin.getZ() + dz), box);
            }
        }
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    boolean edge = Math.abs(dx) == half || Math.abs(dz) == half;
                    m.set(origin.getX() + dx, deckY + wy, origin.getZ() + dz);
                    if (edge) {
                        if (!(dz == half && dx == 0 && wy <= 2)) {
                            put(level, frame, m, box);
                        }
                    } else {
                        put(level, air, m, box);
                    }
                }
            }
        }
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                put(level, deck, m.set(origin.getX() + dx, deckY + 4, origin.getZ() + dz), box);
            }
        }
        for (int y = deckY + 5; y <= deckY + 8; y++) {
            put(level, frame, m.set(origin.getX(), y, origin.getZ()), box);
        }
        put(level, core, m.set(origin.getX(), fy - 6, origin.getZ()), box);
        placeChest(level, new BlockPos(origin.getX() - 1, deckY + 1, origin.getZ() - 1), box, random, OUTPOST_LOOT);
        spawnProbe(level, new BlockPos(origin.getX() + 1, deckY + 1, origin.getZ() + 1), box, random);
    }

    // ---- 坠毁研究探测器残骸 ----
    private void buildCrashedProbe(WorldGenLevel level, BoundingBox box, RandomSource random) {
        BlockState debris = TMBlocks.TITAN_BASALT.get().defaultBlockState();
        BlockState metal = TMBlocks.METEOR_FRAGMENT.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fy = origin.getY();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz <= 5) {
                    put(level, debris, m.set(origin.getX() + dx, fy - 1, origin.getZ() + dz), box);
                    if (random.nextInt(3) == 0) {
                        put(level, metal, m.set(origin.getX() + dx, fy, origin.getZ() + dz), box);
                    }
                }
            }
        }
        put(level, metal, m.set(origin.getX(), fy, origin.getZ()), box);
        put(level, metal, m.set(origin.getX(), fy + 1, origin.getZ()), box);
        put(level, metal, m.set(origin.getX() + 1, fy, origin.getZ()), box);
        placeChest(level, new BlockPos(origin.getX() + 1, fy, origin.getZ() + 1), box, random, OUTPOST_LOOT);
        spawnProbe(level, new BlockPos(origin.getX() - 1, fy, origin.getZ() - 1), box, random);
    }

    // ---- 大巢：极地多腔体冰虫 Boss 地牢（扩展 tholin_geode）----
    private void buildGreatHive(WorldGenLevel level, BoundingBox box, RandomSource random) {
        int r = 8;
        BlockState shell = TMBlocks.THOLIN_MYCELIUM.get().defaultBlockState();
        BlockState crystal = TMBlocks.THOLIN_CRYSTAL.get().defaultBlockState();
        BlockState hollow = Blocks.CAVE_AIR.defaultBlockState();
        BlockState floor = TMBlocks.HARDENED_THOLIN.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > r + 0.5) {
                        continue;
                    }
                    m.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    if (dist > r - 0.9) {
                        put(level, shell, m, box);
                    } else if (dist > r - 1.9) {
                        put(level, random.nextInt(3) == 0 ? crystal : shell, m, box);
                    } else {
                        put(level, hollow, m, box);
                    }
                }
            }
        }
        int floorY = origin.getY() - (r - 2);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz <= 9) {
                    put(level, floor, m.set(origin.getX() + dx, floorY, origin.getZ() + dz), box);
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            int ox = random.nextInt(9) - 4;
            int oz = random.nextInt(9) - 4;
            if (ox * ox + oz * oz <= 12) {
                put(level, crystal, m.set(origin.getX() + ox, floorY + 1, origin.getZ() + oz), box);
            }
        }
        placeChest(level, new BlockPos(origin.getX(), floorY + 1, origin.getZ()), box, random, GEODE_LOOT);
        spawnIceWorm(level, new BlockPos(origin.getX() + 2, floorY + 1, origin.getZ()), box, random);
    }

    // ---- 深空信标阵：荒原废弃射电天线阵 ----
    private void buildBeacon(WorldGenLevel level, BoundingBox box, RandomSource random) {
        BlockState platform = TMBlocks.WEATHERED_TITAN_STONE.get().defaultBlockState();
        BlockState mast = TMBlocks.TITAN_STONE.get().defaultBlockState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int fy = origin.getY();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                put(level, platform, m.set(origin.getX() + dx, fy, origin.getZ() + dz), box);
                put(level, platform, m.set(origin.getX() + dx, fy - 1, origin.getZ() + dz), box);
            }
        }
        int topY = fy + 12;
        for (int y = fy + 1; y <= topY; y++) {
            put(level, mast, m.set(origin.getX(), y, origin.getZ()), box);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz <= 9) {
                    put(level, platform, m.set(origin.getX() + dx, topY, origin.getZ() + dz), box);
                }
            }
        }
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            for (int k = 1; k <= 4; k++) {
                put(level, mast, m.set(origin.getX() + c[0] * k / 4, fy + k * 2, origin.getZ() + c[1] * k / 4), box);
            }
        }
        placeChest(level, new BlockPos(origin.getX() + 2, fy + 1, origin.getZ()), box, random, OUTPOST_LOOT);
        spawnProbe(level, new BlockPos(origin.getX() - 2, fy + 1, origin.getZ() + 2), box, random);
        spawnProbe(level, new BlockPos(origin.getX() + 2, fy + 1, origin.getZ() - 2), box, random);
    }

    private void put(WorldGenLevel level, BlockState state, BlockPos pos, BoundingBox box) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    //? if forge {
    private void placeChest(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random, ResourceLocation loot) {
    //?} else {
    /*private void placeChest(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random, ResourceKey<LootTable> loot) {
    *///?}
        if (!box.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(loot, random.nextLong());
        }
    }

    private void spawnProbe(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random) {
        if (!box.isInside(pos)) {
            return;
        }
        Mob probe = TMEntities.CORRUPTED_PROBE.get().create(level.getLevel());
        if (probe != null) {
            probe.setPersistenceRequired();
            probe.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            //? if forge {
            probe.finalizeSpawn(level, level.getLevel().getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            //?} else {
            /*probe.finalizeSpawn(level, new net.minecraft.world.DifficultyInstance(level.getLevel().getDifficulty(), level.getLevel().getDayTime(), 0L, 0.0F), MobSpawnType.STRUCTURE, null);
            *///?}
            level.addFreshEntity(probe);
        }
    }

    /** 冰虫巢穴 Boss：在托林晶洞中央生成一只常驻的原生冰虫精英。 */
    private void spawnIceWorm(WorldGenLevel level, BlockPos pos, BoundingBox box, RandomSource random) {
        if (!box.isInside(pos)) {
            return;
        }
        Mob worm = TMEntities.NATIVE_ICE_WORM.get().create(level.getLevel());
        if (worm != null) {
            worm.setPersistenceRequired();
            worm.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
            //? if forge {
            worm.finalizeSpawn(level, level.getLevel().getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            //?} else {
            /*worm.finalizeSpawn(level, new net.minecraft.world.DifficultyInstance(level.getLevel().getDifficulty(), level.getLevel().getDayTime(), 0L, 0.0F), MobSpawnType.STRUCTURE, null);
            *///?}
            level.addFreshEntity(worm);
        }
    }
}
