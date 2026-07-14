# 土卫六 (Titan) 维度 · 材料加工链任务清单 (Material Processing Chain — Task List)
*配套设计：[titan_design.md](titan_design.md) §四(代谢 Lore)/§五(材料链)；工程约定：[titan_technical_design.md](titan_technical_design.md)*
*可选前置来源：`build.gradle.kts` 已加 Create / Mekanism / Farmer's Delight（`modCompileOnly`，见 [repo 记忆 titan-refs §8]）*

> **范围 (Scope):** 把「原料采集 → 生物/机械/化学加工 → 成品」落成可玩的材料链；补齐 §五 中 ⬜ 未做的物品；接入三个可选前置的**互斥分档**配方。
> **🚫 不实装生化模拟**（§四仅 Lore）；所有加工是普通配方/方块行为。

---

## 图例 / 约定 (Legend)

**复杂度 Cx:** `S` 单文件/数值 · `M` 多文件/新逻辑 · `L` 新方块实体/系统。**不给时间估算。**
**状态:** ☐ 待办 · ◐ 进行中 · ☑ 完成。

**无头验收规范（沿用 repo 经验）：**
- 编译 `:1.20.1-forge:compileJava` / `:1.21.1-neoforge:compileJava`（新增 .java 需 `--rerun-tasks`）；加载 `runServer` 至 `Done` 零数据包报错。
- **方块实体需 `forceload add <x> <z>` 才 tick**（dev 无玩家时 spawn 区块也不 tick）。
- 读状态 `/data get block <x y z> <path>`；掉落 `/loot spawn ~ ~ ~ loot <ns>:...`；配方 `/recipe give` 或 JEI/EMI 目视。
- 视觉/交互（粒子、破坏、右键）→ `runClient` 目视。
- **两版同步**：每阶段 Forge + NeoForge 都过（datagen `runData` 双跑）。

---

## 一、整合后的设计要点（4 点调整已并入）

1. **乙炔多路线**（不再只靠冰笋）：
   - 直采：`acetylene_spire` 粉碎 → `condensed_acetylene`（高纯捷径）。
   - **托林热解 🔥**：`hardened_tholin`/`tholin_dust` **熔炉烧制** → `condensed_acetylene`（可再生主源）。
   - 焦油干馏：`tholin_tar` 烧制 → `condensed_acetylene` + 少量 `tholin_dust`。
2. **氢气被动产出（绑菌毯）**：新方块 **集氢罩 `hydrogen_collector`**（+BE），架在 `hydrogen_bubble_mat` 正上方，无需能量随时间攒 `hydrogen_capsule`，自动推入相邻容器（漏斗可自动化）；菌毯移除即停。
3. **被动方块② 托林堆肥槽 `tholin_composter`**（+BE）：架在 `tholin_mycelium` 上，吞相邻容器的生物残渣/纤维 → 缓出 `tholin_dust`（分解者闭环）。
4. **晶体三试剂**：`abyss_crystal→abyss_crystal_dust`、`ammonia_crystal→ammonia_salt`、`tholin_crystal→tholin_crystal_dust`（各自独立用途）。
5. **其它被动生产者用处**：甲烷冰花剪采 `methane_ice_shard`（→燃料/甲烷线）；枝条 `crystalline_twig`→`silicon_dust`（电子/助熔）；纤维/丝囊→膜片/绳。

### 配方分档：**原版 ↔ mod 互斥**（关键，含注意点 0）

- **注意点 0（自动派生）**：Create 鼓风机（fan blasting/smoking）与 Mek 能量熔炉会**自动继承 `minecraft:smelting`/`blasting` 配方**。
  → **熔制类步骤只写一份原版 smelting/blasting，永不禁用、不另写**（两台机器自动增强：Mek 提速、Create 批量）。
- **非熔制步骤（粉碎/搅拌/化学/装配）**采用互斥门控：
  - **base（原版工作台）**：门控 `not(mod_loaded:create) 且 not(mod_loaded:mekanism)` → 仅当加工 mod 都不在时加载。
  - **Create 配方**：`mod_loaded:create`；**Mek 配方**：`mod_loaded:mekanism`；二者可共存（原版自动禁用）。
  - **Farmer's Delight**：仅用于生物副线（切菜板拆解腺体/丝囊、炖锅熬解毒剂），`mod_loaded:farmersdelight`，同样禁用其 base 对应项。

---

## 二、契约冻结：新增注册项 ID（冻结后只读）

**新物品（`titan_satellite:` 命名空间，14 项）**

| id | 名称 | 主要来源 |
|---|---|---|
| `tholin_dust` | 托林粉末 | 托林砂/硬化托林/焦油/堆肥槽 |
| `condensed_acetylene` | 凝乙炔 | 冰笋粉碎 / 托林热解 |
| `hydrogen_capsule` | 氢气瓶 | 集氢罩被动 / 菌毯手采 |
| `meteoric_iron_ingot` | 陨铁锭 | 陨铁矿熔炼 |
| `polyphosphazene_coenzyme` | 多磷腈辅酶 | 凝乙炔+氢化合成 / 顶级掠食稀有掉落 |
| `azotosome_sheet` | 氮质体膜片 | 浮游薄膜+托林 |
| `cryo_alloy_ingot` | 冰晶合金锭 | 冰晶甲壳+陨铁锭 |
| `bio_battery` | 生物电池 | 废弃电池+辅酶+膜片+精密组件 |
| `titan_antidote` | 异星解毒剂 | 神经腺+毒腺(+氨盐) |
| `methane_ice_shard` | 甲烷冰晶 | 甲烷冰花剪采 |
| `silicon_dust` | 硅晶粉 | 晶化枝条粉碎 |
| `abyss_crystal_dust` | 深渊晶粉（试剂①） | 深渊晶体粉碎 |
| `ammonia_salt` | 氨盐（试剂②） | 氨晶体粉碎 |
| `tholin_crystal_dust` | 托林晶粉（试剂③） | 托林晶体粉碎 |

**新方块（2 项，均带 BlockEntity）**

| id | 类 | 依附 | 行为 |
|---|---|---|---|
| `hydrogen_collector` | `HydrogenCollectorBlock` + BE | `hydrogen_bubble_mat` 正上方 | 被动攒 `hydrogen_capsule`→推入相邻容器 |
| `tholin_composter` | `TholinComposterBlock` + BE | `tholin_mycelium` 正上方 | 吞生物残渣→缓出 `tholin_dust` |

**改动既有**

| 目标 | 改动 |
|---|---|
| `methane_ice_bloom` loot | 剪采掉落 `methane_ice_shard` |
| `SpecialMethanePumpBlockEntity` | 加 booster 输入槽：喂 `polyphosphazene_coenzyme`→提速/降生态压力增幅（闭合 §4.2 循环） |
| `TSMobEffects.THOLIN_TOXIN` | `titan_antidote` 使用时清除该效果 |

---

## 三、跨模组配方映射（实现对照）

| 步骤 | base（原版，互斥） | Create | Mekanism | FD |
|---|---|---|---|---|
| 托林→托林粉末 | 合成台 | `crushing` | `crushing`/`enriching` | — |
| 冰笋→凝乙炔 | 合成台 | `crushing` | `crushing` | — |
| **托林热解→凝乙炔** | **`smelting`（常开）** | 鼓风机自动 ✔ | 熔炉自动 ✔ | — |
| **陨铁矿→锭** | **`blasting`（常开）** | 自动 ✔ | 自动 ✔（+`enriching` 翻倍*） | — |
| 晶体→三晶粉 | 合成台 | `crushing` | `crushing` | — |
| 枝条→硅晶粉 | 合成台 | `crushing` | `crushing` | — |
| 凝乙炔+氢→辅酶 | 合成台 | `mixing`(加热) | `injecting`(化学注入·原生氢气) | — |
| 薄膜+托林→膜片 | 合成台 | `pressing`/`compacting` | `enriching` | — |
| 甲壳+陨铁→合金 | 合成台 | `mixing`(加热) | `combining`/冶金 | — |
| 腺体→解毒剂 | 酿造/合成台 | — | — | 炖锅 `cooking` |
| 腺体/丝囊拆解 | 合成台 | — | — | 切菜板 `cutting` |

\* 翻倍为**升级**（与 base 熔炼共存，非互斥）。

---

## 四、完成顺序 (Ordered Stages)

### Stage MC0 · 契约冻结与机制选型 → 支撑全链

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC0.1 | 冻结上表全部 item/block/BE id + lang key；确认占位贴图策略（纯色/复用） | S | — | ID 清单定稿、下游只读 | 本文件 §二 |
| ☐ MC0.2 | **配方产出机制选型**：`TSRecipeProvider`(datagen) 统一产出——base 原版用官方 builder；**mod 变体 + 条件用手搓 `JsonObject`（不引用 mod 类，避开 `modCompileOnly` 运行期缺类）**；datagen 自动写正确目录（1.20.1 `recipes/` vs 1.21.1 `recipe/`）；条件 key/type 用 stonecutter `//? if forge/neoforge` 供给 | M | — | provider 骨架编译过；空跑 `runData` 双版目录正确 | `data/TSRecipeProvider.java`, `data/TSDataGenerators.java` |
| ☐ MC0.3 | **条件语法尖峰**：先手搓 **1 条** `mod_loaded:create` 门控配方，Forge/NeoForge 各 `runData`+`runServer`，确认**装 Create 时加载、不装时不加载**（recipe 条件语法两版可能不同于 dimension 条件，必须先验证再量产） | S | MC0.2 | 两版条件配方按 mod 存在与否正确启停 | 1 个样例配方 json |

**Gate MC0**：ID 冻结 + 配方机制/条件语法两版验证通过。

### Stage MC1 · 物品注册与展示 → 支撑配方

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC1.1 | `TSItems` 注册 14 新物品 + 加入 `TSCreativeTabs` | S | MC0.1 | 两版 `build`；创造栏见全部新物品 | `registry/TSItems.java`, `registry/TSCreativeTabs.java` |
| ☐ MC1.2 | 物品模型 + 中英 lang（datagen） | S | MC1.1 | `runData` 生成模型/lang；`runClient` 图标/名称正常 | `data/TSItemModelProvider.java`, `data/TSLanguageProvider.java` |

**Gate MC1**：新物品可在创造栏取用、有名称与图标、两版编译。

### Stage MC2 · 被动生产方块①：集氢罩

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC2.1 | `HydrogenCollectorBlock` + BE：检测正下方 `hydrogen_bubble_mat`；无能量按间隔攒 `hydrogen_capsule`→推正上/相邻容器；菌毯移除停 | L | MC1.1 | forceload 后置于菌毯上，容器渐入氢气瓶；无菌毯不产 | `block/HydrogenCollectorBlock.java`, `blockentity/HydrogenCollectorBlockEntity.java`, `registry/TSBlocks.java`,`TSItems.java`,`TSBlockEntities.java` |
| ☐ MC2.2 | 集氢罩 模型/lang/方块 loot/`mineable` tag | S | MC2.1 | 破坏掉落自身、镐可采 | `data/TS*Provider.java`, `data/loot/TSBlockLoot.java`, 方块标签 |

**Gate MC2**：菌毯上被动产氢、可漏斗自动化、两版一致。

### Stage MC3 · 被动生产方块②：托林堆肥槽

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC3.1 | `TholinComposterBlock` + BE：检测下方 `tholin_mycelium`；消耗相邻容器内生物残渣/`tholin_fibre` 等 → 缓出 `tholin_dust` | L | MC1.1 | forceload 后喂纤维，渐出托林粉末；无菌网不产 | `block/TholinComposterBlock.java`, `blockentity/TholinComposterBlockEntity.java`, 注册三件套 |
| ☐ MC3.2 | 模型/lang/loot/tag | S | MC3.1 | 掉落自身、镐采 | 同 MC2.2 类文件 |

**Gate MC3**：菌网上被动再生托林粉末、两版一致。

### Stage MC4 · 甲烷冰花采集

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC4.1 | `methane_ice_bloom` loot 覆写：剪采 → `methane_ice_shard`（保留其连锁爆炸行为不动） | S | MC1.1 | 剪采掉 shard；徒手/非剪无掉落 | `data/loot/TSBlockLoot.java` |

**Gate MC4**：甲烷冰花可剪采得冰晶。

### Stage MC5 · 配方基建 + base（原版）全链

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC5.1 | **熔制类常开配方**（注意点 0）：托林热解/焦油干馏→凝乙炔（`smelting`）、陨铁矿→锭（`blasting`）、`methane_ice_shard` 作燃料/熔化产物 — **无条件常开** | S | MC0.2,MC1.1 | vanilla 下可熔制；装 Create/Mek 时鼓风机/熔炉自动可做（不重复不禁用） | `data/TSRecipeProvider.java` |
| ☐ MC5.2 | **非熔制 base 配方**（`not(create)+not(mek)` 门控）：托林/晶体/枝条粉碎、辅酶氢化、膜片、合金、生物电池装配、解毒剂 | M | MC0.3,MC1.1 | 纯 vanilla 环境全链可手搓贯通至 `bio_battery`/`titan_antidote` | 同上 |

**Gate MC5**：**无任何前置** mod 时，从原料到终产物全链可玩（`runServer` 零报错 + JEI/`/recipe` 核对）。

### Stage MC6 · Create 分档配方

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC6.1 | 手搓 `create:crushing/mixing/pressing/compacting`（粉碎/氢化/膜片/合金），`mod_loaded:create`；**熔制类跳过**（自动派生） | M | MC0.3,MC5.2 | 装 Create：base 对应项禁用、Create 配方在 JEI 出现且可用 | `TSRecipeProvider`（手搓 JSON 段） |

**Gate MC6**：装 Create → 机械档生效、base 互斥禁用。

### Stage MC7 · Mekanism 分档配方

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC7.1 | 手搓 `mekanism:enriching/crushing/injecting/combining` + 陨铁 `enriching` 翻倍 + `ammonia_salt`→冷却液接口，`mod_loaded:mekanism`；基础熔炼跳过（熔炉自动） | M | MC0.3,MC5.2 | 装 Mek：base 禁用、Mek 配方可用；辅酶走化学注入(原生氢气) | 同上 |

**Gate MC7**：装 Mek → 化学档生效、翻倍升级共存。

### Stage MC8 · Farmer's Delight 分档配方

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC8.1 | 手搓 `farmersdelight:cutting`（腺体/丝囊拆解）+ `cooking`（炖锅熬 `titan_antidote`），`mod_loaded:farmersdelight`，禁用对应 base 项 | M | MC0.3,MC5.2 | 装 FD：切菜板/炖锅路线可用、base 对应禁用 | 同上 |

**Gate MC8**：装 FD → 生物副线走 FD 工站。

### Stage MC9 · 解毒剂效果 + 泵增效闭环

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC9.1 | `titan_antidote` 使用 → 清除 `THOLIN_TOXIN`（+ 使用动画/音效） | S | MC1.1 | 中毒后使用即解 | `item/TitanAntidoteItem.java`, `TSItems.java` |
| ☐ MC9.2 | 甲烷泵 booster 槽：喂 `polyphosphazene_coenzyme` → 提抽取速率 / 降生态压力增幅（改 `SpecialMethanePumpBlockEntity`，保留冻结事件签名） | M | MC1.1 | 运行中投辅酶，产速升/波次强度增幅降；`/data get block` 可验 | `blockentity/SpecialMethanePumpBlockEntity.java` |

**Gate MC9**：解毒实用产物成立；工业-生态闭环成立。

### Stage MC10 · 验收与回写

| ID | 任务 | Cx | Deps | 验收 | Files |
|---|---|---|---|---|---|
| ☐ MC10.1 | **mod 组合矩阵**：无前置 / 仅 Create / 仅 Mek / 仅 FD / 全装 —— 各跑 `runServer` 验证配方启停正确、无冲突/重复 | M | MC6–MC8 | 5 组合均 `Done` 零报错、互斥正确 | — |
| ☐ MC10.2 | 两版 `build`+`runData`+`runClient` 冒烟；回写 `titan_design.md` §五/§8.1 状态；更新 repo 记忆 | S | 全部 | 两版通过、文档/记忆同步 | `docs/titan_design.md`, 记忆 |

**Gate MC10（终）**：两版编译+运行通过；5 种 mod 组合互斥正确；文档回写。

---

## 五、关键风险 (Risks)

1. **配方目录名两版不同**：1.20.1=`recipes/`、1.21.1=`recipe/`。→ 用 datagen（自动写对目录），**不要**手放 `src/main/resources` 共享 JSON。
2. **`modCompileOnly` 运行期缺类**：datagen 运行期无 Create/Mek/FD 类。→ mod 变体配方**手搓 JSON（不 import 其 builder）**。
3. **recipe 条件语法**可能异于 dimension 条件（MC0.3 尖峰先验证两版）。
4. **注意点 0 自动派生**：熔制类**不要**为 Create/Mek 另写或禁用 base，否则鼓风机/熔炉派生项一并消失。
5. **BE 不 tick**：dev 验证记得 `forceload add`。
