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

*   **重力 (Gravity):** 低重力环境（相当于地球的 14%）。玩家拥有永久的跳跃提升和缓慢降落效果。 / Low gravity (14% of Earth's). Players have permanent Jump Boost and Slow Falling.
*   **温度与空气 (Temperature & Oxygen):** 极寒（-179°C），无氧环境。玩家暴露在地表需穿着保暖与供氧装备。 / Extreme cold (-179°C) and no oxygen. Thermal and oxygen gear are required on the surface.
*   **光照与天气 (Lighting & Weather):** 浓厚的橙黄色大气层，地表光照极低。普通降雨替换为“液态甲烷雨”。 / Dense orange haze, very low surface light. Normal rain is replaced with "Liquid Methane Rain."
*   **高度限制 (Build Limit):** Y = 0 至 Y = 320。

---

## 一、 地形与群系分布 (Terrain & Biomes)

### 1. 液态甲烷深渊 (Methane Abyss) [Y: 0 - 64]
由坚硬的黑石/玄武岩构成的陡峭峡谷。
Steep canyons formed of hard blackstone and basalt.
*   **特异地貌 (Special Feature):** **甲烷海 (Methane Mare) [Y: 0 - 45]**。峡谷底部被液态甲烷完全淹没。 / Canyon bottoms flooded entirely with liquid methane.

### 2. 撞击陨坑荒原 (Cratered Wastelands) [Y: 64 - 110]
相对平坦的冰冻荒原，由碎冰和砾石构成。
Relatively flat frozen wastelands made of crushed ice and gravel.
*   **特异地貌 (Special Feature):** **巨型陨石坑 (Giant Craters) [Y: 40 - 80]**。砸穿地层的巨大凹陷，底部露出微型甲烷湖。 / Massive depressions smashing through the crust, exposing miniature methane lakes.

### 3. 托林沙海 (Tholin Dune Seas) [Y: 100 - 170]
广阔的橙色沙丘区，表面覆盖着托林有机化合物粉末。
Vast orange dune fields covered in powdery tholin organics.
*   **特异地貌 (Special Feature):** **巨型沙脊 (Megayardangs) [Y: 140 - 190]**。高墙般的狭长山脊。 / Wall-like, long, and narrow sand ridges.

### 4. 极地迷宫冰原 (Polar Labyrinths) [Y: 160 - 240]
地势急剧抬升，冰层受溶解侵蚀形成的支离破碎的迷宫。
Elevation rises sharply, forming shattered, eroded ice mazes.
*   **特异地貌 (Special Feature):** **冰层天坑 (Ice Sinkholes) [Y: 120 - 160]**。隐藏的巨大冰窟窿，直通下层地形。 / Hidden massive sinkholes dropping directly to lower biomes.

### 5. 冰火山断崖 (Cryovolcanic Cliffs) [Y: 220 - 320]
由蓝冰和浮冰交错构成的极度险峻的垂直断崖。
Extremely steep vertical cliffs composed of Blue Ice and Packed Ice.
*   **特异地貌 (Special Feature):** **氨水火山口 (Ammonia Calderas) [Y: 280 - 320]**。山巅的凹陷区域。 / Sunken craters at the very peaks.

---

## 二、 特色生物群 (Custom Creatures)

### 1. 甲烷浮游体 (Aero-Jelly)
*   **类型 (Type):** 被动 (Passive)
*   **特征 (Features):** 漂浮在空中的气囊生物。 / Balloon-like creatures floating in the atmosphere.
*   **掉落物 (Drops):** 特殊的“浮游薄膜”（基础轻量化合成材料）。 / "Aero-Membrane" (basic lightweight crafting material).

### 2. 冰硅甲虫 (Cryo-Scavenger)
*   **类型 (Type):** 中立 (Neutral)
*   **特征 (Features):** 拥有坚硬冰晶外壳的节肢动物。 / Arthropods with tough ice-crystal shells.
*   **掉落物 (Drops):** “冰晶甲壳”（用于合成或升级抗寒护甲的硬化材料）。 / "Cryo-Carapace" (hardening material for crafting/upgrading cold-resistant armor).

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