package com.tonywww.titan_satellite.client;

import com.tonywww.titan_satellite.TitanSatellite;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * 土卫六维度客户端特效：浓厚橙黄雾、无星空 / 日月（{@link SkyType#NONE}）、无云、平坦环境光，
 * 呈现托林大气的昏黄雾霭。经 Forge {@link RegisterDimensionSpecialEffectsEvent}
 * （客户端 <b>Mod 总线</b>）注册到 {@code titan_satellite:titan}——须与
 * {@code data/titan_satellite/dimension_type/titan.json} 的 {@code "effects"} 字段一致。
 *
 * <p>雾的具体颜色与浓度（可见距离）由 {@link FogHandler}（{@code ViewportEvent}）进一步细化。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TitanDimensionEffects extends DimensionSpecialEffects {

    /** 注册键，须与 dimension_type/titan.json 的 {@code "effects"} 一致。 */
    public static final ResourceLocation KEY = new ResourceLocation(TitanSatellite.MODID, "titan");

    /** 托林大气橙黄基调（线性 RGB）。 */
    private static final Vec3 FOG_TONE = new Vec3(0.65D, 0.45D, 0.19D);

    public TitanDimensionEffects() {
        // cloudLevel=NaN(无云)、hasGround=true、SkyType.NONE(无日月星，天空即雾色)、
        // forceBrightLightmap=false、constantAmbientLight=true(平坦环境光，营造无方向阳光的雾霭)
        super(Float.NaN, true, DimensionSpecialEffects.SkyType.NONE, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // 忽略群系雾色，统一橙黄，并随昼夜亮度轻微明暗。
        return FOG_TONE.scale(brightness * 0.9F + 0.1F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        // 不再让整个维度强制浓雾；浓雾仅由 FogHandler 在液态甲烷深渊群系逐帧施加，
        // 其余群系维持原版按视距渲染的普通雾。
        return false;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null; // 无日出 / 日落染色
    }

    @SubscribeEvent
    public static void onRegister(RegisterDimensionSpecialEffectsEvent event) {
        event.register(KEY, new TitanDimensionEffects());
    }
}
