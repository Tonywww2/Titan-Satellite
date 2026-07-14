package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.AeroJelly;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 甲烷浮游体渲染器：骨架阶段复用原版史莱姆模型与贴图。
 */
public class AeroJellyRenderer extends MobRenderer<AeroJelly, SlimeModel<AeroJelly>> {

    private static final ResourceLocation TEXTURE = TitanSatellite.rl("textures/entity/aero_jelly.png");

    public AeroJellyRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AeroJelly entity) {
        return TEXTURE;
    }
}
