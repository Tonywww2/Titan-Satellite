package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.CorruptedProbe;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 失控探测器渲染器（GeckoLib）：腐蚀金属灰椭球机体 + 发光蓝镜头/激光口（512² 高清贴图、128 UV 空间）。
 * <p>模型按 Blockbench 像素建（16px = 1m），悬浮无人机；默认缩放（1.0），
 * 渲染类型 {@code entityCutoutNoCull}（不剔除背面——建模阶段已按面 UV 化并清除共面 z-fighting）。
 */
public class CorruptedProbeRenderer extends GeoEntityRenderer<CorruptedProbe> {

    public CorruptedProbeRenderer(EntityRendererProvider.Context context) {
        super(context, new CorruptedProbeModel());
        this.shadowRadius = 0.5F;
    }
}
