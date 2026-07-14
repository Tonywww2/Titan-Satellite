package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import com.tonywww.titan_satellite.registry.TSFluids;
import com.tonywww.titan_satellite.registry.TSItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
//? if forge {
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
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

    /**
     * 流体世界渲染层：两种液体的源+流动 Fluid 设为 translucent；否则默认 SOLID → 液面渲染成不透明。
     * setRenderLayer 须主线程执行(enqueueWork)。两端逻辑一致，仅 FMLClientSetupEvent 包名不同(import 隔离)。
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(TSFluids.LIQUID_METHANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TSFluids.FLOWING_LIQUID_METHANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TSFluids.LIQUID_AMMONIA.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TSFluids.FLOWING_LIQUID_AMMONIA.get(), RenderType.translucent());
        });
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

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // 桶物品染色：neoforge:fluid_container 的液面层为 tintindex 1、本身不含颜色，需注册 ItemColor 才会上色。
        // NeoForge 同样提供 DynamicFluidContainerModel.Colors（取桶内流体 getTintColor）。
        event.register(new DynamicFluidContainerModel.Colors(),
                TSItems.LIQUID_METHANE_BUCKET.get(),
                TSItems.LIQUID_AMMONIA_BUCKET.get());
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
