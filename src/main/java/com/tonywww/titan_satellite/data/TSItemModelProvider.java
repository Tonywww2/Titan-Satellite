package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 非方块物品的模型 datagen（材料/桶/刷怪蛋）。方块物品模型在 {@link TSBlockStateProvider} 生成。
 * 复刻现有：材料/桶为 generated + 复用原版贴图；刷怪蛋 parent template_spawn_egg。
 */
public class TSItemModelProvider extends ItemModelProvider {

    public TSItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TitanSatellite.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 材料物品（generated + 复用原版贴图）
        generated("aero_membrane", "minecraft:item/phantom_membrane");
        generated("cryo_carapace", "minecraft:item/prismarine_shard");
        generated("toxic_gland", "minecraft:item/spider_eye");
        generated("depleted_battery", "minecraft:item/redstone");
        generated("precision_components", "minecraft:item/copper_ingot");

        // 生态深化材料（ECO-A2；generated + 复用原版贴图占位）
        generated("crystalline_twig", "minecraft:item/stick");
        generated("tholin_fibre", "minecraft:item/string");
        generated("tough_neural_gland", "minecraft:item/glow_ink_sac");
        generated("tholin_silk_sac", "minecraft:item/slime_ball");

        // 流体桶（generated + water_bucket 贴图）
        generated("liquid_methane_bucket", "minecraft:item/water_bucket");
        generated("liquid_ammonia_bucket", "minecraft:item/water_bucket");

        // 刷怪蛋（parent template_spawn_egg，无贴图）
        withExistingParent("aero_jelly_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("cryo_scavenger_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("ammonia_stalker_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("corrupted_probe_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("tholin_weaver_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("native_ice_worm_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("methane_midge_spawn_egg", mcLoc("item/template_spawn_egg"));
        withExistingParent("hydrotroph_grazer_spawn_egg", mcLoc("item/template_spawn_egg"));
    }

    private void generated(String name, String texture) {
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", new ResourceLocation(texture));
    }
}
