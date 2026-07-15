# 土卫六 · 方块颜色参考表 (Titan Block Color Reference)

> 依据两张**真实土卫六影像**制定，结合 [titan_design.md](titan_design.md)（群系/地形/分布 + §二–§八 生态/方块用途）。
> 用途：为**纯色占位方块**指定统一色值；对**已有自定义贴图**的方块做注释、不覆盖。

## 影像依据 (Image Basis)

1. **惠更斯号着陆实拍 (Huygens DISR — ESA/NASA/JPL/U. Arizona)**：单调的**橙褐/赭黄**河床，散落浑圆冰卵石，天空是更亮的橙黄雾霭。→ 奠定维度**暖色基调**（赭黄 / 琥珀 / 茶褐）。
2. **卡西尼 VIMS 红外伪彩合成 (Cassini VIMS)**：赤道沙丘**橙棕**、部分地体**蓝紫 / 薰衣草**、亮区**近白**、暗区**深褐**。→ 提供**伪彩点缀**（紫 / 青 / 白），用于晶体、氨、冰等"异星"方块。

## 调色板 (Palette)

| 色名 | Hex | 取自影像 | 主要用途 |
|---|---|---|---|
| Titan 赭黄 Ochre | `#C08A4A` | 沙丘 / 赭土地表 | 托林砂、硬化托林 |
| 深托林棕 Deep Tholin | `#8A5A2C` | 干涸托林有机质 | 托林灌木、有机装饰 |
| 琥珀阴影 Amber Shadow | `#6E4A26` | 卵石阴面 / 岩壁 | 深色岩、过渡 |
| 焦油黑 Tar Black | `#171009` | 暗区 | 焦油洼、核心 |
| 陨铁灰 Iron Gray | `#565049` | 陨石金属 | 陨铁碎块 |
| 深渊紫 Abyss Violet | `#8A57C2` | VIMS 蓝紫地体 | 深渊晶体 |
| 甲烷冰蓝 Ice Blue | `#AFD0DC` | 亮冰区 | 枝状结晶、甲烷冰花 |
| 氨青 Ammonia Cyan | `#4EC6C2` | 化学亮区 | 氨晶体、氨液 |
| 冰晶白蓝 Glacier | `#C8E6EA` | 近白亮区 | 冰火山喷泉（发光） |
| 霜灰 Frost Gray | `#B3A992` | 惠更斯灰卵石 | 霜枯灌木 |
| 机械金属 Metal | `#7C7C82` | —（人造物） | 特制甲烷泵 |

**状态图例：** 🎨 本次已上色（纯色占位→参考色） ｜ 🖼️ 已有自定义贴图（注释，未改） ｜ ♻️ 复用他块贴图 ｜ 💧 流体（颜色由 `FluidType` tint 控制）

---

## 按群系分布 (By Biome)

### 1. 液态甲烷深渊 Methane Abyss (Y 0–32)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `sedimentary_titan_stone` | 表层 | ♻️ 复用 `titan_basalt` | （随 basalt） | 沉积泰坦石，深褐近黑 |
| `titan_basalt` | 峡谷岩壁 | 🖼️ 自定义贴图 | 近黑 `~#241C15` | 已有细节贴图，未改 |
| `abyss_crystal` | 发光晶(L6) | 🎨 | `#8A57C2` | 深渊紫，取 VIMS 蓝紫伪彩 |
| `tholin_tar` | 焦油洼 | 🎨 | `#171009` | 焦油黑 |
| `methane_pool_core` | 功能核心(L3) | 🎨 | `#1E1811` | 近黑、微光 |
| `liquid_methane` | 流体 | 💧 tint `#E7E3B8` | `#E7E3B8` | 极淡暖黄(LNG)，液面由 tint 控制；粒子贴图已同步 |

### 2. 撞击陨坑荒原 Cratered Wastelands (Y 64–80)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `weathered_titan_stone` | 表层 | ♻️ 复用 `titan_stone` | （随 stone） | 风化泰坦石 |
| `branch_crystal` | 枝状结晶(L4) | 🎨 | `#A7C9D3` | 冰蓝，"冻结的枝杉" |
| `meteor_fragment` | 陨铁矿 | 🎨 | `#565049` | 陨铁灰 |
| `crushed_ice` | 碎冰 | 🖼️ 自定义贴图 | 淡蓝白 | 已有贴图，未改 |

### 3. 托林沙海 Tholin Dune Seas (Y 64–80)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `tholin_sand` | 表层 | 🖼️ 自定义贴图 ⚠️ | 目标 `#C08A4A` | **当前贴图偏灰白、未体现土卫六橙色**；建议后续重着为赭黄 |
| `hardened_tholin` | 结壳 / 风柱 | 🎨 | `#B07C3C` | 赭黄结壳（Ochre 稍深） |
| `tholin_shrub` | 十字装饰 | 🎨 | `#8A5A2C` | 深托林棕 |
| `tholin_crystal` | 晶洞晶体(L10) | 🖼️ 自定义贴图 | 青 + 品红 | 变异有机晶，已有贴图 |

### 4. 极地迷宫冰原 Polar Labyrinths (Y 160–200)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `packed_methane_ice` | 表层 | 🖼️ 自定义贴图 | 淡蓝 | 已有贴图，未改 |
| `methane_ice_bloom` | 冰花装饰(L5) | 🎨 | `#B9DEE6` | 淡青冰蓝 |

### 5. 冰火山断崖 Cryovolcanic Cliffs (Y 280–320)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `cryo_ice` | 表层 | 🖼️ 自定义贴图 | 蓝灰冰 | 已有贴图，未改 |
| `ammonia_crystal` | 氨晶矿(L7) | 🎨 | `#4EC6C2` | 氨青 |
| `cryovolcanic_geyser` | 喷泉功能(L6) | 🎨 | `#C8E6EA` | 冰晶白蓝、发光 |
| `liquid_ammonia` | 流体 | 💧 tint `#BAE8E4` | `#BAE8E4` | 极淡冷青，液面由 tint 控制；粒子贴图已同步 |

### 6. 荒芜高原 Barren Plateau (Y 150–200)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `weathered_titan_stone` | 台地表层 | ♻️ 复用 `titan_stone` | （随 stone） | 同陨坑荒原 |
| `titan_gravel` | 砾石场 | 🖼️ 自定义贴图 | 灰砾（目标暖褐 `#9C8358`） | 已有贴图，未改 |
| `frost_bush` | 霜枯灌木 | 🎨 | `#B3A992` | 霜灰（陨坑 / 荒原 / 冰火山共用） |

### 基础 / 功能方块 (Base & Functional)

| 方块 id | 用途 | 状态 | 色值 | 说明 / 依据 |
|---|---|---|---|---|
| `titan_stone` | 维度 `default_block` | 🖼️ 自定义贴图 | 深灰褐 | 已有贴图；建议偏暖 `~#6B5847` |
| `special_methane_pump` | 开采机器(带 BE) | 🎨 | `#7C7C82` | 机械金属灰 |

---

## 备注 (Notes)

- **纯色占位判定**：以贴图 PNG 文件大小区分——约 136–137 B 为单色占位（本次上色的 14 块）；343–742 B 为已有细节贴图（8 块，仅注释）。
- **流体**：`liquid_methane` / `liquid_ammonia` 世界液面颜色由 [TSFluidTypes](../src/main/java/com/tonywww/titan_moon/registry/TSFluidTypes.java) 的 `getTintColor` 控制（复用原版 water 贴图 + 染色），属**刻意的"近无色写实"**设定，本表不改其 tint，仅把破碎粒子 PNG 同步为同色。
- **待办**：`tholin_sand` 现贴图偏灰白，与土卫六橙色基调不符，建议后续按 `#C08A4A` 重着；`titan_gravel` 可偏暖褐。
