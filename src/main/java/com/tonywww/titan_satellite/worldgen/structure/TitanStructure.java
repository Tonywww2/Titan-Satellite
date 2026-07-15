package com.tonywww.titan_satellite.worldgen.structure;

import com.mojang.serialization.Codec;
//? if neoforge {
/*import com.mojang.serialization.MapCodec;
*///?}
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * 土卫六自定义结构：单一参数化结构，{@link Variant} 决定建造哪种。
 * <ul>
 *   <li>{@link Variant#GEODE} 托林晶洞——地下中空晶球（冷冰外壳 + 托林晶体内衬 + 战利品箱）；</li>
 *   <li>{@link Variant#OUTPOST} 先驱前哨站——地表废弃小屋（战利品箱 + 一台失控探测器）。</li>
 * </ul>
 * 无 NBT 模板，几何由 {@link TitanStructurePiece} 程序化生成。
 */
public class TitanStructure extends Structure {

    //? if forge {
    public static final Codec<TitanStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    settingsCodec(instance),
                    Variant.CODEC.fieldOf("variant").forGetter(s -> s.variant)
            ).apply(instance, TitanStructure::new));
    //?} else {
    /*public static final MapCodec<TitanStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    Variant.CODEC.fieldOf("variant").forGetter(s -> s.variant)
            ).apply(instance, TitanStructure::new));
    *///?}

    private final Variant variant;

    public TitanStructure(Structure.StructureSettings settings, Variant variant) {
        super(settings);
        this.variant = variant;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        // 用 OCEAN_FLOOR_WG（实心顶）而非 WORLD_SURFACE_WG：后者把 default_fluid（液态甲烷）
        // 液面也算作地表，低海拔柱上常覆 1-2 格薄甲烷 → 结构会锚在甲烷面上系统性悬空 1-2 格。
        // 全 mod 的 feature 一律用 OCEAN_FLOOR_WG（实心顶），此处对齐。
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int y;
        if (variant == Variant.GEODE || variant == Variant.GREAT_HIVE) {
            int minY = context.heightAccessor().getMinBuildHeight() + 12;
            int maxY = surfaceY - 12;
            if (maxY <= minY) {
                return Optional.empty();
            }
            y = Mth.randomBetweenInclusive(context.random(), minY, maxY);
        } else {
            y = surfaceY;
        }

        BlockPos origin = new BlockPos(x, y, z);
        Variant v = this.variant;
        return Optional.of(new Structure.GenerationStub(origin,
                builder -> builder.addPiece(new TitanStructurePiece(origin, v))));
    }

    @Override
    public StructureType<?> type() {
        return TSStructures.TITAN_STRUCTURE.get();
    }

    /** 结构变体，写在 {@code worldgen/structure/*.json} 的 {@code "variant"} 字段。 */
    public enum Variant implements StringRepresentable {
        GEODE("geode"),
        OUTPOST("outpost"),
        DERRICK("derrick"),
        CRASHED_PROBE("crashed_probe"),
        GREAT_HIVE("great_hive"),
        BEACON("beacon");

        public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);

        private final String serializedName;

        Variant(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
