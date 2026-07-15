package com.tonywww.titan_moon.data.loot;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMItems;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
//? if neoforge {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
*///?}
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

/**
 * 箱子 loot（复刻现有 2 个）：pioneer_outpost（rolls 3-6）、tholin_geode（rolls 3-5）。
 * 条目内联（避免显式命名 LootPoolSingletonContainer.Builder 在 Loom 编译类路径下不解析）。
 */
public class TMChestLoot implements LootTableSubProvider {

    //? if neoforge {
    /*public TMChestLoot(HolderLookup.Provider provider) {
    }
    *///?}

    //? if forge {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
    //?} else {
    /*@Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
    *///?}
        consumer.accept(chest("pioneer_outpost"), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                        .add(LootItem.lootTableItem(TMItems.DEPLETED_BATTERY.get()).setWeight(8)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(TMItems.PRECISION_COMPONENTS.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(6)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                        .add(LootItem.lootTableItem(Items.COOKED_BEEF).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 6.0F))))));

        consumer.accept(chest("tholin_geode"), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(3.0F, 5.0F))
                        .add(LootItem.lootTableItem(TMItems.THOLIN_CRYSTAL.get()).setWeight(8)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                        .add(LootItem.lootTableItem(TMItems.PRECISION_COMPONENTS.get()).setWeight(6)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(TMItems.CRYO_CARAPACE.get()).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        .add(LootItem.lootTableItem(TMItems.AERO_MEMBRANE.get()).setWeight(4)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(2))));
    }

    //? if forge {
    private static ResourceLocation chest(String name) {
        return TitanMoon.rl("chests/" + name);
    }
    //?} else {
    /*private static ResourceKey<LootTable> chest(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, TitanMoon.rl("chests/" + name));
    }
    *///?}
}
