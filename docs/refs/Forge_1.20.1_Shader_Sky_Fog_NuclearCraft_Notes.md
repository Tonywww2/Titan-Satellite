# Forge 1.20.1 Shader / 后处理 / 天空 / 雾 迁移参考（NuclearCraft-Neoteric）

本笔记面向 The Betweenlands 移植，目标是替代原 1.12 coremod 的渲染 hook（`PreRenderShadersHookTransformer` 等）与自定义天空/雾/rift/后处理管线。结论来源：

- 用户上传的详细参考：`nuclearcraft_neoteric_forge1201_shader_reference_ai.md`（基于 `igentuman/NuclearCraft-Neoteric` 的 `1.20` 分支逐文件核对，**版本为 Forge 1.20.1**，与 TBL 完全对齐）。
- Forge 1.20.x 官方事件源码（`RegisterShadersEvent`、`RenderLevelStageEvent`、`EntityRenderersEvent`）。
- 天空/雾的非 shader 部分另见 `docs/reference/Bumblezone_Client_Render_Particle_Vehicle_Case_Report.md` 第 1、4 节。

研究日期：2026-06-23

> 本文是面向 TBL 的提炼与映射。**完整逐文件细节（每个 shader json/vsh/fsh 字段、DistortShader 投影公式、PostChain 合成步骤、常见失败点）以上传的 `nuclearcraft_neoteric_forge1201_shader_reference_ai.md` 为准**，本文不重复抄录，只给迁移决策与链路。

## 1. 关键结论：coremod 渲染 hook 的 1.20.1 替代路径已确定

原 1.12 用 ASM coremod 在渲染管线里插入 `ClientHooks.onPreRenderShaders(partialTicks)` 来跑自定义后处理。1.20.1 Forge **不需要 coremod**，NuclearCraft 已验证的等价路径：

- **全屏后处理（屏幕空间扭曲/泛光/闪光）** → `PostChain` + 在 `RenderLevelStageEvent`（Forge event bus）的某个 stage 执行 `chain.process(...)` 并合成回主 framebuffer。
- **自定义世界几何 shader（自发光 billboard、rift 等）** → 自定义 `RenderType` + core `ShaderInstance`（`RegisterShadersEvent`，mod event bus）。
- **覆盖 vanilla shader（云/天空）** → 在 `assets/minecraft/shaders/core/<name>.*` 放替换文件 + 必要时 mixin 注入 vanilla renderer 设置新 uniform（NuclearCraft 的 `rendertype_clouds` + `LevelRendererCloudsMixin`）。

因此任务清单阶段 16.4 / 21 的 shader 替代方案**可以定案为：Forge event + PostChain + 自定义 RenderType（+ 必要时 Mixin）**，不再保留 coremod。

## 2. 两条事件总线（极易错，硬性约束）

来自 Forge 源码注释与 NuclearCraft 实测：

- `RegisterShadersEvent`：**mod event bus**，仅 logical client，是 `IModBusEvent`。用 `@Mod.EventBusSubscriber(value=Dist.CLIENT, bus=Bus.MOD)`。提供 `registerShader(ShaderInstance, Consumer<ShaderInstance> onLoaded)` 和 `getResourceProvider()`。
- `RenderLevelStageEvent`：**Forge runtime event bus**（不是 IModBusEvent），仅 logical client。NuclearCraft 用到 `AFTER_CUTOUT_BLOCKS`（后处理）与 `AFTER_TRANSLUCENT_BLOCKS`（透明 billboard 批绘）。
- `EntityRenderersEvent.RegisterRenderers`：mod event bus，注册 entity/BER。

放错总线是最常见失败点。

## 3. 自定义 core shader + RenderType（rift / 自发光 / 半透明 billboard）

适用 TBL：rift 渲染、需要自发光或特殊混合的 BER/实体渲染。最小链路：

1. 资源：`assets/thebetweenlands/shaders/core/<id>.json|.vsh|.fsh`。JSON 的 `attributes` 必须与 `VertexFormat` 对齐（如 `POSITION_COLOR_TEX` → `["Position","Color","UV0"]`）。
2. 注册（mod bus）：

```java
@Mod.EventBusSubscriber(modid = TheBetweenlands.ID, value = Dist.CLIENT, bus = Bus.MOD)
public final class BLShaders {
    public static final ShaderTracker RIFT = new ShaderTracker(); // 实现 Supplier<ShaderInstance>
    @SubscribeEvent
    public static void onRegister(RegisterShadersEvent e) throws IOException {
        e.registerShader(new ShaderInstance(e.getResourceProvider(),
            new ResourceLocation(TheBetweenlands.ID, "rendertype_rift"),
            DefaultVertexFormat.POSITION_COLOR_TEX), RIFT::setInstance);
    }
}
```

3. `ShaderTracker` 持有 `ShaderInstance` 并暴露 `RenderStateShard.ShaderStateShard shard = new ShaderStateShard(this)`。
4. 自定义 `RenderType`（仿 `NCRenderType.BLACKHOLE`）：`CompositeState.builder().setShaderState(BLShaders.RIFT.shard).setTextureState(...).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(MAIN_TARGET)...create("bl_rift", POSITION_COLOR_TEX, QUADS, 256, true, true, state)`，建议 `Util.memoize` 按 texture 复用。
5. 透明绘制不要在 BER 里直接画，参考 NuclearCraft 的延迟队列：BER 每帧只更新数据，统一在 `RenderLevelStageEvent.AFTER_TRANSLUCENT_BLOCKS` 批绘 + `endBatch`，并按距离从远到近排序，绘制前 `matrix.translate(-camX,-camY,-camZ)`。

## 4. 全屏后处理（屏幕空间效果，替代旧 shader hook）

适用 TBL：环境事件视觉（blood sky、rift 扭曲、dense fog 后处理等若需要屏幕空间效果）。链路：

1. 资源：`assets/thebetweenlands/shaders/post/<post>.json` + `program/<prog>.json|.vsh|.fsh`。
   - post pass `"name": "thebetweenlands:<prog>"` → program 放 `assets/thebetweenlands/shaders/program/`。
   - post pass `"name": "<prog>"`（无命名空间）→ program 必须放 `assets/minecraft/shaders/program/`（NuclearCraft 黑洞即此布局）。**两者不可混用。**
2. 在 `RegisterShadersEvent` 里创建 `PostChain`：

```java
PostChain chain = new PostChain(mc.getTextureManager(), mc.getResourceManager(),
    mc.getMainRenderTarget(), new ResourceLocation(TheBetweenlands.ID, "shaders/post/<post>.json"));
chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
```

3. 每帧（`RenderLevelStageEvent`，Forge bus）：检测窗口尺寸变化 → `chain.resize(...)`；设置 uniform（`effect.getUniform("X").set(...)`）；`chain.process(mc.getFrameTime())`；再绑定主 framebuffer 写 + 取最后一 pass `outTarget` 读，画全屏 quad 合成回来。绘制前后正确管理 `depthMask`/`blend`/`depthFunc(515)`，结束恢复。
4. 世界坐标→屏幕 UV：先减相机坐标，再乘 view matrix（`event.getPoseStack().last().pose()`）、projection matrix（`RenderSystem.getProjectionMatrix()`），透视除 w，`uv = xy*0.5+0.5`，`depth = (z+1)*0.5`。可见性判 `z∈(-1,1)` 且 uv 在 `[-margin,1+margin]`。
5. uniform 若只在 Java 设却没在 GLSL 实际参与输出，可能被编译器优化掉——NuclearCraft 用 `dummyOut` 引用保活。

> 完整字段、blend 设置、合成顺序、10 条常见失败点见上传文档第 7/8/13/14/15 章。

## 5. 覆盖 vanilla 云/天空 shader（可选，配合 Mixin）

NuclearCraft 用 `assets/minecraft/shaders/core/rendertype_clouds.*` 替换 vanilla 云 shader，并用 `LevelRendererCloudsMixin` 注入 `LevelRenderer.renderClouds`，在 `VertexBuffer.drawWithShader` 前对 `RenderSystem.getShader()` 设置新增 uniform。TBL 如需在 vanilla 天空/云上叠加自定义效果可仿此，但属于侵入式（Mixin），优先级低于第 3、4 节路线。Mixin 格式见 `docs/reference/Forge_1.20.1_Mixin_Setup_Notes.md`。

## 6. 非 shader 的天空 / 雾（优先做，最稳）

不是所有 TBL 天空/雾都需要 shader。优先用原生事件（无 coremod、无 mixin）：

- 雾色/距离/形状：`ViewportEvent.ComputeFogColor` / `ViewportEvent.RenderFog`（Forge）。
- 维度天空类型、云高、雾色基调：继承 `DimensionSpecialEffects` 并用 `RegisterDimensionSpecialEffectsEvent` 注册。

详见 `docs/reference/Bumblezone_Client_Render_Particle_Vehicle_Case_Report.md` 第 1 节。TBL 的 `FogHandler` 与基础天空迁移走这条；只有真正需要屏幕空间扭曲/泛光时才上第 4 节的 PostChain。

## 7. NuclearCraft 覆盖了 TBL 的哪些需求 / 仍需注意

覆盖（可直接照搬模式）：
- ✅ 全屏后处理管线（PostChain + RenderLevelStageEvent + 合成）——替代 coremod shader hook。
- ✅ 自定义 core shader + RenderType + 延迟透明批绘——用于 rift / 自发光。
- ✅ 覆盖 vanilla shader + mixin 设 uniform——用于云/天空叠加。
- ✅ BER 注册两种写法（`EntityRenderersEvent` 或 `FMLClientSetupEvent.enqueueWork` + `BlockEntityRenderers.register`）。

仍需注意 / 未直接给出的：
- ⚠️ NuclearCraft **没有完整的“自定义天空穹顶/星空/rift 几何天空”渲染器**（它只覆盖云 shader）。TBL 的 `BLSkyRenderer`（自定义天空 + rift）需要用 `RenderLevelStageEvent`（如 `AFTER_SKY` 之类阶段）自绘几何，或在 `DimensionSpecialEffects` 中覆写天空渲染相关行为。这部分要自研，但 NuclearCraft 的“RenderType + 顶点写入 + 相机相对坐标”模式可复用。
- ⚠️ stencil buffer 用法 NuclearCraft 未涉及；TBL 原 stencil helper 若仍需要，需另查 Forge framebuffer/stencil 资料。
- ⚠️ `PostChain` 的 `resize` 用 `width+height` 检测尺寸变化（不唯一），照搬即可，若改独立缓存需自测。

**总体判断：NuclearCraft 充分覆盖了 TBL 最关键的“去 coremod + 全屏后处理 + 自定义渲染管线”需求；缺的是“纯自定义天空穹顶几何渲染器”这一块，需在其模式基础上自研。** 若用户有现成的 1.20.1 Forge 自定义天空穹顶/星空渲染案例，可进一步补充。

## 8. 对应任务清单

- 阶段 16.4（Fog/sky/shader）：非 shader 雾/天空走第 6 节；rift 渲染走第 3 节；屏幕空间后处理走第 4 节。
- 阶段 21（Coremod 替代）：`PreRenderShadersHookTransformer` → 第 4 节 PostChain + `RenderLevelStageEvent`，**确认可去 coremod**。
- 阶段 16.2（renderer 迁移）：BER/实体 renderer 注册见第 3 节末与上传文档第 13.5 节。

## 9. 参考

- 上传文档（主依据，逐文件细节）：`nuclearcraft_neoteric_forge1201_shader_reference_ai.md`
- 仓库：`https://github.com/igentuman/NuclearCraft-Neoteric`（分支 `1.20`）
- Forge 事件源码：`RegisterShadersEvent`、`RenderLevelStageEvent`、`EntityRenderersEvent`（MinecraftForge `1.20.x` 分支）
- 配套：`docs/reference/Bumblezone_Client_Render_Particle_Vehicle_Case_Report.md`（天空/雾非 shader 部分）、`docs/reference/Forge_1.20.1_Mixin_Setup_Notes.md`（覆盖 vanilla shader 时的 mixin）
