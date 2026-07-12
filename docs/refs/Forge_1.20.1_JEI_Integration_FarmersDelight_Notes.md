# Forge 1.20.1 JEI 集成参考：Farmer's Delight 研究笔记

来源：`vectorwing/FarmersDelight` 的 **`1.20` 分支**（Minecraft 1.20.1 + Forge）。目标项目只处理 JEI 兼容（已确认），本笔记作为 The Betweenlands 机器配方 JEI 显示的模板。

研究日期：2026-06-23

> JEI 1.20.1 的 API 与 1.12 完全不同：`IRecipeCategory`、`IRecipeLayoutBuilder`、`IRecipeSlotsView`、`RecipeType<T>` 都是新体系。本笔记基于 JEI 1.20.1 Forge API（FD 1.20 分支使用）。

## 1. 总体结构

JEI 集成由三块组成：

1. `@JeiPlugin` 主类（`implements IModPlugin`）：注册 categories、recipes、catalysts、GUI 点击区、配方传输。
2. 每个机器一个 `IRecipeCategory<T>`：定义标题、图标、背景、槽位布局、绘制、tooltip。
3. JEI 侧 `RecipeType<T>` 常量（`mezz.jei.api.recipe.RecipeType`，注意与 Minecraft 的 `RecipeType` 不是同一个类）。

FD 对应文件：

- `integration/jei/JEIPlugin.java`
- `integration/jei/FDRecipeTypes.java`
- `integration/jei/category/CookingRecipeCategory.java`
- `integration/jei/FDRecipes.java`（从 `RecipeManager` 收集配方实例供 JEI 显示）

## 2. JEI RecipeType 常量

```java
public final class FDRecipeTypes {
    public static final RecipeType<CookingPotRecipe> COOKING =
        RecipeType.create(FarmersDelight.MODID, "cooking", CookingPotRecipe.class);
    public static final RecipeType<CuttingBoardRecipe> CUTTING =
        RecipeType.create(FarmersDelight.MODID, "cutting", CuttingBoardRecipe.class);
}
```

`mezz.jei.api.recipe.RecipeType.create(namespace, path, recipeClass)`。这个 `RecipeType` 是 JEI 用来索引 category 与 recipe 实例的键，和第 1 篇笔记里 Minecraft 的 `RecipeType` 概念不同但通常一一对应。

## 3. @JeiPlugin 主类

```java
@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation(FarmersDelight.MODID, "jei_plugin");
    @Override public ResourceLocation getPluginUid() { return ID; }

    // 1) 注册 category（需要 IGuiHelper 构建 drawable）
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper gui = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new CookingRecipeCategory(gui));
        registry.addRecipeCategories(new CuttingRecipeCategory(gui));
    }

    // 2) 把实际配方实例塞给对应 category
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        FDRecipes modRecipes = new FDRecipes();
        registration.addRecipes(FDRecipeTypes.COOKING, modRecipes.getCookingPotRecipes());
        registration.addRecipes(FDRecipeTypes.CUTTING, modRecipes.getCuttingBoardRecipes());
        // 也可以补充 vanilla 类型、配方说明：
        registration.addRecipes(RecipeTypes.CRAFTING, modRecipes.getSpecialCraftingRecipes());
        registration.addIngredientInfo(new ItemStack(ModItems.WHEAT_DOUGH.get()),
            VanillaTypes.ITEM_STACK, TextUtils.JEI("info.dough"));   // “物品信息”页
    }

    // 3) 催化剂：哪个方块/物品对应哪个 category（点机器能查它的配方）
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.COOKING_POT.get()), FDRecipeTypes.COOKING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.STOVE.get()), RecipeTypes.CAMPFIRE_COOKING);
    }

    // 4) 机器 GUI 上点某区域直接跳到配方
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(CookingPotScreen.class, 89, 25, 24, 17, FDRecipeTypes.COOKING);
    }

    // 5) 配方传输：JEI “+” 一键填充机器输入槽
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            CookingPotMenu.class, ModMenuTypes.COOKING_POT.get(),
            FDRecipeTypes.COOKING, /*recipeSlotStart*/0, /*recipeSlotCount*/6,
            /*inventorySlotStart*/9, /*inventorySlotCount*/36);
    }
}
```

`@JeiPlugin` 注解让 JEI 自动发现该类，**不需要在 mods.toml 注册**。

> 服务端安全：JEIPlugin 及其引用的 `Screen`/category 类都是客户端类。JEI 只在客户端加载该插件，所以这些类不会在服务端被加载——但仍要确保 common 侧代码不 import 它们。

## 4. IRecipeCategory 实现

`CookingRecipeCategory implements IRecipeCategory<CookingPotRecipe>`：

### 4.1 构造：用 IGuiHelper 建 drawable

```java
public CookingRecipeCategory(IGuiHelper helper) {
    title = TextUtils.JEI("cooking");
    ResourceLocation widgetBg = new ResourceLocation(FarmersDelight.MODID, "textures/gui/jei/cooking_pot.png");
    ResourceLocation guiTex  = new ResourceLocation(FarmersDelight.MODID, "textures/gui/cooking_pot.png");
    background = helper.createDrawable(widgetBg, 0, 0, 116, 56);
    icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.COOKING_POT.get()));
    arrow = helper.drawableBuilder(guiTex, 176, 15, 24, 17)
                  .buildAnimated(200, IDrawableAnimated.StartDirection.LEFT, false);  // 动画箭头
    // heatIndicator / timeIcon / expIcon 同理 createDrawable(...)
}
```

### 4.2 元数据方法

```java
@Override public RecipeType<CookingPotRecipe> getRecipeType() { return FDRecipeTypes.COOKING; }
@Override public Component getTitle() { return title; }
@Override public int getWidth()  { return 116; }
@Override public int getHeight() { return 56; }
@Override public IDrawable getIcon() { return icon; }
```

### 4.3 setRecipe：声明槽位与内容（1.20.1 新 builder API）

```java
@Override
public void setRecipe(IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focus) {
    NonNullList<Ingredient> ings = recipe.getIngredients();
    ItemStack result = RecipeUtils.getResultItem(recipe);
    ItemStack container = recipe.getOutputContainer();
    int slot = 18;
    for (int row = 0; row < 2; ++row)
      for (int col = 0; col < 3; ++col) {
        int idx = row * 3 + col;
        if (idx < ings.size())
            builder.addSlot(RecipeIngredientRole.INPUT, col*slot + 1, row*slot + 1)
                   .addItemStacks(Arrays.asList(ings.get(idx).getItems()));
      }
    builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 10).addItemStack(result);
    if (!container.isEmpty())
        builder.addSlot(RecipeIngredientRole.CATALYST, 63, 39).addItemStack(container);
}
```

`RecipeIngredientRole`：`INPUT` / `OUTPUT` / `CATALYST`。`addSlot(role, x, y)` 返回的 builder 再 `addItemStack(s)` / `addItemStacks(list)` / 还能 `addFluidStack(...)`（流体机器用）。

### 4.4 draw 与 getTooltip（自定义绘制）

```java
@Override
public void draw(CookingPotRecipe recipe, IRecipeSlotsView view, GuiGraphics g, double mx, double my) {
    background.draw(g, 0, 0);
    arrow.draw(g, 60, 9);          // 动画箭头
    heatIndicator.draw(g, 18, 39);
    if (recipe.getExperience() > 0) expIcon.draw(g, 63, 21);
}

@Override
public void getTooltip(ITooltipBuilder tooltip, CookingPotRecipe recipe, IRecipeSlotsView view, double mx, double my) {
    if (ClientRenderUtils.isCursorInsideBounds(61, 2, 22, 28, mx, my)) {
        tooltip.add(Component.translatable("gui.jei.category.smelting.time.seconds", recipe.getCookTime()/20));
    }
}
```

注意 1.20.1 用 `GuiGraphics`（不是旧 `PoseStack`+`Tessellator` 直绘），drawable 的 `draw(GuiGraphics, x, y)`。

## 5. 流体显示（TBL purifier/steeping pot 需要）

FD cooking pot 不含流体，但 builder 支持：

```java
builder.addSlot(RecipeIngredientRole.INPUT, x, y)
       .addIngredient(NeoForgeTypes.FLUID_STACK /* 或 ForgeTypes.FLUID_STACK */, fluidStack)
       .setFluidRenderer(capacityMb, false, width, height);
```

具体 fluid ingredient type 名称按 JEI/Forge 版本（`mezz.jei.api.forge.ForgeTypes.FLUID_STACK`）确认。

## 6. 依赖配置（build.gradle）

参考 umapyoi（同为 1.20.1 Forge）的写法：

```gradle
compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-common-api:${jei_version}"))
compileOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge-api:${jei_version}"))
runtimeOnly(fg.deobf("mezz.jei:jei-${mc_version}-forge:${jei_version}"))
```

即：编译期只依赖 common+forge **api**，运行期用完整 forge jar。这样 JEI 是可选依赖，不装也能启动。

## 7. 对 The Betweenlands 的迁移建议

- 原项目 `compat/jei/*` 全部重写。每个机器一个 `IRecipeCategory`：mortar、purifier、animator、censer、smoking rack、steeping pot、crab pot filter、compost、infuser、alembic、BL furnace/dual furnace。
- 一个 `BetweenlandsJEIPlugin implements IModPlugin`（`@JeiPlugin`）统一注册。
- JEI 侧 `BLJeiRecipeTypes` 常量与 `BLRecipeTypes` 一一对应。
- catalysts 用机器方块物品；`addRecipeClickArea` 接到各机器 `Screen`；`addRecipeTransferHandler` 接到各 `Menu`。
- 配方实例从 `level.getRecipeManager().getAllRecipesFor(BLRecipeTypes.XXX.get())` 收集（FD 的 `FDRecipes` 即此思路），需要在 JEI `registerRecipes` 时拿到客户端 level/recipeManager。
- 务必先完成第 1 篇笔记的 `RecipeType`/`RecipeSerializer`，JEI 依赖它们的配方实例。
- 依赖按可选处理（compileOnly api + runtimeOnly full），不影响无 JEI 启动。

## 8. 参考路径（1.20 分支）

- `src/main/java/vectorwing/farmersdelight/integration/jei/JEIPlugin.java`
- `src/main/java/vectorwing/farmersdelight/integration/jei/FDRecipeTypes.java`
- `src/main/java/vectorwing/farmersdelight/integration/jei/category/CookingRecipeCategory.java`
- `src/main/java/vectorwing/farmersdelight/integration/jei/FDRecipes.java`
- 仓库：`https://github.com/vectorwing/FarmersDelight/tree/1.20`
