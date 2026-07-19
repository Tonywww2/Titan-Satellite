package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.MethaneMidge;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 甲烷微浮群的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/methane_midge.geo.json}（identifier {@code geometry.methane_midge}）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/methane_midge.png}（淡金气泡；发光雾走 AutoGlowingGeoLayer 读 *_glowmask）</li>
 *   <li>动画：{@code assets/titan_moon/animations/methane_midge.animation.json}（idle_float/drift/disturbed_scatter/hurt/death）</li>
 * </ul>
 */
public class MethaneMidgeModel extends GeoModel<MethaneMidge> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/methane_midge.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/methane_midge.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/methane_midge.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(MethaneMidge animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(MethaneMidge animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MethaneMidge animatable) {
        return ANIMATION;
    }
}
