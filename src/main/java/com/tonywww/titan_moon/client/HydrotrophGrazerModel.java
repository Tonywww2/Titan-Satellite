package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.HydrotrophGrazer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 氢营养蹒兽的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/hydrotroph_grazer.geo.json}（identifier {@code geometry.hydrotroph_grazer}）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/hydrotroph_grazer.png}（霜灰掺赭黄皮甲、背结白霜；128²）</li>
 *   <li>动画：{@code assets/titan_moon/animations/hydrotroph_grazer.animation.json}（idle/walk/graze/panic/hurt/death/look_*）</li>
 * </ul>
 */
public class HydrotrophGrazerModel extends GeoModel<HydrotrophGrazer> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/hydrotroph_grazer.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/hydrotroph_grazer.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/hydrotroph_grazer.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(HydrotrophGrazer animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(HydrotrophGrazer animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HydrotrophGrazer animatable) {
        return ANIMATION;
    }
}
