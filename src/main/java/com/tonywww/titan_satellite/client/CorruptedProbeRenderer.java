package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.entity.CorruptedProbe;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 失控探测器渲染器：复用原版史莱姆模型（占位）。
 */
public class CorruptedProbeRenderer extends MobRenderer<CorruptedProbe, SlimeModel<CorruptedProbe>> {

    private static final ResourceLocation TEXTURE = TitanSatellite.rl("textures/entity/corrupted_probe.png");

    public CorruptedProbeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CorruptedProbe entity) {
        return TEXTURE;
    }
}
