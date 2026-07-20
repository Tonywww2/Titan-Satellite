package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.CryoScavenger;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 冰硅甲虫的 GeckoLib 模型：绑定 Blockbench 导出的几何 / 贴图 / 动画资源。
 * <ul>
 *   <li>几何：{@code assets/titan_moon/geo/cryo_scavenger.geo.json}（identifier {@code geometry.cryo_scavenger}）</li>
 *   <li>贴图：{@code assets/titan_moon/textures/entity/cryo_scavenger.png}（深色硅碳躯体 + 冰晶白蓝棱角甲壳；512² 高清、128 UV 空间）</li>
 *   <li>动画：{@code assets/titan_moon/animations/cryo_scavenger.animation.json}（idle/walk/attack_melee/roll_*(蜷球冲撞三段)/hurt/death）</li>
 * </ul>
 */
public class CryoScavengerModel extends GeoModel<CryoScavenger> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/cryo_scavenger.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/cryo_scavenger.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/cryo_scavenger.animation.json");

    // NeoForge 4.9.2 将单参数 getModelResource/getTextureResource 标记 @Deprecated（推荐双参数重载），
    // 但它们仍是 abstract 必须覆写；Forge 4.8.4 未弃用。此处抑制弃用警告，两加载器通用。
    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(CryoScavenger animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(CryoScavenger animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CryoScavenger animatable) {
        return ANIMATION;
    }
}
