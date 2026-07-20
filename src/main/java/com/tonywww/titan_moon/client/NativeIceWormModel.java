package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.NativeIceWorm;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 原生冰虫的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：geo/native_ice_worm.geo.json（identifier geometry.native_ice_worm；按面 UV，无退化面）</li>
 *   <li>贴图：textures/entity/native_ice_worm.png（蓝白甲烷冰甲 + 深色托林肉 + 冰晶脊刺/侧棘、多环冰颚；512 高清、128 UV 空间）</li>
 *   <li>动画：animations/native_ice_worm.animation.json（idle_coiled/slither/awaken_erupt/attack_melee/leap/spit_iceshards/hurt/death）</li>
 * </ul>
 */
public class NativeIceWormModel extends GeoModel<NativeIceWorm> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/native_ice_worm.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/native_ice_worm.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/native_ice_worm.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(NativeIceWorm animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(NativeIceWorm animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(NativeIceWorm animatable) {
        return ANIMATION;
    }
}
