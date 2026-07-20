package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.TholinWeaver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 托林织体蛛渲染器（GeckoLib）：深托林棕八足蛛形猎手，背结沙纹伪装脊、立体青眼簇、腿生硬刺。
 * <p>展开约 1.1m，模型按 Blockbench 像素建（16px = 1m），用默认缩放（1.0）；
 * 渲染类型 {@code entityCutoutNoCull}（不剔除背面——建模阶段已用共面审计清除 z-fighting）。
 */
public class TholinWeaverRenderer extends GeoEntityRenderer<TholinWeaver> {

    public TholinWeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new TholinWeaverModel());
        this.shadowRadius = 0.6F;
    }
}
