package com.tonywww.titan_satellite.data.loot;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
//? if forge {
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
*///?}
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

/**
 * 实体掉落 loot（复刻现有 3 个）：
 * cryo_scavenger→cryo_carapace(0-2)+looting(0-1)；ammonia_stalker→toxic_gland(0-2)+looting；
 * corrupted_probe→depleted_battery(0-1)+looting 与 precision_components(0-2)+looting（两池）。
 */
public class TSEntityLoot implements LootTableSubProvider {

    //? if neoforge {
    /*private final HolderLookup.Provider provider;

    public TSEntityLoot(HolderLookup.Provider provider) {
        this.provider = provider;
    }
    *///?}

    //? if forge {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
    //?} else {
    /*@Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
    *///?}
        consumer.accept(entity("cryo_scavenger"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.CRYO_CARAPACE.get(), 0, 2)));

        consumer.accept(entity("ammonia_stalker"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.TOXIC_GLAND.get(), 0, 2)));

        consumer.accept(entity("corrupted_probe"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.DEPLETED_BATTERY.get(), 0, 1))
                .withPool(singleDrop(TSItems.PRECISION_COMPONENTS.get(), 0, 2)));

        consumer.accept(entity("tholin_weaver"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.TOUGH_NEURAL_GLAND.get(), 0, 1))
                .withPool(singleDrop(TSItems.THOLIN_SILK_SAC.get(), 0, 2)));

        consumer.accept(entity("native_ice_worm"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.TOUGH_NEURAL_GLAND.get(), 1, 2))
                .withPool(singleDrop(TSItems.CRYO_CARAPACE.get(), 0, 2)));

        // 微浮群太小无掉落（空表）；蹒兽掉落少量浮游薄膜（化能气囊）。
        consumer.accept(entity("methane_midge"), LootTable.lootTable());

        consumer.accept(entity("hydrotroph_grazer"), LootTable.lootTable()
                .withPool(singleDrop(TSItems.AERO_MEMBRANE.get(), 0, 1)));
    }

    /** 单物品掉落池：uniform(min,max) 数量 + looting_enchant(0-1)。 */
    private LootPool.Builder singleDrop(Item item, float min, float max) {
        //? if forge {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))));
        //?} else {
        /*return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.provider, UniformGenerator.between(0.0F, 1.0F))));
        *///?}
    }

    //? if forge {
    private static ResourceLocation entity(String name) {
        return TitanSatellite.rl("entities/" + name);
    }
    //?} else {
    /*private static ResourceKey<LootTable> entity(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, TitanSatellite.rl("entities/" + name));
    }
    *///?}
}
