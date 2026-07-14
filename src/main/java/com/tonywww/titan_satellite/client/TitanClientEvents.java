package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import com.tonywww.titan_satellite.registry.TSItems;
//? if forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.tonywww.titan_satellite.registry.TSFluidTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
*///?}

/**
 * 客户端 mod 总线事件：仅在 {@link Dist#CLIENT} 加载，注册实体渲染器。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//?} else {
/*@EventBusSubscriber(modid = TitanSatellite.MODID, value = Dist.CLIENT)
*///?}
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

    //? if forge {
    /**
     * 桶物品染色：forge:fluid_container 的液面层为 tintindex 1、本身不含颜色，需注册 ItemColor 才会上色。
     * Forge 现成实现 DynamicFluidContainerModel.Colors 会取桶内流体的 getTintColor。
     */
    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(),
                TSItems.LIQUID_METHANE_BUCKET.get(),
                TSItems.LIQUID_AMMONIA_BUCKET.get());
    }
    //?} else {
    /*@SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // NeoForge 无 FluidType.initializeClient，改由此注册流体客户端渲染（复用水贴图 + 甲烷/氨染色）。
        event.registerFluidType(fluidExt(0xFFE7E3B8, 0x99E7E3B8), TSFluidTypes.LIQUID_METHANE.get());
        event.registerFluidType(fluidExt(0xFFBAE8E4, 0x99BAE8E4), TSFluidTypes.LIQUID_AMMONIA.get());
    }

    private static IClientFluidTypeExtensions fluidExt(int itemColor, int worldColor) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return TitanSatellite.mcRl("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return TitanSatellite.mcRl("block/water_flow");
            }

            @Override
            public int getTintColor() {
                return itemColor;
            }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                return worldColor;
            }
        };
    }
    *///?}
}
