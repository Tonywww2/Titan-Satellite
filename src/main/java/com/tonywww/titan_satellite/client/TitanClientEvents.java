package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
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
        event.registerEntityRenderer(TSEntities.NATIVE_ICE_WORM.get(), NativeIceWormRenderer::new);
        event.registerEntityRenderer(TSEntities.METHANE_MIDGE.get(), MethaneMidgeRenderer::new);
        event.registerEntityRenderer(TSEntities.HYDROTROPH_GRAZER.get(), HydrotrophGrazerRenderer::new);
    }

    /**
     * 桶物品染色：forge:fluid_container 的液面层为 tintindex 1、本身不含颜色，需注册 ItemColor 才会上色。
     * Forge 现成实现 {@link DynamicFluidContainerModel.Colors} 会取桶内流体的 getTintColor。
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(),
                TSItems.LIQUID_METHANE_BUCKET.get(),
                TSItems.LIQUID_AMMONIA_BUCKET.get());
    }
}
