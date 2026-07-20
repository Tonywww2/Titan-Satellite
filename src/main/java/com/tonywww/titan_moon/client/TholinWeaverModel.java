package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.TholinWeaver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 托林织体蛛的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/tholin_weaver.geo.json}（identifier {@code geometry.tholin_weaver}，25 骨骼、8 足两节腿）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/tholin_weaver.png}（深托林棕躯体 + 背部沙纹伪装脊 + 立体青眼簇；512² 高清、128 UV 空间）</li>
 *   <li>动画：{@code assets/titan_moon/animations/tholin_weaver.animation.json}（idle_buried/emerge/stalk/leap_pounce/attack_melee/spit_web/hurt/death）</li>
 * </ul>
 */
public class TholinWeaverModel extends GeoModel<TholinWeaver> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/tholin_weaver.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/tholin_weaver.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/tholin_weaver.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(TholinWeaver animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(TholinWeaver animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TholinWeaver animatable) {
        return ANIMATION;
    }
}
