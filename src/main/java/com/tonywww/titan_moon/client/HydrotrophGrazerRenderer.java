package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.HydrotrophGrazer;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 氢营养蹒兽渲染器：复用原版猪模型（占位）。
 */
public class HydrotrophGrazerRenderer extends MobRenderer<HydrotrophGrazer, PigModel<HydrotrophGrazer>> {

    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/hydrotroph_grazer.png");

    public HydrotrophGrazerRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(HydrotrophGrazer entity) {
        return TEXTURE;
    }
}
