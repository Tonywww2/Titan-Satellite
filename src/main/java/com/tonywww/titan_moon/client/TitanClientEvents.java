package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.TitanMoon;
import com.tonywww.titan_moon.registry.TMEntities;
import com.tonywww.titan_moon.registry.TMFluids;
import com.tonywww.titan_moon.registry.TMItems;
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
import com.tonywww.titan_moon.registry.TMFluidTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
*///?}

/**
 * 客户端 mod 总线事件：仅在 {@link Dist#CLIENT} 加载，注册实体渲染器。
 */
//? if forge {
@Mod.EventBusSubscriber(modid = TitanMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
//?} else {
/*@EventBusSubscriber(modid = TitanMoon.MODID, value = Dist.CLIENT)
*///?}
public final class TitanClientEvents {

    private TitanClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TMEntities.AERO_JELLY.get(), AeroJellyRenderer::new);
        event.registerEntityRenderer(TMEntities.CRYO_SCAVENGER.get(), CryoScavengerRenderer::new);
        event.registerEntityRenderer(TMEntities.AMMONIA_STALKER.get(), AmmoniaStalkerRenderer::new);
        event.registerEntityRenderer(TMEntities.CORRUPTED_PROBE.get(), CorruptedProbeRenderer::new);
        event.registerEntityRenderer(TMEntities.THOLIN_WEAVER.get(), TholinWeaverRenderer::new);
        event.registerEntityRenderer(TMEntities.NATIVE_ICE_WORM.get(), NativeIceWormRenderer::new);
        event.registerEntityRenderer(TMEntities.METHANE_MIDGE.get(), MethaneMidgeRenderer::new);
        event.registerEntityRenderer(TMEntities.HYDROTROPH_GRAZER.get(), HydrotrophGrazerRenderer::new);
    }

    /**
     * 流体世界渲染层：两种液体的源+流动 Fluid 设为 translucent；否则默认 SOLID → 液面渲染成不透明。
     * setRenderLayer 须主线程执行(enqueueWork)。两端逻辑一致，仅 FMLClientSetupEvent 包名不同(import 隔离)。
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(TMFluids.LIQUID_METHANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TMFluids.FLOWING_LIQUID_METHANE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TMFluids.LIQUID_AMMONIA.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TMFluids.FLOWING_LIQUID_AMMONIA.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TMFluids.LIQUID_HYDROGEN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(TMFluids.FLOWING_LIQUID_HYDROGEN.get(), RenderType.translucent());
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
                TMItems.LIQUID_METHANE_BUCKET.get(),
                TMItems.LIQUID_AMMONIA_BUCKET.get(),
                TMItems.LIQUID_HYDROGEN_BUCKET.get());
    }
    //?} else {
    /*@SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // NeoForge 无 FluidType.initializeClient，改由此注册流体客户端渲染（复用水贴图 + 甲烷/氨染色）。
        // 色值/贴图统一取自 TMFluidTypes，避免与 Forge 分支重复维护。
        event.registerFluidType(fluidExt(TMFluidTypes.COLOR_METHANE), TMFluidTypes.LIQUID_METHANE.get());
        event.registerFluidType(fluidExt(TMFluidTypes.COLOR_AMMONIA), TMFluidTypes.LIQUID_AMMONIA.get());
        event.registerFluidType(fluidExt(TMFluidTypes.COLOR_HYDROGEN), TMFluidTypes.LIQUID_HYDROGEN.get());
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        // 桶物品染色：neoforge:fluid_container 的液面层为 tintindex 1、本身不含颜色，需注册 ItemColor 才会上色。
        // NeoForge 同样提供 DynamicFluidContainerModel.Colors（取桶内流体 getTintColor）。
        event.register(new DynamicFluidContainerModel.Colors(),
                TMItems.LIQUID_METHANE_BUCKET.get(),
                TMItems.LIQUID_AMMONIA_BUCKET.get(),
                TMItems.LIQUID_HYDROGEN_BUCKET.get());
    }

    private static IClientFluidTypeExtensions fluidExt(int rgb) {
        final int itemColor = 0xFF000000 | (rgb & 0xFFFFFF);                        // 桶物品用不透明色
        final int worldColor = (TMFluidTypes.WORLD_ALPHA << 24) | (rgb & 0xFFFFFF); // 世界液面半透明
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return TMFluidTypes.STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return TMFluidTypes.FLOWING_TEXTURE;
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
