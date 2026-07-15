package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.CryoScavenger;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 冰硅甲虫渲染器：复用原版蜘蛛模型（占位）。
 */
public class CryoScavengerRenderer extends MobRenderer<CryoScavenger, SpiderModel<CryoScavenger>> {

    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/cryo_scavenger.png");

    public CryoScavengerRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(CryoScavenger entity) {
        return TEXTURE;
    }
}
