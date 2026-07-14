package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tonywww.titan_satellite.entity.MethaneMidge;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 甲烷微浮群渲染器：缩小的原版史莱姆模型占位（真实模型/贴图待美术）。
 */
public class MethaneMidgeRenderer extends MobRenderer<MethaneMidge, SlimeModel<MethaneMidge>> {

    private static final ResourceLocation TEXTURE = TitanSatellite.rl("textures/entity/methane_midge.png");

    public MethaneMidgeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.15F);
    }

    @Override
    protected void scale(MethaneMidge entity, PoseStack pose, float partialTick) {
        pose.scale(0.3F, 0.3F, 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(MethaneMidge entity) {
        return TEXTURE;
    }
}
