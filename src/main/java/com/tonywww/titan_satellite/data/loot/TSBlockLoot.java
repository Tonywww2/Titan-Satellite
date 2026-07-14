package com.tonywww.titan_satellite.data.loot;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSBlocks;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
//? if forge {
import net.minecraftforge.registries.ForgeRegistries;
//?} else {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
*///?}

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 方块掉落 loot（此前项目<b>无</b>方块 loot，方块挖了不掉落）。为全部本 mod 固体方块补自掉落
 * （dropSelf）；液体方块（LiquidBlock，properties 已 noLootTable）不生成。
 */
public class TSBlockLoot extends BlockLootSubProvider {

    private final List<Block> blocks;

    //? if forge {
    public TSBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        this.blocks = ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(TitanSatellite.MODID))
                .map(Map.Entry::getValue)
                .filter(b -> !(b instanceof LiquidBlock))
                .toList();
    }
    //?} else {
    /*public TSBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        this.blocks = BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(TitanSatellite.MODID))
                .map(Map.Entry::getValue)
                .filter(b -> !(b instanceof LiquidBlock))
                .toList();
    }
    *///?}

    @Override
    protected void generate() {
        blocks.forEach(this::dropSelf);
        // 生态采集覆盖（在 dropSelf 后 put 以覆写）
        // C3：树枝结晶 镐采→晶化枝条 1–2（精采例外掋方块）
        this.add(TSBlocks.BRANCH_CRYSTAL.get(), createSilkTouchDispatchTable(TSBlocks.BRANCH_CRYSTAL.get(),
                LootItem.lootTableItem(TSItems.CRYSTALLINE_TWIG.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))));
        // C2：灌木剑刀采→托林纤维（否则无掉落）
        this.add(TSBlocks.THOLIN_SHRUB.get(), createShearsOnlyDrop(TSItems.THOLIN_FIBRE.get()));
        this.add(TSBlocks.FROST_BUSH.get(), createShearsOnlyDrop(TSItems.THOLIN_FIBRE.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return blocks;
    }
}
