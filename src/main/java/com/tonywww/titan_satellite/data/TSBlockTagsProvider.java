package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * 方块 tags datagen。复刻 #titan_satellite:base_stone（ore/disk 特征的 target 基岩集）。
 * 群系 tags（is_titan / has_structure）与群系耦合，在 worldgen 阶段随注册集生成。
 */
public class TSBlockTagsProvider extends BlockTagsProvider {

    private static final TagKey<Block> BASE_STONE =
            TagKey.create(Registries.BLOCK, new ResourceLocation(TitanSatellite.MODID, "base_stone"));

    public TSBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TitanSatellite.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BASE_STONE).add(
                TSBlocks.TITAN_STONE.get(),
                TSBlocks.TITAN_BASALT.get(),
                TSBlocks.WEATHERED_TITAN_STONE.get(),
                TSBlocks.SEDIMENTARY_TITAN_STONE.get());
    }
}
