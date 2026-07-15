package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.TholinWeaver;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 托林织体蜉渲染器：复用原版蜘蛛模型（占位）。
 */
public class TholinWeaverRenderer extends MobRenderer<TholinWeaver, SpiderModel<TholinWeaver>> {

    private static final ResourceLocation TEXTURE = TitanSatellite.rl("textures/entity/tholin_weaver.png");

    public TholinWeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(TholinWeaver entity) {
        return TEXTURE;
    }
}
