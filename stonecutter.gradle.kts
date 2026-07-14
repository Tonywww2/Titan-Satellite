plugins { id("dev.kikugie.stonecutter") }

// 当前编辑/链接的活动版本，必须恰好赋值一次
stonecutter active "1.20.1-forge"

stonecutter parameters {
    // 从节点名取加载器：1.20.1-forge -> forge
    val loader = current.project.substringAfterLast('-')
    // 启用版本化注释常量：//? if forge { ... } / //? if neoforge { ... }
    constants { match(loader, "forge", "neoforge") }
}

// ---------------------------------------------------------------------------------------------------
// 一条命令发布所有加载器：对每个 Stonecutter 节点跑 publishMods（CurseForge 上传，见 build.gradle.kts），
// 与当前活动节点无关。
//   ./gradlew publishAllVersions
// ---------------------------------------------------------------------------------------------------
tasks.register("publishAllVersions") {
    group = "publishing"
    description = "Builds and publishes every Minecraft/loader version to CurseForge."
    dependsOn(stonecutter.tasks.named("publishMods").map { it.values })
}

// 各加载器产物串行上传，避免 CurseForge API 限流。
stonecutter.tasks.order("publishCurseforge")
