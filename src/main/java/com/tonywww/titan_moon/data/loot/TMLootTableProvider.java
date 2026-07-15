package com.tonywww.titan_moon.data.loot;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//? if neoforge {
/*import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;
*///?}

import java.util.List;
import java.util.Set;

/**
 * loot 总 provider：方块自掉落 + 实体掉落 + 箱子。
 */
public class TMLootTableProvider extends LootTableProvider {

    //? if forge {
    public TMLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(TMBlockLoot::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(TMEntityLoot::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(TMChestLoot::new, LootContextParamSets.CHEST)
        ));
    }
    //?} else {
    /*public TMLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(TMBlockLoot::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(TMEntityLoot::new, LootContextParamSets.ENTITY),
                new SubProviderEntry(TMChestLoot::new, LootContextParamSets.CHEST)
        ), lookup);
    }
    *///?}
}
