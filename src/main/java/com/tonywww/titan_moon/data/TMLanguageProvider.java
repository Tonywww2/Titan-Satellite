package com.tonywww.titan_moon.data;

import com.tonywww.titan_moon.TitanMoon;
import net.minecraft.data.PackOutput;
//? if forge {
import net.minecraftforge.common.data.LanguageProvider;
//?} else {
/*import net.neoforged.neoforge.common.data.LanguageProvider;
*///?}

/**
 * 语言文件 datagen（en_us + zh_cn）。一个类按 locale 生成两份，复刻现有 lang 全部键
 * （含若干无对应注册物的历史键：crystalline_twig / tholin_fibre / tough_neural_gland / tholin_silk_sac）。
 */
public class TMLanguageProvider extends LanguageProvider {

    private final boolean en;

    public TMLanguageProvider(PackOutput output, String locale) {
        super(output, TitanMoon.MODID, locale);
        this.en = locale.equals("en_us");
    }

    /** 按 locale 选择译文。 */
    private void t(String key, String enValue, String zhValue) {
        add(key, en ? enValue : zhValue);
    }

    @Override
    protected void addTranslations() {
        t("itemGroup.titan_moon.titan", "Titan Moon", "土卫六");

        t("planet.titan_moon.titan", "Titan", "土卫六");

        // 方块
        t("block.titan_moon.titan_stone", "Titan Stone", "泰坦岩");
        t("block.titan_moon.titan_basalt", "Titan Basalt", "泰坦玄武岩");
        t("block.titan_moon.tholin_sand", "Tholin Sand", "托林沙");
        t("block.titan_moon.crushed_ice", "Crushed Ice", "碎冰");
        t("block.titan_moon.cryo_ice", "Cryo Ice", "寒冰");
        t("block.titan_moon.packed_methane_ice", "Packed Methane Ice", "甲烷浮冰");
        t("block.titan_moon.cryovolcanic_geyser", "Cryovolcanic Geyser", "冰火山喷泉");
        t("block.titan_moon.methane_pool_core", "Methane Pool Core", "甲烷池核心");
        t("block.titan_moon.special_methane_pump", "Special Methane Pump", "特制甲烷泵");
        t("block.titan_moon.tholin_crystal", "Tholin Crystal", "托林晶体");
        t("block.titan_moon.liquid_methane", "Liquid Methane", "液态甲烷");
        t("block.titan_moon.liquid_ammonia", "Liquid Ammonia", "液态氨");
        t("block.titan_moon.liquid_hydrogen", "Liquid Hydrogen", "液氢");
        t("block.titan_moon.weathered_titan_stone", "Weathered Titan Stone", "风化泰坦石");
        t("block.titan_moon.sedimentary_titan_stone", "Sedimentary Titan Stone", "沉积泰坦石");
        t("block.titan_moon.branch_crystal", "Branch Crystal", "树枝状结晶");
        t("block.titan_moon.abyss_crystal", "Abyss Crystal", "深渊晶体");
        t("block.titan_moon.tholin_tar", "Tholin Tar", "托林焦油");
        t("block.titan_moon.meteor_fragment", "Meteor Fragment", "陨铁碎块");
        t("block.titan_moon.hardened_tholin", "Hardened Tholin", "硬化托林");
        t("block.titan_moon.tholin_shrub", "Tholin Shrub", "托林灌木");
        t("block.titan_moon.methane_ice_bloom", "Methane Ice Bloom", "甲烷冰花");
        t("block.titan_moon.ammonia_crystal", "Ammonia Crystal", "氨晶体");
        t("block.titan_moon.titan_gravel", "Titan Gravel", "泰坦砾石");
        t("block.titan_moon.frost_bush", "Frost Bush", "霜枯灌木");
        t("block.titan_moon.acetylene_spire", "Acetylene Ice Spire", "乙炔冰笋");
        t("block.titan_moon.tholin_mycelium", "Tholin Mycelium", "托林菌网");
        t("block.titan_moon.hydrogen_bubble_mat", "Hydrogen Bubble Mat", "氢泡菌毯");
        t("block.titan_moon.hydrogen_collector", "Hydrogen Collector", "集氢罩");
        t("block.titan_moon.tholin_composter", "Tholin Composter", "托林堆肥槽");
        t("block.titan_moon.tholin_composter.hint", "Needs Tholin Mycelium below; insert biomatter (fibre / silk sac / twig / rotten flesh).", "需下方为托林菌网；投入生物残渣（纤维 / 丝囊 / 枝条 / 腐肉）。");

        // 物品
        t("item.titan_moon.aero_membrane", "Aero-Membrane", "浮游薄膜");
        t("item.titan_moon.cryo_carapace", "Cryo-Carapace", "冰晶甲壳");
        t("item.titan_moon.toxic_gland", "Toxic Gland", "毒性腺体");
        t("item.titan_moon.depleted_battery", "Depleted High-Energy Battery", "废弃高能电池");
        t("item.titan_moon.precision_components", "Precision Electronic Components", "精密电子元件");
        t("item.titan_moon.crystalline_twig", "Crystalline Twig", "晶化枝条");
        t("item.titan_moon.tholin_fibre", "Tholin Fibre", "托林纤维");
        t("item.titan_moon.tough_neural_gland", "Tough Neural Gland", "强韧神经腺");
        t("item.titan_moon.tholin_silk_sac", "Tholin Silk Sac", "托林丝囊");

        // 材料加工链物品
        t("item.titan_moon.tholin_dust", "Tholin Dust", "托林粉末");
        t("item.titan_moon.condensed_acetylene", "Condensed Acetylene", "凝乙炔");
        t("item.titan_moon.hydrogen_capsule", "Hydrogen Capsule", "氢气瓶");
        t("item.titan_moon.meteoric_iron_ingot", "Meteoric Iron Ingot", "陨铁锭");
        t("item.titan_moon.silicon_dust", "Silicon Dust", "硅晶粉");
        t("item.titan_moon.methane_ice_shard", "Methane Ice Shard", "甲烷冰晶");
        t("item.titan_moon.abyss_crystal_dust", "Abyss Crystal Dust", "深渊晶粉");
        t("item.titan_moon.ammonia_salt", "Ammonia Salt", "氨盐");
        t("item.titan_moon.tholin_crystal_dust", "Tholin Crystal Dust", "托林晶粉");
        t("item.titan_moon.polyphosphazene_coenzyme", "Polyphosphazene Coenzyme", "多磷腈辅酶");
        t("item.titan_moon.azotosome_sheet", "Azotosome Sheet", "氮质体膜片");
        t("item.titan_moon.cryo_alloy_ingot", "Cryo-Alloy Ingot", "冰晶合金锭");
        t("item.titan_moon.bio_battery", "Bio-Battery", "生物电池");
        t("item.titan_moon.titan_antidote", "Titan Antidote", "异星解毒剂");
        // 饰品（Curios 扩展坞系列）
        t("item.titan_moon.cryo_alloy_ring_mount", "Cryo-Alloy Ring Mount", "冰晶合金指环座");
        t("item.titan_moon.tholin_fibre_belt_rig", "Tholin-Fibre Belt Rig", "托林纤维腰挂");
        t("item.titan_moon.meteoric_iron_back_frame", "Meteoric-Iron Back Frame", "陨铁背架");
        t("item.titan_moon.tholin_life_support_kit", "Tholin Life-Support Kit", "托林维生套件");
        t("item.titan_moon.tholin_venom_glove", "Tholin Venom Glove", "托林毒腺手套");
        t("item.titan_moon.tholin_venom_glove.desc",
                "On physical hits: %s%% chance to inflict Tholin Toxin.",
                "造成物理伤害时：%s%% 概率附加托林毒素。");
        t("item.titan_moon.tholin_life_support_kit.desc",
                "While worn, restores %2$s + %3$s%% max health every %1$s seconds.",
                "佩戴时每 %1$s 秒回复 %2$s + %3$s%% 最大生命值。");        t("item.titan_moon.liquid_methane_bucket", "Liquid Methane Bucket", "液态甲烷桶");
        t("item.titan_moon.liquid_ammonia_bucket", "Liquid Ammonia Bucket", "液态氨桶");
        t("item.titan_moon.liquid_hydrogen_bucket", "Liquid Hydrogen Bucket", "液氢桶");

        // 流体类型（FluidType.getDescriptionId → fluid_type.<ns>.<path>）
        t("fluid_type.titan_moon.liquid_methane", "Liquid Methane", "液态甲烷");
        t("fluid_type.titan_moon.liquid_ammonia", "Liquid Ammonia", "液态氨");
        t("fluid_type.titan_moon.liquid_hydrogen", "Liquid Hydrogen", "液氢");
        t("item.titan_moon.aero_jelly_spawn_egg", "Aero-Jelly Spawn Egg", "甲烷浮游体刷怪蛋");
        t("item.titan_moon.cryo_scavenger_spawn_egg", "Cryo-Scavenger Spawn Egg", "冰硅甲虫刷怪蛋");
        t("item.titan_moon.ammonia_stalker_spawn_egg", "Ammonia Stalker Spawn Egg", "氨泉掠食者刷怪蛋");
        t("item.titan_moon.corrupted_probe_spawn_egg", "Tholin-Parasitized Probe Spawn Egg", "托林寄生探测器刷怪蛋");
        t("item.titan_moon.tholin_weaver_spawn_egg", "Tholin-Weaver Spawn Egg", "托林织体蛛刷怪蛋");
        t("item.titan_moon.native_ice_worm_spawn_egg", "Native Ice Worm Spawn Egg", "原生冰虫刷怪蛋");
        t("item.titan_moon.methane_midge_spawn_egg", "Methane Midge Spawn Egg", "甲烷微浮群刷怪蛋");
        t("item.titan_moon.hydrotroph_grazer_spawn_egg", "Hydrotroph Grazer Spawn Egg", "氢营养蹒兽刷怪蛋");

        // 状态效果
        t("effect.titan_moon.tholin_toxin", "Tholin Toxin", "托林毒素");

        // 实体
        t("entity.titan_moon.aero_jelly", "Aero-Jelly", "甲烷浮游体");
        t("entity.titan_moon.cryo_scavenger", "Cryo-Scavenger", "冰硅甲虫");
        t("entity.titan_moon.ammonia_stalker", "Ammonia Stalker", "氨泉掠食者");
        t("entity.titan_moon.corrupted_probe", "Tholin-Parasitized Probe", "托林寄生探测器");
        t("entity.titan_moon.tholin_weaver", "Tholin-Weaver", "托林织体蛛");
        t("entity.titan_moon.native_ice_worm", "Native Ice Worm", "原生冰虫");
        t("entity.titan_moon.methane_midge", "Methane Midge", "甲烷微浮群");
        t("entity.titan_moon.hydrotroph_grazer", "Hydrotroph Grazer", "氢营养蹒兽");

        // 群系
        t("biome.titan_moon.methane_abyss", "Titan \u00b7 Methane Abyss", "土卫六\u00b7液态甲烷深渊");
        t("biome.titan_moon.cratered_wastelands", "Titan \u00b7 Cratered Wastelands", "土卫六\u00b7撞击陨坑荒原");
        t("biome.titan_moon.tholin_dune_sea", "Titan \u00b7 Tholin Dune Sea", "土卫六\u00b7托林沙海");
        t("biome.titan_moon.polar_labyrinth", "Titan \u00b7 Polar Labyrinth", "土卫六\u00b7极地迷宫冰原");
        t("biome.titan_moon.cryovolcanic_cliff", "Titan \u00b7 Cryovolcanic Cliff", "土卫六\u00b7冰火山断崖");
        t("biome.titan_moon.barren_plateau", "Titan \u00b7 Barren Plateau", "土卫六\u00b7荒芜高原");
    }
}
