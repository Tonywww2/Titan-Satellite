# 土卫六 (Titan) 维度最终设计方案
# Titan Dimension Final Design Document
*基于 Minecraft 1.20.1 整合包的维度设计*
*Dimension design for Minecraft 1.20.1 modpack*

本设计方案结合了现实中土卫六（Titan）的地质特征与 Minecraft 1.20.1 的维度定制特性，构建了一个具有极大高度差和深度冒险玩法的极端维度。
This design document combines Titan's real-world geological features with Minecraft 1.20.1 custom dimension capabilities to create an extreme dimension with massive elevation changes and deep adventure mechanics.

> 技术实现方案（注册项、数据驱动 worldgen、事件/Mixin 接入点、工程结构与里程碑）见配套文档：[titan_technical_design.md](titan_technical_design.md)。
> Technical implementation (registries, data-driven worldgen, event/Mixin hooks, project structure and milestones): see [titan_technical_design.md](titan_technical_design.md).

---

## 〇、 维度基础参数 (Base Dimension Parameters)

*   **重力 (Gravity):** 采用普通重力（不改变玩家重力/跳跃/降落）。土卫六低重力仅作背景设定，不作玩法机制。 / Normal gravity (no player gravity/jump/fall changes). Titan's low gravity is background lore only, not a gameplay mechanic.
*   **温度与空气 (Temperature & Oxygen):** 极寒（-179°C）、无氧的橙黄大气仅作**环境氛围**（冷色群系、冰/甲烷地貌），**不再有需装备抵御的极寒/缺氧生存机制**。 / Extreme cold (-179°C) and the anoxic orange atmosphere are environmental ambiance only (cold biomes, ice/methane terrain); there is no cold/oxygen survival mechanic requiring gear.
*   **光照与天气 (Lighting & Weather):** 浓厚的橙黄色大气层，地表光照极低。普通降雨替换为“液态甲烷雨”。 / Dense orange haze, very low surface light. Normal rain is replaced with "Liquid Methane Rain."
*   **高度限制 (Build Limit):** Y = 0 至 Y = 320。

---

## 一、 地形与群系分布 (Terrain & Biomes)

> **地形基调 (Terrain tone):** 整体追求强烈的**破碎感**——悬崖峧壁、断层、陡坡与孤峰交错，避免平缓过渡；配合 0–320 的极大高差营造险峻、可垂直探索的地貌。 / Overall a strongly **fragmented** terrain — cliffs, precipices, fault scarps, steep slopes and isolated spires, avoiding smooth transitions; combined with the 0–320 elevation range for a rugged, vertically-explorable landscape.

> **群系分布机制 (Biome distribution):** 六大群系由 `multi_noise` 的气候噪声（temperature/humidity/continentalness/erosion/weirdness/depth）铺开；各群系专属表层方块由 `surface_rule` 决定，专属地物由按群系限定的 Feature 注入。**冰火山断崖为极地迷宫冰原内部中心的稀有子区域**。 / Six biomes spread by multi_noise climate fields; per-biome surface blocks via surface_rule, per-biome landmarks via biome-limited features. The Cryovolcanic Cliffs is a rare sub-region nested at the center of the Polar Labyrinths.

### 1. 土卫六·液态甲烷深渊 (Titan · Methane Abyss) [Y: 0 - 32]
由**沉积泰坦石 (sedimentary_titan_stone)** 构成的陡峭峡谷地带。地表遍布**大量碎裂的裂隙**（形似缩小的老版本峡谷），裂隙底部积存着液态甲烷。
Steep canyon terrain of sedimentary titan stone; the surface is riven with **numerous fractured fissures** (like smaller legacy ravines), with liquid methane pooling at their bottoms.
*   **特异地貌 (Special Feature):** **甲烷海 (Methane Mare)**。大面积地表整体坡陷至海平面以下、被液态甲烷彻底淹没，形成开阔的甲烷之海（类似巨型天坑式的地表移除）。 / Vast stretches of surface collapse below sea level and are completely flooded by liquid methane (large sinkhole-like surface removal).

### 2. 土卫六·撞击陨坑荒原 (Titan · Cratered Wastelands) [Y: 64 - 80]
由**风化泰坦石 (weathered_titan_stone)** 构成的冰冻荒原。地表零散分布着**树枝状结晶 (branch_crystal)** 丛，如冻结的枝杉。
Frozen wastelands of weathered titan stone, dotted with **branching crystal (branch_crystal)** growths like frozen twigs.
*   **特异地貌 (Special Feature):** **巨型陨石坑 (Giant Craters)**。带**凸起坑缘**的真实陨石坑轮廓（碗形凹陷 + 抬升的环形边缘）；坑底**小概率**露出微型甲烷湖。 / Realistic impact craters with **raised rims**; the crater floor **rarely** exposes a miniature methane lake.

### 3. 土卫六·托林沙海 (Titan · Tholin Dune Seas) [Y: 64 - 80]
广阔的橙色托林沙丘区，表面覆盖着托林有机化合物粉末（**tholin_sand**）。
Vast orange dune fields covered in powdery tholin organics.
*   **特异地貌 (Special Feature):** **巨型沙脊 (Megayardangs)**。高墙般狭长的风蚀山脊，配合连绵起伏的沙丘。 / Wall-like, long, narrow wind-eroded ridges amid rolling dunes.

### 4. 土卫六·极地迷宫冰原 (Titan · Polar Labyrinths) [Y: 160 - 200]
地势急剧抬升的甲烷浮冰（**packed_methane_ice**）迷宫。**地表向下数格后即化为巨型的破碎海绵**——密布空洞的多孔冰体。
Sharply rising labyrinths of packed methane ice. **A few blocks below the surface it turns into a giant shattered sponge** — porous ice riddled with cavities.
*   **特异地貌 (Special Feature):** **巨型冰层天坑 (Ice Sinkholes)**。巨大的冰窟竆直通下层地形。 / Massive ice sinkholes dropping straight to lower terrain.

### 5. 土卫六·冰火山断崖 (Titan · Cryovolcanic Cliffs) [Y: 280 - 320]
由寒冰（**cryo_ice**）构成的极度险峭的垂直断崖，**仅生成于极地迷宫冰原的中心区域**、被极地群系环绕。
Extremely steep vertical cliffs of cryo ice, generating **only at the center of the Polar Labyrinths**, encircled by the polar biome.
*   **特异地貌 (Special Feature):** **冰火山喷泉群 (Cryovolcanic Geysers)** 与山巅的**氨水火山口 (Ammonia Calderas)**。 / Clusters of cryovolcanic geysers and ammonia calderas at the peaks.

### 6. 土卫六·荒芜高原 (Titan · Barren Plateau) [Y: 150 - 200]（新增 / New）
由**风化泰坦石 (weathered_titan_stone)** 构成的高耸台地。地质类似极地迷宫冰原，但**空洞区域极少出现**、地层坚实。与相邻群系之间以**极其陡峭的断崖**过渡。
Towering plateaus of weathered titan stone. Geologically similar to the Polar Labyrinths but with **very few cavities** and solid strata; transitions to neighboring biomes are **extremely steep escarpments**.

---

## 二、 特色生物群 (Custom Creatures)

### 1. 甲烷浮游体 (Aero-Jelly)
*   **类型 (Type):** 被动 (Passive)
*   **特征 (Features):** 漂浮在空中的气囊生物。 / Balloon-like creatures floating in the atmosphere.
*   **掉落物 (Drops):** 特殊的“浮游薄膜”（基础轻量化合成材料）。 / "Aero-Membrane" (basic lightweight crafting material).

### 2. 冰硅甲虫 (Cryo-Scavenger)
*   **类型 (Type):** 中立 (Neutral)
*   **特征 (Features):** 拥有坚硬冰晶外壳的节肢动物。 / Arthropods with tough ice-crystal shells.
*   **掉落物 (Drops):** “冰晶甲壳”（用于合成或升级装备的硬化材料）。 / "Cryo-Carapace" (hardening material for crafting/upgrading equipment).

### 3. 氨泉掠食者 (Ammonia Stalker)
*   **类型 (Type):** 敌对 (Hostile)
*   **特征 (Features):** 潜伏在冰区的两栖猛兽，攻击附带毒素。 / Amphibious predators lurking in ice biomes, attacks inflict poison.
*   **掉落物 (Drops):** “毒性腺体”（用于合成抗性药剂或特殊涂层）。 / "Toxic Gland" (used for brewing resistance potions or special coatings).

### 4. 失控的探测器 (Corrupted Probe)
*   **类型 (Type):** 敌对 (Hostile)
*   **特征 (Features):** 发射激光的古代机械遗留物。 / Ancient mechanical relics that fire lasers.
*   **掉落物 (Drops):** “废弃高能电池”、“精密电子元件”。 / "Depleted High-Energy Battery", "Precision Electronic Components".

---

## 三、 冒险与探索玩法 (Adventure & Exploration)

### 1. 动态环境与特殊方块 (Dynamic Environments & Special Blocks)
*   **冰火山喷泉方块 (Cryovolcanic Geyser Block):** 
    自然生成于“冰火山断崖”群系的特殊方块。它会周期性地喷发高压的液态氨与冰晶。踩在上面的实体会被赋予极大的垂直动能（击飞），玩家可以利用它配合滑翔装备快速跨越 Y 轴高差。
    A special block naturally generated in the "Cryovolcanic Cliffs" biome. It periodically erupts high-pressure liquid ammonia and ice crystals. Entities standing on it receive massive vertical kinetic energy (launched upward), which players can use alongside gliding gear to quickly traverse Y-axis elevations.

### 2. 探索遗迹 / 地牢 (Exploration Structures)
*   **托林晶洞与地下冰虫巢穴 (Tholin Geodes & Underground Hives):**
    生成在迷宫冰原下方的深层洞穴。墙壁上长满发光的变异有机晶体（高级合成材料）。破坏晶体有概率释放毒气并惊醒潜伏在巢穴中的敌对生物。
    Deep caves generating beneath the Polar Labyrinths. Walls are covered in glowing mutated organic crystals (advanced crafting materials). Breaking these crystals has a chance to release toxic gas and awaken hostile mobs lurking in the hive.
*   **废弃的先驱者前哨站 (Abandoned Pioneer Outposts) [生成权重低 / Low Generation Weight]:**
    半掩埋在荒原或沙丘中的科幻废墟。内部包含休眠的探测器和被锁定的旧日科技储藏箱。
    Sci-fi ruins half-buried in wastelands or dunes, containing dormant probes and locked stashes of old technology.

### 3. 工业防卫战：甲烷开采事件 (Industrial Defense: Methane Extraction Event)
该维度最核心的终局资源获取机制。
The core endgame resource acquisition mechanic for this dimension.
*   **机制核心 (Core Mechanics):**
    在深渊群系的底部，会罕见地生成一种 **“甲烷池核心方块” (Methane Pool Core Block)**。玩家无法用桶直接带走无限的燃料，必须合成并放置一台 **“特制甲烷泵” (Special Methane Pump)** 在核心方块上方。
    At the bottom of the Abyss biome, a rare **"Methane Pool Core Block"** can be found. Players cannot scoop infinite fuel directly with buckets; they must craft and place a **"Special Methane Pump"** above the core block.
*   **事件触发 (Event Trigger):**
    当泵启动并开始抽取液体时，在后端会向总线抛出一个 `ForgeEvent`。巨大的轰鸣声和能量波动会吸引深渊中的怪物。这会触发类似塔防的波次攻击，玩家必须在开采进度完成前保护泵站不被怪物摧毁。结合 Mixins 等技术，可以高度自定义每波怪物的生成逻辑和强度。
    When the pump is activated and begins extracting liquid, it fires a `ForgeEvent` on the backend. The massive noise and energy fluctuation will attract monsters from the abyss. This triggers a tower-defense-style wave attack, where players must protect the pump from being destroyed before the extraction process finishes. Using technologies like Mixins, the spawning logic and intensity of each monster wave can be highly customized.