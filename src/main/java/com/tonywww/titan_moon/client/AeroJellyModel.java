package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.AeroJelly;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 甲烷浮游体的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/aero_jelly.geo.json}（identifier {@code geometry.aero_jelly}）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/aero_jelly.png}（乳黄膜质气球；储气囊发光走 AutoGlowingGeoLayer 读 *_glowmask）</li>
 *   <li>动画：{@code assets/titan_moon/animations/aero_jelly.animation.json}
 *       （idle_hover/swim_drift/filter_feed/flee/look_at_player/hurt/death）</li>
 * </ul>
 */
public class AeroJellyModel extends GeoModel<AeroJelly> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/aero_jelly.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/aero_jelly.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/aero_jelly.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(AeroJelly animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(AeroJelly animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AeroJelly animatable) {
        return ANIMATION;
    }
}
