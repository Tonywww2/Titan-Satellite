package com.tonywww.titan_moon.registry;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.curio.LifeSupportCurioItem;
import com.tonywww.titan_moon.curio.SlotExpanderCurioItem;
import com.tonywww.titan_moon.curio.ToxinGloveCurioItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
//? if forge {
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
//?} else {
/*import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

/**
 * 物品注册表：材料物品、固体方块的 BlockItem、流体桶。
 */
public final class TMItems {

    private TMItems() {
    }

    public static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(Registries.ITEM, TitanMoon.MODID);

    // ---- 材料物品 ----
    // 浮游薄膜：甲烷浮游体掉落的轻量材料（占位，复用原版幻翼膜贴图）。
    public static final Supplier<Item> AERO_MEMBRANE = mat("aero_membrane", Rarity.COMMON);

    // ---- 方块物品（与 TMBlocks 一一对应，流体方块除外）----
    public static final Supplier<Item> TITAN_STONE = blockItem("titan_stone", TMBlocks.TITAN_STONE);
    public static final Supplier<Item> TITAN_BASALT = blockItem("titan_basalt", TMBlocks.TITAN_BASALT);
    public static final Supplier<Item> THOLIN_SAND = blockItem("tholin_sand", TMBlocks.THOLIN_SAND);
    public static final Supplier<Item> CRUSHED_ICE = blockItem("crushed_ice", TMBlocks.CRUSHED_ICE);
    public static final Supplier<Item> CRYO_ICE = blockItem("cryo_ice", TMBlocks.CRYO_ICE);
    public static final Supplier<Item> PACKED_METHANE_ICE = blockItem("packed_methane_ice", TMBlocks.PACKED_METHANE_ICE);
    public static final Supplier<Item> CRYOVOLCANIC_GEYSER = blockItem("cryovolcanic_geyser", TMBlocks.CRYOVOLCANIC_GEYSER);
    public static final Supplier<Item> METHANE_POOL_CORE = blockItem("methane_pool_core", TMBlocks.METHANE_POOL_CORE);
    public static final Supplier<Item> SPECIAL_METHANE_PUMP = blockItem("special_methane_pump", TMBlocks.SPECIAL_METHANE_PUMP);
    public static final Supplier<Item> THOLIN_CRYSTAL = blockItem("tholin_crystal", TMBlocks.THOLIN_CRYSTAL);

    // ---- 群系特色化新增方块物品 ----
    public static final Supplier<Item> WEATHERED_TITAN_STONE = blockItem("weathered_titan_stone", TMBlocks.WEATHERED_TITAN_STONE);
    public static final Supplier<Item> SEDIMENTARY_TITAN_STONE = blockItem("sedimentary_titan_stone", TMBlocks.SEDIMENTARY_TITAN_STONE);
    public static final Supplier<Item> BRANCH_CRYSTAL = blockItem("branch_crystal", TMBlocks.BRANCH_CRYSTAL);

    // ---- 装饰地物新增方块物品 ----
    public static final Supplier<Item> ABYSS_CRYSTAL = blockItem("abyss_crystal", TMBlocks.ABYSS_CRYSTAL);
    public static final Supplier<Item> THOLIN_TAR = blockItem("tholin_tar", TMBlocks.THOLIN_TAR);
    public static final Supplier<Item> METEOR_FRAGMENT = blockItem("meteor_fragment", TMBlocks.METEOR_FRAGMENT);
    public static final Supplier<Item> HARDENED_THOLIN = blockItem("hardened_tholin", TMBlocks.HARDENED_THOLIN);
    public static final Supplier<Item> THOLIN_SHRUB = blockItem("tholin_shrub", TMBlocks.THOLIN_SHRUB);
    public static final Supplier<Item> METHANE_ICE_BLOOM = blockItem("methane_ice_bloom", TMBlocks.METHANE_ICE_BLOOM);
    public static final Supplier<Item> AMMONIA_CRYSTAL = blockItem("ammonia_crystal", TMBlocks.AMMONIA_CRYSTAL);
    public static final Supplier<Item> TITAN_GRAVEL = blockItem("titan_gravel", TMBlocks.TITAN_GRAVEL);
    public static final Supplier<Item> FROST_BUSH = blockItem("frost_bush", TMBlocks.FROST_BUSH);

    // ---- 宏伟地物构成方块物品 ----
    public static final Supplier<Item> ACETYLENE_SPIRE = blockItem("acetylene_spire", TMBlocks.ACETYLENE_SPIRE);
    public static final Supplier<Item> THOLIN_MYCELIUM = blockItem("tholin_mycelium", TMBlocks.THOLIN_MYCELIUM);
    public static final Supplier<Item> HYDROGEN_BUBBLE_MAT = blockItem("hydrogen_bubble_mat", TMBlocks.HYDROGEN_BUBBLE_MAT);

    // ---- 材料加工链被动生产方块物品 ----
    public static final Supplier<Item> HYDROGEN_COLLECTOR = blockItem("hydrogen_collector", TMBlocks.HYDROGEN_COLLECTOR);
    public static final Supplier<Item> THOLIN_COMPOSTER = blockItem("tholin_composter", TMBlocks.THOLIN_COMPOSTER);

    // ---- 流体桶 ----
    //? if forge {
    public static final Supplier<Item> LIQUID_METHANE_BUCKET = register("liquid_methane_bucket",
            () -> new BucketItem(TMFluids.LIQUID_METHANE, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final Supplier<Item> LIQUID_AMMONIA_BUCKET = register("liquid_ammonia_bucket",
            () -> new BucketItem(TMFluids.LIQUID_AMMONIA, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final Supplier<Item> LIQUID_HYDROGEN_BUCKET = register("liquid_hydrogen_bucket",
            () -> new BucketItem(TMFluids.LIQUID_HYDROGEN, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    //?} else {
    /*public static final Supplier<Item> LIQUID_METHANE_BUCKET = register("liquid_methane_bucket",
            () -> new BucketItem(TMFluids.LIQUID_METHANE.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final Supplier<Item> LIQUID_AMMONIA_BUCKET = register("liquid_ammonia_bucket",
            () -> new BucketItem(TMFluids.LIQUID_AMMONIA.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final Supplier<Item> LIQUID_HYDROGEN_BUCKET = register("liquid_hydrogen_bucket",
            () -> new BucketItem(TMFluids.LIQUID_HYDROGEN.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    *///?}

    // ---- 生物掉落材料（稀有度按获取复杂度）----
    public static final Supplier<Item> CRYO_CARAPACE = mat("cryo_carapace", Rarity.COMMON);
    public static final Supplier<Item> TOXIC_GLAND = mat("toxic_gland", Rarity.COMMON);
    public static final Supplier<Item> DEPLETED_BATTERY = mat("depleted_battery", Rarity.UNCOMMON);
    public static final Supplier<Item> PRECISION_COMPONENTS = mat("precision_components", Rarity.UNCOMMON);

    // ---- 生态深化掉落材料（稀有度按获取复杂度）----
    public static final Supplier<Item> CRYSTALLINE_TWIG = mat("crystalline_twig", Rarity.COMMON);
    public static final Supplier<Item> THOLIN_FIBRE = mat("tholin_fibre", Rarity.COMMON);
    public static final Supplier<Item> TOUGH_NEURAL_GLAND = mat("tough_neural_gland", Rarity.UNCOMMON);
    public static final Supplier<Item> THOLIN_SILK_SAC = mat("tholin_silk_sac", Rarity.UNCOMMON);

    // ---- 材料加工链（稀有度按获取复杂度：直采→COMMON，1 步→UNCOMMON，多步组合/深层→RARE，终局→EPIC）----
    // 中间体
    public static final Supplier<Item> THOLIN_DUST = mat("tholin_dust", Rarity.UNCOMMON);
    public static final Supplier<Item> CONDENSED_ACETYLENE = mat("condensed_acetylene", Rarity.UNCOMMON);
    public static final Supplier<Item> HYDROGEN_CAPSULE = mat("hydrogen_capsule", Rarity.COMMON);
    public static final Supplier<Item> METEORIC_IRON_INGOT = mat("meteoric_iron_ingot", Rarity.UNCOMMON);
    public static final Supplier<Item> SILICON_DUST = mat("silicon_dust", Rarity.UNCOMMON);
    public static final Supplier<Item> METHANE_ICE_SHARD = mat("methane_ice_shard", Rarity.COMMON);
    // 三试剂（晶体粉碎）
    public static final Supplier<Item> ABYSS_CRYSTAL_DUST = mat("abyss_crystal_dust", Rarity.RARE);
    public static final Supplier<Item> AMMONIA_SALT = mat("ammonia_salt", Rarity.UNCOMMON);
    public static final Supplier<Item> THOLIN_CRYSTAL_DUST = mat("tholin_crystal_dust", Rarity.UNCOMMON);
    // 高级产物
    public static final Supplier<Item> POLYPHOSPHAZENE_COENZYME = mat("polyphosphazene_coenzyme", Rarity.RARE);
    public static final Supplier<Item> AZOTOSOME_SHEET = mat("azotosome_sheet", Rarity.RARE);
    public static final Supplier<Item> CRYO_ALLOY_INGOT = mat("cryo_alloy_ingot", Rarity.RARE);
    public static final Supplier<Item> BIO_BATTERY = mat("bio_battery", Rarity.EPIC);
    public static final Supplier<Item> TITAN_ANTIDOTE = mat("titan_antidote", Rarity.RARE);

    // ---- 饰品（Curios 扩展坞系列：放入通用 curio 栏 → 授予额外功能栏）----
    public static final Supplier<Item> CRYO_ALLOY_RING_MOUNT = register("cryo_alloy_ring_mount",
            () -> new SlotExpanderCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), "ring", 1));
    public static final Supplier<Item> THOLIN_FIBRE_BELT_RIG = register("tholin_fibre_belt_rig",
            () -> new SlotExpanderCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "belt", 1));
    public static final Supplier<Item> METEORIC_IRON_BACK_FRAME = register("meteoric_iron_back_frame",
            () -> new SlotExpanderCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), "back", 1));
    // 维生套件（护符栏；每 10 秒回复 固定值 + 百分比，数值见 TMConfig）
    public static final Supplier<Item> THOLIN_LIFE_SUPPORT_KIT = register("tholin_life_support_kit",
            () -> new LifeSupportCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 200));
    // 毒腺手套（hands 槽；造成物理伤害时按概率附加托林毒素，逻辑见 CurioCombatHandler）
    public static final Supplier<Item> THOLIN_VENOM_GLOVE = register("tholin_venom_glove",
            () -> new ToxinGloveCurioItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // ---- 刷怪蛋 ----
    //? if forge {
    public static final Supplier<Item> AERO_JELLY_SPAWN_EGG = register("aero_jelly_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.AERO_JELLY, 0xC9A24B, 0xF0E6C0, new Item.Properties()));
    public static final Supplier<Item> CRYO_SCAVENGER_SPAWN_EGG = register("cryo_scavenger_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.CRYO_SCAVENGER, 0x8FD8F0, 0x3A6EA5, new Item.Properties()));
    public static final Supplier<Item> AMMONIA_STALKER_SPAWN_EGG = register("ammonia_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.AMMONIA_STALKER, 0x9FD8F0, 0x4B7A3A, new Item.Properties()));
    public static final Supplier<Item> CORRUPTED_PROBE_SPAWN_EGG = register("corrupted_probe_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.CORRUPTED_PROBE, 0x555555, 0xB0822E, new Item.Properties()));
    public static final Supplier<Item> THOLIN_WEAVER_SPAWN_EGG = register("tholin_weaver_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.THOLIN_WEAVER, 0xC86428, 0x3A2A1A, new Item.Properties()));
    public static final Supplier<Item> NATIVE_ICE_WORM_SPAWN_EGG = register("native_ice_worm_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.NATIVE_ICE_WORM, 0x8FD8F0, 0xE0F4FF, new Item.Properties()));
    public static final Supplier<Item> METHANE_MIDGE_SPAWN_EGG = register("methane_midge_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.METHANE_MIDGE, 0xBFD8A0, 0x6E8A4A, new Item.Properties()));
    public static final Supplier<Item> HYDROTROPH_GRAZER_SPAWN_EGG = register("hydrotroph_grazer_spawn_egg",
            () -> new ForgeSpawnEggItem(TMEntities.HYDROTROPH_GRAZER, 0x8A6E5A, 0xC8A87A, new Item.Properties()));
    //?} else {
    /*public static final Supplier<Item> AERO_JELLY_SPAWN_EGG = register("aero_jelly_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.AERO_JELLY, 0xC9A24B, 0xF0E6C0, new Item.Properties()));
    public static final Supplier<Item> CRYO_SCAVENGER_SPAWN_EGG = register("cryo_scavenger_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.CRYO_SCAVENGER, 0x8FD8F0, 0x3A6EA5, new Item.Properties()));
    public static final Supplier<Item> AMMONIA_STALKER_SPAWN_EGG = register("ammonia_stalker_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.AMMONIA_STALKER, 0x9FD8F0, 0x4B7A3A, new Item.Properties()));
    public static final Supplier<Item> CORRUPTED_PROBE_SPAWN_EGG = register("corrupted_probe_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.CORRUPTED_PROBE, 0x555555, 0xB0822E, new Item.Properties()));
    public static final Supplier<Item> THOLIN_WEAVER_SPAWN_EGG = register("tholin_weaver_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.THOLIN_WEAVER, 0xC86428, 0x3A2A1A, new Item.Properties()));
    public static final Supplier<Item> NATIVE_ICE_WORM_SPAWN_EGG = register("native_ice_worm_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.NATIVE_ICE_WORM, 0x8FD8F0, 0xE0F4FF, new Item.Properties()));
    public static final Supplier<Item> METHANE_MIDGE_SPAWN_EGG = register("methane_midge_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.METHANE_MIDGE, 0xBFD8A0, 0x6E8A4A, new Item.Properties()));
    public static final Supplier<Item> HYDROTROPH_GRAZER_SPAWN_EGG = register("hydrotroph_grazer_spawn_egg",
            () -> new DeferredSpawnEggItem(TMEntities.HYDROTROPH_GRAZER, 0x8A6E5A, 0xC8A87A, new Item.Properties()));
    *///?}

    private static Supplier<Item> register(String name, Supplier<Item> supplier) {
        return REGISTER.register(name, supplier);
    }

    /** 材料物品：按获取复杂度设稀有度（COMMON/UNCOMMON/RARE/EPIC 影响物品名颜色）。 */
    private static Supplier<Item> mat(String name, Rarity rarity) {
        return REGISTER.register(name, () -> new Item(new Item.Properties().rarity(rarity)));
    }

    private static Supplier<Item> blockItem(String name, Supplier<Block> block) {
        return REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
