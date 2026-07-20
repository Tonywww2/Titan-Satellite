package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.AmmoniaStalker;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 氨泉掠食者渲染器（GeckoLib）：深蓝灰滑腻躯体 + 氨青霜花 / 发光胸泵 / 骨白利爪·脊刺·獠牙
 * （512² 高清贴图、128 UV 空间、按面 UV）。
 * <p>渲染类型默认 {@code entityCutoutNoCull}（不剔除背面——建模阶段已清除共面 z-fighting）；
 * 阴影半径 0.6（半直立掠食者体型）。
 */
public class AmmoniaStalkerRenderer extends GeoEntityRenderer<AmmoniaStalker> {

    public AmmoniaStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new AmmoniaStalkerModel());
        this.shadowRadius = 0.6F;
    }
}
