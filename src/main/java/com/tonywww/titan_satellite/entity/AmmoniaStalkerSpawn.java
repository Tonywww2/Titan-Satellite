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
 * 氨泉掠食者的生成放置规则：陆地、按原版怪物生成条件（黑暗），
 * 经 mod 总线 {@link SpawnPlacementRegisterEvent} 自订阅注册（不改主类）。
 * 生成注入见 data/titan_satellite/forge/biome_modifier/ammonia_stalker_spawn.json。
 */
@Mod.EventBusSubscriber(modid = TitanSatellite.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AmmoniaStalkerSpawn {

    private AmmoniaStalkerSpawn() {
    }

    @SubscribeEvent
    public static void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(TSEntities.AMMONIA_STALKER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
