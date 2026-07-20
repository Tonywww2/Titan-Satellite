package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.AmmoniaStalker;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for the Ammonia-Stalker: binds the Blockbench-exported geometry, texture and animation.
 */
public class AmmoniaStalkerModel extends GeoModel<AmmoniaStalker> {

    private static final ResourceLocation MODEL = TitanMoon.rl("geo/ammonia_stalker.geo.json");
    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/ammonia_stalker.png");
    private static final ResourceLocation ANIMATION = TitanMoon.rl("animations/ammonia_stalker.animation.json");

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getModelResource(AmmoniaStalker animatable) {
        return MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getTextureResource(AmmoniaStalker animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AmmoniaStalker animatable) {
        return ANIMATION;
    }
}
