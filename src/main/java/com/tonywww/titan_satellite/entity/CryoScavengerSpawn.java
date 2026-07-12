package com.tonywww.titan_satellite.entity;

import com.tonywww.titan_satellite.TitanSatellite;
import com.tonywww.titan_satellite.registry.TSEntities;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 冰硅甲虫的生成放置规则：陆地生成（中立生物，不要求黑暗），仅需下方为可生成方块，
 * 经 mod 总线 {@link SpawnPlacementRegisterEvent} 自订阅注册（不改主类）。
 * 生成注入见 data/titan_satellite/forge/biome_modifier/cryo_scavenger_spawn.json。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CryoScavengerSpawn {

    private CryoScavengerSpawn() {
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(TSEntities.CRYO_SCAVENGER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
