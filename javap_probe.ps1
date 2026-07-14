$ErrorActionPreference = 'SilentlyContinue'
$jar = Get-ChildItem "$env:USERPROFILE\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\neoforge-21.1.235-minecraft-merged-mojang" -Recurse -Include *.jar | Select-Object -First 1 -ExpandProperty FullName
function Probe($cls, $pat) {
  "==== $cls ===="
  javap -classpath $jar $cls 2>$null | Select-String -Pattern $pat | ForEach-Object { $_.Line.Trim() }
  ""
}
Probe 'net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent' 'public|register|Operation'
Probe 'net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent$Operation' 'REPLACE|OR|AND'
Probe 'net.minecraft.world.entity.SpawnPlacements' 'class SpawnPlacements|Type'
Probe 'net.minecraft.world.entity.SpawnPlacementTypes' 'ON_GROUND|NO_RESTRICTIONS|public'
Probe 'net.minecraft.world.level.pathfinder.PathType' 'WATER|public'
Probe 'net.minecraft.world.entity.Mob' 'checkMobSpawnRules|finalizeSpawn|setPathfindingMalus'
Probe 'net.minecraft.world.entity.monster.Monster' 'checkMonsterSpawnRules'
