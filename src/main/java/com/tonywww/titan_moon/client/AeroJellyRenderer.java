package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.AeroJelly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * 甲烷浮游体渲染器（GeckoLib）：半透明乳黄膜质气球 + 内部发光储气囊。
 * <ul>
 *   <li>{@link #getRenderType} → {@code entityTranslucent}：膜体半透明，透出内部储气囊光。</li>
 *   <li>{@link AutoGlowingGeoLayer}：读取 {@code aero_jelly_glowmask.png}，储气囊区域全亮发光（emissive），如纸灯笼。</li>
 * </ul>
 */
public class AeroJellyRenderer extends GeoEntityRenderer<AeroJelly> {

    public AeroJellyRenderer(EntityRendererProvider.Context context) {
        super(context, new AeroJellyModel());
        this.shadowRadius = 0.5F;
        // 模型按 Blockbench 像素建（伞宽约 14px、整体高约 22px），缩放到 ~1.2m 视觉。可按需微调。
        this.scaleWidth = 0.9F;
        this.scaleHeight = 0.9F;
        // 内部储气囊发光：AutoGlowingGeoLayer 读取 <texture>_glowmask.png 做 emissive 全亮渲染。
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    /** 半透明膜体：用 entityTranslucent 渲染类型，使内部储气囊光透出。 */
    @Override
    public RenderType getRenderType(AeroJelly animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
