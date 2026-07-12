package com.tonywww.titan_satellite.entity;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 失控探测器的生成放置规则（mod 总线自订阅，不改主类）：地面、标准怪物生成条件。
 * 具体在哪些群系生成由 forge:add_spawns biome_modifier 控制（见
 * data/titan_satellite/forge/biome_modifier/corrupted_probe_spawn.json）。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CorruptedProbeSpawn {

    private CorruptedProbeSpawn() {
    }

    @SubscribeEvent
    public static void register(SpawnPlacementRegisterEvent event) {
        event.register(TSEntities.CORRUPTED_PROBE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
}
