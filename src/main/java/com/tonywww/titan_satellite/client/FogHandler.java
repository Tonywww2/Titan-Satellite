package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 土卫六维度雾特效（客户端 <b>Forge 总线</b> {@link ViewportEvent}）：维度内统一橙黄雾色；
 * 浓雾（低能见度）仅在 {@code methane_abyss}（液态甲烷深渊）群系生效，其余群系维持原版按视距渲染的普通雾。
 *
 * <p>天空类型 / 无星空 / 基础雾色见 {@link TitanDimensionEffects}；本类负责逐帧的雾色与浓度。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, value = Dist.CLIENT)
public final class FogHandler {

    private FogHandler() {
    }

    private static final float FOG_R = 0.65F;
    private static final float FOG_G = 0.45F;
    private static final float FOG_B = 0.19F;
    /** 液态甲烷深渊（methane_abyss）浓雾近 / 远平面（格）——低能见度。 */
    private static final float FOG_START = 224.0F;
    private static final float FOG_END = 256.0F;
    /** 液态甲烷深渊群系 id（仅此群系维持浓雾）。 */
    private static final ResourceLocation METHANE_ABYSS =
            new ResourceLocation(TitanSatellite.MODID, "methane_abyss");

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (isTitan(event.getCamera())) {
            event.setRed(FOG_R);
            event.setGreen(FOG_G);
            event.setBlue(FOG_B);
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        // 仅在液态甲烷深渊压缩地形可见雾（FOG_TERRAIN）形成浓雾；其余群系不作覆盖，
        // 交由原版按视距渲染普通雾（配合 TitanDimensionEffects#isFoggyAt 返回 false，
        // 深渊以外不再有额外迷雾）。天空雾保持默认，避免天空穹顶异常。
        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN && isMethaneAbyss(event.getCamera())) {
            event.setNearPlaneDistance(FOG_START);
            event.setFarPlaneDistance(FOG_END);
            event.setCanceled(true);
        }
    }

    private static boolean isTitan(Camera camera) {
        Entity entity = camera.getEntity();
        return entity != null && entity.level().dimension() == TSDimensions.TITAN_LEVEL;
    }

    /** 相机所在处是否为液态甲烷深渊群系。 */
    private static boolean isMethaneAbyss(Camera camera) {
        Entity entity = camera.getEntity();
        return entity != null
                && entity.level().getBiome(BlockPos.containing(camera.getPosition())).is(METHANE_ABYSS);
    }
}
