package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.HydrotrophGrazer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 氢营养蹒兽渲染器（GeckoLib）：不透明皮甲，无发光层。
 * <p>模型按 Blockbench 像素建（16px = 1m），碰撞箱 0.9×1.0，故用默认缩放（1.0）；
 * 默认渲染类型 {@code entityCutoutNoCull}（不剔除背面——已在建模阶段清除共面 z-fighting）。
 */
public class HydrotrophGrazerRenderer extends GeoEntityRenderer<HydrotrophGrazer> {

    public HydrotrophGrazerRenderer(EntityRendererProvider.Context context) {
        super(context, new HydrotrophGrazerModel());
        this.shadowRadius = 0.5F;
    }
}

