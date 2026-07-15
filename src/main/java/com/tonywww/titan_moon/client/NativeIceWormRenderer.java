package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tonywww.titan_moon.entity.NativeIceWorm;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 原生冰虫渲染器：复用放大的原版蟹虫模型（占位）。
 */
public class NativeIceWormRenderer extends MobRenderer<NativeIceWorm, SilverfishModel<NativeIceWorm>> {

    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/native_ice_worm.png");

    public NativeIceWormRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel<>(context.bakeLayer(ModelLayers.SILVERFISH)), 0.6F);
    }

    @Override
    protected void scale(NativeIceWorm entity, PoseStack pose, float partialTick) {
        pose.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(NativeIceWorm entity) {
        return TEXTURE;
    }
}
