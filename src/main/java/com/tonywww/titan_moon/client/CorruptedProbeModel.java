package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.CorruptedProbe;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 失控探测器的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/corrupted_probe.geo.json}（identifier {@code geometry.corrupted_probe}）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/corrupted_probe.png}（腐蚀金属灰机体 + 发光蓝镜头/激光口；512² 高清、128 UV 空间）</li>
 *   <li>动画：{@code assets/titan_moon/animations/corrupted_probe.animation.json}（idle_hover/drift_patrol/charge-fire-cooldown/attack_melee/hurt/death）</li>
 * </ul>
 */
public class CorruptedProbeModel extends GeoModel<CorruptedProbe> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/corrupted_probe.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/corrupted_probe.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/corrupted_probe.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(CorruptedProbe animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(CorruptedProbe animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CorruptedProbe animatable) {
        return ANIMATION;
    }
}
