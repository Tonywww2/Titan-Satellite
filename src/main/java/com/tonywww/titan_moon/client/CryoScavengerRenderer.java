package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.CryoScavenger;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 冰硅甲虫渲染器（GeckoLib）：深色硅碳躯体 + 冰晶白蓝棱角甲壳（512² 高清贴图、128 UV 空间）。
 * <p>模型按 Blockbench 像素建（16px = 1m），低伏甲虫状；默认缩放（1.0），
 * 渲染类型 {@code entityCutoutNoCull}（不剔除背面——建模阶段已清除共面 z-fighting）。
 */
public class CryoScavengerRenderer extends GeoEntityRenderer<CryoScavenger> {

    public CryoScavengerRenderer(EntityRendererProvider.Context context) {
        super(context, new CryoScavengerModel());
        this.shadowRadius = 0.55F;
    }
}
