package com.tonywww.titan_moon.data;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
//? if forge {
import net.minecraftforge.common.data.ExistingFileHelper;
//?} else {
/*import net.neoforged.neoforge.common.data.ExistingFileHelper;
*///?}

import java.util.concurrent.CompletableFuture;

/**
 * 实体类型 tags datagen：把本 mod 的全部生物加入 Ad Astra 的宇宙环境生存 tag，
 * 使其在真空 / 极寒 / 极热 / 缺氧 / 酸雨等太空与异星环境下正常生存（不受环境伤害、不被减压吸出）。
 *
 * <p>tag 归属 {@code ad_astra} 命名空间（{@code data/ad_astra/tags/entity_type(s)/*.json}），
 * 原版 tag 合并语义（{@code replace=false}）会把本 mod 生物追加进 Ad Astra 已有条目，而非覆盖。
 * Ad Astra 仅 Forge 节点存在；NeoForge 无此依赖时这些 tag 惰性无害。
 */
public class TMEntityTypeTagsProvider extends EntityTypeTagsProvider {

    // Ad Astra 生存 tag（对应 earth.terrarium.adastra.common.tags.ModEntityTypeTags）
    private static final TagKey<EntityType<?>> CAN_SURVIVE_IN_SPACE = adAstra("can_survive_in_space");
    private static final TagKey<EntityType<?>> LIVES_WITHOUT_OXYGEN = adAstra("lives_without_oxygen");
    private static final TagKey<EntityType<?>> CAN_SURVIVE_EXTREME_COLD = adAstra("can_survive_extreme_cold");
    private static final TagKey<EntityType<?>> CAN_SURVIVE_EXTREME_HEAT = adAstra("can_survive_extreme_heat");
    private static final TagKey<EntityType<?>> CAN_SURVIVE_IN_ACID_RAIN = adAstra("can_survive_in_acid_rain");
    private static final TagKey<EntityType<?>> IGNORES_AIR_VORTEX = adAstra("ignores_air_vortex");

    public TMEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TitanMoon.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        surviveEverywhere(CAN_SURVIVE_IN_SPACE);
        surviveEverywhere(LIVES_WITHOUT_OXYGEN);
        surviveEverywhere(CAN_SURVIVE_EXTREME_COLD);
        surviveEverywhere(CAN_SURVIVE_EXTREME_HEAT);
        surviveEverywhere(CAN_SURVIVE_IN_ACID_RAIN);
        surviveEverywhere(IGNORES_AIR_VORTEX);
    }

    /** 把本 mod 的全部生物加入指定 Ad Astra 生存 tag。 */
    private void surviveEverywhere(TagKey<EntityType<?>> tag) {
        tag(tag).add(
                TMEntities.AERO_JELLY.get(),
                TMEntities.CRYO_SCAVENGER.get(),
                TMEntities.AMMONIA_STALKER.get(),
                TMEntities.CORRUPTED_PROBE.get(),
                TMEntities.THOLIN_WEAVER.get(),
                TMEntities.NATIVE_ICE_WORM.get(),
                TMEntities.METHANE_MIDGE.get(),
                TMEntities.HYDROTROPH_GRAZER.get());
    }

    private static TagKey<EntityType<?>> adAstra(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, TitanMoon.parse("ad_astra:" + path));
    }
}
