package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSFluids;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
//? if forge {
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
//?} else {
/*import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
*///?}

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
        // 材料物品（generated + 本模组纯色占位贴图）
        generated("aero_membrane", "titan_satellite:item/aero_membrane");
        generated("cryo_carapace", "titan_satellite:item/cryo_carapace");
        generated("toxic_gland", "titan_satellite:item/toxic_gland");
        generated("depleted_battery", "titan_satellite:item/depleted_battery");
        generated("precision_components", "titan_satellite:item/precision_components");

        // 生态深化材料（ECO-A2；generated + 本模组纯色占位贴图）
        generated("crystalline_twig", "titan_satellite:item/crystalline_twig");
        generated("tholin_fibre", "titan_satellite:item/tholin_fibre");
        generated("tough_neural_gland", "titan_satellite:item/tough_neural_gland");
        generated("tholin_silk_sac", "titan_satellite:item/tholin_silk_sac");

        // 材料加工链（MC1；generated + 纯色占位贴图）
        generated("tholin_dust", "titan_satellite:item/tholin_dust");
        generated("condensed_acetylene", "titan_satellite:item/condensed_acetylene");
        generated("hydrogen_capsule", "titan_satellite:item/hydrogen_capsule");
        generated("meteoric_iron_ingot", "titan_satellite:item/meteoric_iron_ingot");
        generated("silicon_dust", "titan_satellite:item/silicon_dust");
        generated("methane_ice_shard", "titan_satellite:item/methane_ice_shard");
        generated("abyss_crystal_dust", "titan_satellite:item/abyss_crystal_dust");
        generated("ammonia_salt", "titan_satellite:item/ammonia_salt");
        generated("tholin_crystal_dust", "titan_satellite:item/tholin_crystal_dust");
        generated("polyphosphazene_coenzyme", "titan_satellite:item/polyphosphazene_coenzyme");
        generated("azotosome_sheet", "titan_satellite:item/azotosome_sheet");
        generated("cryo_alloy_ingot", "titan_satellite:item/cryo_alloy_ingot");
        generated("bio_battery", "titan_satellite:item/bio_battery");
        generated("titan_antidote", "titan_satellite:item/titan_antidote");

        // 流体桶：forge:fluid_container 动态模型（空桶轮廓 + 按流体 FluidType.getTintColor 染色的液面），
        // 不再复用固定蓝色的 water_bucket 贴图，故每种流体的桶显示各自的液体颜色。
        fluidBucket("liquid_methane_bucket", TSFluids.LIQUID_METHANE.get());
        fluidBucket("liquid_ammonia_bucket", TSFluids.LIQUID_AMMONIA.get());

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
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", TitanSatellite.parse(texture));
    }

    // Forge 动态流体容器模型：空桶轮廓 + 流体液面（液面为 tintindex 1，颜色由 RegisterColorHandlersEvent.Item
    // 注册的 DynamicFluidContainerModel.Colors 按流体 getTintColor 提供，见 TitanClientEvents）。
    // 父模型 forge:item/bucket 用 UncheckedModelFile 避免 datagen 的 ExistingFileHelper 校验报错。
    // 注意：1.20.1 的 forge:fluid_container 加载器不读 apply_tint 字段，染色只能靠上面的 ItemColor。
    private void fluidBucket(String name, Fluid fluid) {
        //? if forge {
        getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("forge", "item/bucket")))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(fluid)
                .end();
        //?} else {
        /*getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("neoforge", "item/bucket")))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(fluid)
                .end();
        *///?}
    }
}
