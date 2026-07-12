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
 * 土卫六维度雾特效（客户端 <b>Forge 总线</b> {@link ViewportEvent}）：把雾色改为橙黄，
 * 并压低终端可见距离形成浓雾（低能见度）。仅在相机处于 {@link TSDimensions#TITAN_LEVEL} 时生效。
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
    private static final float FOG_START = 72.0F;
    private static final float FOG_END = 128.0F;
    /** 深渊以外群系的淡雾近 / 远平面（格）——仅远处留一层橙黄薄霭，能见度大幅提高。 */
    private static final float THIN_FOG_START = 96.0F;
    private static final float THIN_FOG_END = 192.0F;
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
        // 仅压缩地形可见雾（FOG_TERRAIN）；天空雾保持默认，避免天空穹顶异常。
        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN && isTitan(event.getCamera())) {
            if (isMethaneAbyss(event.getCamera())) {
                // 液态甲烷深渊：维持原浓雾、低能见度。
                event.setNearPlaneDistance(FOG_START);
                event.setFarPlaneDistance(FOG_END);
            } else {
                // 深渊以外：淡雾，能见度大幅提高。
                event.setNearPlaneDistance(THIN_FOG_START);
                event.setFarPlaneDistance(THIN_FOG_END);
            }
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
