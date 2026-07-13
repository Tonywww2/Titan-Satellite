package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端 mod 总线事件：仅在 {@link Dist#CLIENT} 加载，注册实体渲染器。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TitanClientEvents {

    private TitanClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TSEntities.AERO_JELLY.get(), AeroJellyRenderer::new);
        event.registerEntityRenderer(TSEntities.CRYO_SCAVENGER.get(), CryoScavengerRenderer::new);
        event.registerEntityRenderer(TSEntities.AMMONIA_STALKER.get(), AmmoniaStalkerRenderer::new);
        event.registerEntityRenderer(TSEntities.CORRUPTED_PROBE.get(), CorruptedProbeRenderer::new);
        event.registerEntityRenderer(TSEntities.THOLIN_WEAVER.get(), TholinWeaverRenderer::new);
    }
}
