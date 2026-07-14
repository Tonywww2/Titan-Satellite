package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.AmmoniaStalker;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 氨泉掠食者渲染器（桩，复用原版蠹虫模型）。真实模型/贴图由 M2 / PC-2 替换。
 */
public class AmmoniaStalkerRenderer extends MobRenderer<AmmoniaStalker, HumanoidModel<AmmoniaStalker>> {

    private static final ResourceLocation TEXTURE = TitanSatellite.rl("textures/entity/ammonia_stalker.png");

    public AmmoniaStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AmmoniaStalker entity) {
        return TEXTURE;
    }
}
