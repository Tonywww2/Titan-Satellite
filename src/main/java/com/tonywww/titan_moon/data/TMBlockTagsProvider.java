package com.tonywww.titan_moon.data;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
//? if forge {
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
//?} else {
/*import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
*///?}

import java.util.concurrent.CompletableFuture;

/**
 * 方块 tags datagen。复刻 #titan_moon:base_stone（ore/disk 特征的 target 基岩集）。
 * 群系 tags（is_titan / has_structure）与群系耦合，在 worldgen 阶段随注册集生成。
 */
public class TMBlockTagsProvider extends BlockTagsProvider {

    private static final TagKey<Block> BASE_STONE =
            TagKey.create(Registries.BLOCK, TitanMoon.rl("base_stone"));

    public TMBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TitanMoon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BASE_STONE).add(
                TMBlocks.TITAN_STONE.get(),
                TMBlocks.TITAN_BASALT.get(),
                TMBlocks.WEATHERED_TITAN_STONE.get(),
                TMBlocks.SEDIMENTARY_TITAN_STONE.get());
    }
}
