package com.tonywww.titan_satellite.data;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * 语言文件 datagen（en_us + zh_cn）。一个类按 locale 生成两份，复刻现有 lang 全部键
 * （含若干无对应注册物的历史键：crystalline_twig / tholin_fibre / tough_neural_gland / tholin_silk_sac）。
 */
public class TSLanguageProvider extends LanguageProvider {

    private final boolean en;

    public TSLanguageProvider(PackOutput output, String locale) {
        super(output, TitanSatellite.MODID, locale);
        this.en = locale.equals("en_us");
    }

    /** 按 locale 选择译文。 */
    private void t(String key, String enValue, String zhValue) {
        add(key, en ? enValue : zhValue);
    }

    @Override
    protected void addTranslations() {
        t("itemGroup.titan_satellite.titan", "Titan Satellite", "土卫六");

        t("planet.titan_satellite.titan", "Titan", "土卫六");

        // 方块
        t("block.titan_satellite.titan_stone", "Titan Stone", "泰坦岩");
        t("block.titan_satellite.titan_basalt", "Titan Basalt", "泰坦玄武岩");
        t("block.titan_satellite.tholin_sand", "Tholin Sand", "托林沙");
        t("block.titan_satellite.crushed_ice", "Crushed Ice", "碎冰");
        t("block.titan_satellite.cryo_ice", "Cryo Ice", "寒冰");
        t("block.titan_satellite.packed_methane_ice", "Packed Methane Ice", "甲烷浮冰");
        t("block.titan_satellite.cryovolcanic_geyser", "Cryovolcanic Geyser", "冰火山喷泉");
        t("block.titan_satellite.methane_pool_core", "Methane Pool Core", "甲烷池核心");
        t("block.titan_satellite.special_methane_pump", "Special Methane Pump", "特制甲烷泵");
        t("block.titan_satellite.tholin_crystal", "Tholin Crystal", "托林晶体");
        t("block.titan_satellite.liquid_methane", "Liquid Methane", "液态甲烷");
        t("block.titan_satellite.liquid_ammonia", "Liquid Ammonia", "液态氨");
        t("block.titan_satellite.weathered_titan_stone", "Weathered Titan Stone", "风化泰坦石");
        t("block.titan_satellite.sedimentary_titan_stone", "Sedimentary Titan Stone", "沉积泰坦石");
        t("block.titan_satellite.branch_crystal", "Branch Crystal", "树枝状结晶");
        t("block.titan_satellite.abyss_crystal", "Abyss Crystal", "深渊晶体");
        t("block.titan_satellite.tholin_tar", "Tholin Tar", "托林焦油");
        t("block.titan_satellite.meteor_fragment", "Meteor Fragment", "陨铁碎块");
        t("block.titan_satellite.hardened_tholin", "Hardened Tholin", "硬化托林");
        t("block.titan_satellite.tholin_shrub", "Tholin Shrub", "托林灌木");
        t("block.titan_satellite.methane_ice_bloom", "Methane Ice Bloom", "甲烷冰花");
        t("block.titan_satellite.ammonia_crystal", "Ammonia Crystal", "氨晶体");
        t("block.titan_satellite.titan_gravel", "Titan Gravel", "泰坦砾石");
        t("block.titan_satellite.frost_bush", "Frost Bush", "霜枯灌木");

        // 物品
        t("item.titan_satellite.aero_membrane", "Aero-Membrane", "浮游薄膜");
        t("item.titan_satellite.cryo_carapace", "Cryo-Carapace", "冰晶甲壳");
        t("item.titan_satellite.toxic_gland", "Toxic Gland", "毒性腺体");
        t("item.titan_satellite.depleted_battery", "Depleted High-Energy Battery", "废弃高能电池");
        t("item.titan_satellite.precision_components", "Precision Electronic Components", "精密电子元件");
        t("item.titan_satellite.crystalline_twig", "Crystalline Twig", "晶化枝条");
        t("item.titan_satellite.tholin_fibre", "Tholin Fibre", "托林纤维");
        t("item.titan_satellite.tough_neural_gland", "Tough Neural Gland", "强韧神经腺");
        t("item.titan_satellite.tholin_silk_sac", "Tholin Silk Sac", "托林丝囊");
        t("item.titan_satellite.liquid_methane_bucket", "Liquid Methane Bucket", "液态甲烷桶");
        t("item.titan_satellite.liquid_ammonia_bucket", "Liquid Ammonia Bucket", "液态氨桶");
        t("item.titan_satellite.aero_jelly_spawn_egg", "Aero-Jelly Spawn Egg", "甲烷浮游体刷怪蛋");
        t("item.titan_satellite.cryo_scavenger_spawn_egg", "Cryo-Scavenger Spawn Egg", "冰硅甲虫刷怪蛋");
        t("item.titan_satellite.ammonia_stalker_spawn_egg", "Ammonia Stalker Spawn Egg", "氨泉掉食者刷怪蛋");
        t("item.titan_satellite.corrupted_probe_spawn_egg", "Tholin-Parasitized Probe Spawn Egg", "托林寄生探测器刷怪蛋");
        t("item.titan_satellite.tholin_weaver_spawn_egg", "Tholin-Weaver Spawn Egg", "托林织体蛛刷怪蛋");

        // 状态效果
        t("effect.titan_satellite.tholin_toxin", "Tholin Toxin", "托林毒素");

        // 实体
        t("entity.titan_satellite.aero_jelly", "Aero-Jelly", "甲烷浮游体");
        t("entity.titan_satellite.cryo_scavenger", "Cryo-Scavenger", "冰硅甲虫");
        t("entity.titan_satellite.ammonia_stalker", "Ammonia Stalker", "氨泉掉食者");
        t("entity.titan_satellite.corrupted_probe", "Tholin-Parasitized Probe", "托林寄生探测器");
        t("entity.titan_satellite.tholin_weaver", "Tholin-Weaver", "托林织体蛛");

        // 群系
        t("biome.titan_satellite.methane_abyss", "Titan \u00b7 Methane Abyss", "土卫六\u00b7液态甲烷深渊");
        t("biome.titan_satellite.cratered_wastelands", "Titan \u00b7 Cratered Wastelands", "土卫六\u00b7撞击陨坑荒原");
        t("biome.titan_satellite.tholin_dune_sea", "Titan \u00b7 Tholin Dune Sea", "土卫六\u00b7托林沙海");
        t("biome.titan_satellite.polar_labyrinth", "Titan \u00b7 Polar Labyrinth", "土卫六\u00b7极地迷宫冰原");
        t("biome.titan_satellite.cryovolcanic_cliff", "Titan \u00b7 Cryovolcanic Cliff", "土卫六\u00b7冰火山断崖");
        t("biome.titan_satellite.barren_plateau", "Titan \u00b7 Barren Plateau", "土卫六\u00b7荒芜高原");
    }
}
