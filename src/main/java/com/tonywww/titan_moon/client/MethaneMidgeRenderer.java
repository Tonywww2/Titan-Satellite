package com.tonywww.titan_moon.client;

import com.tonywww.titan_moon.entity.MethaneMidge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * 甲烷微浮群渲染器（GeckoLib）：半透明气泡 + 内部发光雾。
 * <ul>
 *   <li>{@link #getRenderType} → {@code entityTranslucent}：气泡半透明，透出内部光雾。</li>
 *   <li>{@link AutoGlowingGeoLayer}：读取 {@code methane_midge_glowmask.png}，遮罩区域全亮发光（emissive）。</li>
 * </ul>
 */
public class MethaneMidgeRenderer extends GeoEntityRenderer<MethaneMidge> {

    public MethaneMidgeRenderer(EntityRendererProvider.Context context) {
        super(context, new MethaneMidgeModel());
        this.shadowRadius = 0.15F;
        // 模型按 Blockbench 像素建（气泡直径约 8px ≈ 0.5m），缩放到 ~0.3m 视觉（生物碰撞箱 0.3）。可按需微调。
        this.scaleWidth = 0.35F;
        this.scaleHeight = 0.35F;
        // 内部发光雾：AutoGlowingGeoLayer 读取 <texture>_glowmask.png 做 emissive 全亮渲染。
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    /** 半透明气泡：用 entityTranslucent 渲染类型，使内部光雾透出。 */
    @Override
    public RenderType getRenderType(MethaneMidge animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}

