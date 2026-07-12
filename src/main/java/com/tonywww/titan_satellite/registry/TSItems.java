package com.tonywww.titan_satellite.registry;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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

    // ---- 流体桶 ----
    public static final RegistryObject<Item> LIQUID_METHANE_BUCKET = register("liquid_methane_bucket",
            () -> new BucketItem(TSFluids.LIQUID_METHANE, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));
    public static final RegistryObject<Item> LIQUID_AMMONIA_BUCKET = register("liquid_ammonia_bucket",
            () -> new BucketItem(TSFluids.LIQUID_AMMONIA, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    private static RegistryObject<Item> register(String name, Supplier<Item> supplier) {
        return REGISTER.register(name, supplier);
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
