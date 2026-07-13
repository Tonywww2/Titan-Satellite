package com.tonywww.titan_satellite.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/**
 * loot 总 provider：方块自掉落 + 实体掉落 + 箱子。
 */
public class TSLootTableProvider extends LootTableProvider {

    public TSLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(TSBlockLoot::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(TSEntityLoot::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(TSChestLoot::new, LootContextParamSets.CHEST)
        ));
    }
}
