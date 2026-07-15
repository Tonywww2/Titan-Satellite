# 土卫六 (Titan Moon) — 验收测试矩阵 (Acceptance Test Matrix)

> 对应 **PF-4 / T5.4**（平衡 + 验收矩阵 + DoD）。源：[parallel-tasks.md](parallel-tasks.md) 各任务 output + [task-plan.md](task-plan.md)。
> 范围：**M0–M5（Stage A–F）**。M6（Stage G · 群系特色化，PG-1..4）为后续独立阶段，见 §5。
>
> **图例**：✅ 无头实测（`compileJava` / `runServer` 命令验证通过）· 🖥 需 `runClient` 肉眼/听感确认（渲染/音效/雾）· ⚙ 编译 + 逻辑验证（运行时需真实玩家交互触发，无头服无法驱动）。

## 1. 验收矩阵 (Acceptance Matrix)

| 里程碑 | 任务 | 功能 | 验收标准 | 验证方式（已做） | 状态 |
|---|---|---|---|---|---|
| M0/A | PA-1 | 注册/装配/lang/渲染器桩冻结 | build 通过、全注册项存在、lang 完整、到标题页 | `compileJava` + `runServer`(Done) + `runClient`(标题页) | ✅ |
| M0/A | PA-2 | worldgen 类型 / event / mixin / is_titan 标签 | 编译过、event/mixin 自订阅不改主类、标签加载 | `compileJava` + `runServer`（density 探针） | ✅ |
| M1/B | PB-1 | 五群系 biome + multi_noise 维度 | 5 群系加载、维度用 multi_noise | `runServer`(Done) + `locate biome` | ✅ 全分布待 M6/PG-2 |
| M1/B | PB-2 | noise_settings + density + 按群系 surface_rule | 峡谷/沙丘/断崖高差、各群系地表正确 | `runServer` + forceload 远近点 top-Y 对比 | ✅ |
| M1/B | PB-3 | 特征（甲烷湖/陨坑/沙脊/天坑/晶簇/喷泉斑） | 特征经 biome_modifier 注入并生成 | `runServer` + `/place feature`×6 全 Placed | ✅ |
| M2/C | PC-1 | 冰硅甲虫（中立、护甲减伤） | summon 正常、掉落、生成于 titan | `runServer` summon + `/loot spawn` | ✅ 反击/减伤需玩家战斗 |
| M2/C | PC-2 | 氨泉掠食者（敌对、攻击附毒、两栖） | summon、攻击中毒、掉落、生成 | `runServer` summon + 铁傀儡引战染 poison + `/loot` | ✅ |
| M2/C | PC-3 | 失控探测器（敌对、即时激光） | summon、掉落、生成于遗迹 | `runServer` summon + `/loot spawn` | ✅ 激光视觉需 runClient |
| M4/E | PE-1 | 冰火山喷泉击飞 | 站上喷发被击飞 | `runServer` fill 喷泉 + armor_stand 读 Motion(y↑) | ✅ |
| M4/E | PE-2 | 甲烷开采塔防（状态机 + 事件族 + Mixin） | 放泵激活→波次→保护→成功产出/失败重置 | `runServer` data merge 全流程→SUCCESS 产出 + mixin 强化 + CORE_REMOVED 重置 | ✅ |
| M4/E | PE-3 | 晶洞惊扰（破坏放毒气 + 惊怪） | 破坏晶体→毒气云 + 惊醒附近敌对 | `runServer` setblock + 事件/方块类加载 | ⚙ 毒气/惊怪需玩家破坏(runClient) |
| M5/F | PF-1 | 结构（托林晶洞 / 先驱前哨站） | 结构在对应群系生成、内含战利品/探测器 | `runServer` `/place`×2 + `/locate`×2 + outpost 箱/墙/探测器实测 | ✅ |
| M5/F | PF-2 | 维度天空/雾特效（橙黄浓雾） | 进维度呈橙黄浓雾、低能见度 | `compileJava` + `runServer`(JSON) + `runClient`(注册无崩) | 🖥 雾视觉需 runClient |
| M5/F | PF-3 | 流体交互 + 音效 | 甲烷/氨接触速冻、关键动作有音效 | `runServer` 双流体接触→packed_methane_ice 实测 | ✅ 音效需 runClient 听 |
| M5/F | PF-4 | 平衡 config + 验收矩阵 + DoD | config 生成、矩阵齐、全系统联动 | 本文档 + `TSConfig`(config toml) + DoD 集成 runServer | ✅ 见 §2 |

## 2. DoD / Gate F 检查单

> 集成 `runServer`（独立世界 `titan_pf4_test:25574`）——**全 M1–M5 系统同服加载、无跨任务冲突**。

- [x] **build 通过**：`compileJava --rerun-tasks` BUILD SUCCESSFUL（仅既有 `[removal]` 弃用警告）。
- [x] **runServer Done**：全系统联动加载（worldgen + 5 群系 + 实体×4 + 事件×3 + mixin + 结构×2 + 流体交互 + 天空特效注册 + 平衡 config），无报错。
- [x] **结构生成**：`/place structure` 两结构均 Generated、`/locate structure` 均找到自然实例。
- [x] **全系统联动**：同一 `runServer` 内 summon 四生物、`/place` 结构、流体交互、config 波次缩放均正常，子系统互不冲突。
- [x] **平衡 config**：`config/titan_moon-common.toml` 生成；改 `waveMobHealthMultiplier` 后波次怪最大生命按乘子缩放（NBT 实测）。
- [ ] **天空橙黄浓雾（肉眼）**：需 `runClient` 进 titan 维度目视（无头无法截屏）——见 §4。
- [ ] **完整 `runClient` 通关（人工）**：需真人跑一遍（战斗/破坏/击飞/激光/音效）——见 §4。

> **结论**：DoD 中所有**无头可验证**项全部通过；剩余为**渲染/音效/玩家交互**类，须人工 `runClient` 目视/操作确认（各任务已在其 output 标注）。

## 3. 平衡参考 (Balance Reference)

> 当前各系统平衡数值与其**设定位置**。带 ⚙ 者已由 PF-4 的 `config/titan_moon-common.toml` 暴露为可调项；其余硬编码在各自 Owns 文件（改动需对应任务的 CR）。

| 系统 | 参数 | 当前值 | 设定位置 |
|---|---|---|---|
| 塔防波次 | 满进度 / 总波数 / 完整度 | 2400t / 5 / 100 | `blockentity/SpecialMethanePumpBlockEntity` |
| 塔防波次 | 每波怪数 | 2 + waveIndex | `event/WaveController#baseWaveMobCount` |
| 塔防波次 | ⚙ 波次怪生命乘子 | 1.0（可调 0.25–8.0） | **`config/TSConfig`** |
| 塔防波次 | ⚙ 波次怪攻击加成 | 0.0（可调 0–20） | **`config/TSConfig`** |
| 塔防产出 | precision_components | 2 + 幸存波数 | `event/WaveController#succeed` |
| 生物 | 氨泉掠食者 HP/攻/毒时长 | 24 / 5 / 80–200t | `entity/AmmoniaStalker` |
| 生物 | 失控探测器 HP/激光伤/射程 | 20 / 6 / 16 | `entity/CorruptedProbe` |
| 生物 | 冰硅甲虫 HP/减伤 | 16 / ×0.6 | `entity/CryoScavenger` |
| 生成 | 各怪 biome_modifier 权重 | 甲虫18? 见各文件 | `data/.../forge/biome_modifier/*_spawn.json` |
| 结构 | 晶洞 / 前哨站 spacing/sep | 22/7 · 34/12 | `data/.../worldgen/structure_set/*.json` |
| 喷泉 | 周期 / 喷发时长 | 140t / 45t | `block/CryovolcanicGeyserBlock` |
| 晶洞惊扰 | 触发概率 / 毒气半径·时长 / 惊怪半径 | 50% / 3·200t / 12（事件可调） | `block/TholinCrystalBlock` + `TholinCrystalDisturbedEvent` |
| 雾 | 深渊浓雾 / 他群系淡雾 near-far | 3–42 / 48–192 | `client/FogHandler`（CR-9） |

## 4. 需人工 `runClient` 确认项 (Client-manual Verification)

无头服无法驱动渲染/音效/玩家交互；以下须真人 `runClient` → 建世界 → `/execute in titan_moon:titan run tp @s <x> 120 <z>` 逐项确认：

1. **天空/雾**（PF-2/CR-9）：进 titan 呈橙黄雾；`methane_abyss` 浓雾（约 42 格）、其它群系淡雾（约 192 格）。
2. **喷泉击飞手感**（PE-1）：站冰火山喷泉喷发瞬间被弹起。
3. **晶洞惊扰**（PE-3）：生存模式破坏 `tholin_crystal` → 概率毒气云 + 附近敌对被惊醒锁定你。
4. **激光开火视觉**（PC-3）：靠近失控探测器 → 蓄力后激光光束 + 命中粒子/音。
5. **中立甲虫反击/减伤**（PC-1）：攻击冰硅甲虫 → 反击并唤醒同类；物理伤害被减。
6. **流体音效 / 速冻粒子**（PF-3）：桶引甲烷贴氨 → 速冻音 + CLOUD 粒子。
7. **塔防实战**（PE-2）：放泵于甲烷池核心上右键激活 → 波次来袭、保护泵至成功。
8. **战斗附毒**（PC-2）：被氨泉掠食者攻击中毒。

## 5. M6 (Stage G · 群系特色化) 状态

M6（PG-1..4 / T6.1..6.4）为 **Gate B 后另开的独立阶段**（新表层块、气候分布修复、地形深化、群系专属特征），**不在 PF-4 / Gate F(M5) 的 DoD 范围内**，当前全部 ☐ 未开始。M5 DoD 通过不受其影响。
