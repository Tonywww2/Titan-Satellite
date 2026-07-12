package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * 物品注册表：材料物品、固体方块的 BlockItem、流体桶。
 */
public final class TSItems {

    private TSItems() {
    }

    public static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(ForgeRegistries.ITEMS, TitanSatellite.MODID);

    // ---- 材料物品 ----
    // 浮游薄膜：甲烷浮游体掉落的轻量材料（占位，复用原版幻翼膜贴图）。
    public static final RegistryObject<Item> AERO_MEMBRANE = register("aero_membrane",
            () -> new Item(new Item.Properties()));

    // ---- 方块物品（与 TSBlocks 一一对应，流体方块除外）----
    public static final RegistryObject<Item> TITAN_STONE = blockItem("titan_stone", TSBlocks.TITAN_STONE);
    public static final RegistryObject<Item> TITAN_BASALT = blockItem("titan_basalt", TSBlocks.TITAN_BASALT);
    public static final RegistryObject<Item> THOLIN_SAND = blockItem("tholin_sand", TSBlocks.THOLIN_SAND);
    public static final RegistryObject<Item> CRUSHED_ICE = blockItem("crushed_ice", TSBlocks.CRUSHED_ICE);
    public static final RegistryObject<Item> CRYO_ICE = blockItem("cryo_ice", TSBlocks.CRYO_ICE);
    public static final RegistryObject<Item> PACKED_METHANE_ICE = blockItem("packed_methane_ice", TSBlocks.PACKED_METHANE_ICE);
    public static final RegistryObject<Item> CRYOVOLCANIC_GEYSER = blockItem("cryovolcanic_geyser", TSBlocks.CRYOVOLCANIC_GEYSER);
    public static final RegistryObject<Item> METHANE_POOL_CORE = blockItem("methane_pool_core", TSBlocks.METHANE_POOL_CORE);
    public static final RegistryObject<Item> SPECIAL_METHANE_PUMP = blockItem("special_methane_pump", TSBlocks.SPECIAL_METHANE_PUMP);
    public static final RegistryObject<Item> THOLIN_CRYSTAL = blockItem("tholin_crystal", TSBlocks.THOLIN_CRYSTAL);

    // ---- M6 群系特色化新增方块物品（PG-1）----
    public static final RegistryObject<Item> WEATHERED_TITAN_STONE = blockItem("weathered_titan_stone", TSBlocks.WEATHERED_TITAN_STONE);
    public static final RegistryObject<Item> SEDIMENTARY_TITAN_STONE = blockItem("sedimentary_titan_stone", TSBlocks.SEDIMENTARY_TITAN_STONE);
    public static final RegistryObject<Item> BRANCH_CRYSTAL = blockItem("branch_crystal", TSBlocks.BRANCH_CRYSTAL);

    // ---- 流体桶 ----
    public static final RegistryObject<Item> LIQUID_METHANE_BUCKET = register("liquid_methane_bucket",
            () -> new BucketItem(TSFluids.LIQUID_METHANE, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> LIQUID_AMMONIA_BUCKET = register("liquid_ammonia_bucket",
            () -> new BucketItem(TSFluids.LIQUID_AMMONIA, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // ---- 生物掉落材料（M2）----
    public static final RegistryObject<Item> CRYO_CARAPACE = register("cryo_carapace", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOXIC_GLAND = register("toxic_gland", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEPLETED_BATTERY = register("depleted_battery", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PRECISION_COMPONENTS = register("precision_components", () -> new Item(new Item.Properties()));

    // ---- 刷怪蛋 ----
    public static final RegistryObject<Item> AERO_JELLY_SPAWN_EGG = register("aero_jelly_spawn_egg",
            () -> new ForgeSpawnEggItem(TSEntities.AERO_JELLY, 0xC9A24B, 0xF0E6C0, new Item.Properties()));
    public static final RegistryObject<Item> CRYO_SCAVENGER_SPAWN_EGG = register("cryo_scavenger_spawn_egg",
            () -> new ForgeSpawnEggItem(TSEntities.CRYO_SCAVENGER, 0x8FD8F0, 0x3A6EA5, new Item.Properties()));
    public static final RegistryObject<Item> AMMONIA_STALKER_SPAWN_EGG = register("ammonia_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(TSEntities.AMMONIA_STALKER, 0x9FD8F0, 0x4B7A3A, new Item.Properties()));
    public static final RegistryObject<Item> CORRUPTED_PROBE_SPAWN_EGG = register("corrupted_probe_spawn_egg",
            () -> new ForgeSpawnEggItem(TSEntities.CORRUPTED_PROBE, 0x555555, 0xB0822E, new Item.Properties()));

    private static RegistryObject<Item> register(String name, Supplier<Item> supplier) {
        return REGISTER.register(name, supplier);
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
