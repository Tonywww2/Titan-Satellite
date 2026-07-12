package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.entity.CryoScavenger;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 冰硅甲虫渲染器（桩，复用原版蜘蛛模型）。真实模型/贴图由 M2 / PC-1 替换。
 */
public class CryoScavengerRenderer extends MobRenderer<CryoScavenger, SpiderModel<CryoScavenger>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/spider/spider.png");

    public CryoScavengerRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(CryoScavenger entity) {
        return TEXTURE;
    }
}
