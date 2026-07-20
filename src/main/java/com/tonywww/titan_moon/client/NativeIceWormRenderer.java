package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.NativeIceWorm;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 原生冰虫渲染器（GeckoLib）：约 2.4m 纺锤形分节钻地环节虫 Boss——蓝白甲烷冰甲 / 深色托林肉、
 * 背脊与侧向冰晶尖刺、前端多环冰颚巨口。
 * <p>渲染类型默认 entityCutoutNoCull（不剔除背面——建模阶段已清除共面 z-fighting）；
 * 阴影半径 1.2（大型 Boss 体型）。
 */
public class NativeIceWormRenderer extends GeoEntityRenderer<NativeIceWorm> {

    public NativeIceWormRenderer(EntityRendererProvider.Context context) {
        super(context, new NativeIceWormModel());
        this.shadowRadius = 1.2F;
    }
}
