package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.entity.AmmoniaStalker;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 氨泉掠食者渲染器：复用原版人形（僵尸）模型（占位）。
 */
public class AmmoniaStalkerRenderer extends MobRenderer<AmmoniaStalker, HumanoidModel<AmmoniaStalker>> {

    private static final ResourceLocation TEXTURE = TitanMoon.rl("textures/entity/ammonia_stalker.png");

    public AmmoniaStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AmmoniaStalker entity) {
        return TEXTURE;
    }
}
