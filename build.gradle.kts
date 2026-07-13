plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
}

// 加载器由节点 gradle.properties 的 loom.platform 决定（当前节点为 forge）
val loader = property("loom.platform").toString()
val mcVersion = property("vers.mcVersion").toString()


group = property("mod.group").toString()
version = "${property("mod.version")}+$mcVersion"
base.archivesName = "${property("mod.id")}-$loader"

// 1.20.1 -> Java 17；>=1.20.6 -> Java 21（为将来多版本预留）
val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.6")) 21 else 17

loom {
    silentMojangMappingsLicense()
    // Mixin（Loom 内建），两版共用同一配置；Loom 生成 refmap
    mixin { 
        useLegacyMixinAp = true
        defaultRefmapName = "${property("mod.id")}.refmap.json" 
    }
    // Forge 开发环境需显式注册 mixin 配置——Architectury Loom 的 runServer/runClient 不会
    // 自动识别 mods.toml 的 [[mixins]]，缺了这行 mixin 在 dev 下静默不加载（NeoForge 原生识别）
    if (loader == "forge") {
        forge { mixinConfig("${property("mod.id")}.mixins.json") }
    }
    // 只给活动节点生成 IDE 运行配置，run 目录集中到根
    if (stonecutter.current.isActive) {
        runConfigs.all {
            ideConfigGenerated(true)
            runDir("../../run")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.minecraftforge.net/")
    // 开发期运行时 mod 依赖源
    maven("https://maven.blamejared.com")    // JEI
    maven("https://api.modrinth.com/maven")  // Jade / Architectury API / Integrated Dynamics 系列
    maven {                                    // FTB Library / FTB Chunks（Forge 1.20.1 仅 CurseForge）
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${property("vers.deps.fml")}")
    } else {
        "forge"("net.minecraftforge:forge:$mcVersion-${property("vers.deps.fml")}")

        // 开发期运行时 mod（仅 Forge 节点；供 runClient/runServer 测试，不进入发布产物）
        // JEI（配方/物品查看）
        "modRuntimeOnly"("mezz.jei:jei-1.20.1-forge:15.20.0.106")
        // 方块/实体信息 HUD：The One Probe（Jade 的 StringRenderOutputMixin @Shadow this$0 在 Loom
        // dev 反混淆环境下定位失败崩溃；TOP 独立无前置、dev 友好，作为 Jade 替代）
        "modRuntimeOnly"("maven.modrinth:the-one-probe:1.20.1-10.0.3")
        // Architectury API（FTB 前置）
        "modRuntimeOnly"("maven.modrinth:architectury-api:9.2.14+forge")
        // Integrated Dynamics 系列（含前置 Cyclops Core / Common Capabilities）
        "modRuntimeOnly"("maven.modrinth:cyclops-core:1.20.1-1.22.1")
        "modRuntimeOnly"("maven.modrinth:common-capabilities:1.20.1-2.9.11")
        "modRuntimeOnly"("maven.modrinth:integrated-dynamics:1.20.1-1.30.7")
        "modRuntimeOnly"("maven.modrinth:integrated-tunnels:1.20.1-1.9.3")
        "modRuntimeOnly"("maven.modrinth:integrated-crafting:1.20.1-1.4.6")
        "modRuntimeOnly"("maven.modrinth:integrated-terminals:1.20.1-1.6.20")

        // FTB Library（Forge 1.20.1 仅 CurseForge，经 CurseMaven 引入）
        "modRuntimeOnly"("curse.maven:ftb-library-forge-404465:8226927") // 2001.2.13
        // 小地图：JourneyMap（独立无前置、事件驱动、极少 mixin，Loom dev 环境最稳）。
        // 换过两次坑：① FTB Chunks 的 PistonBaseBlockMixin 本地变量捕获描述符不匹配 → 崩溃；
        // ② Xaero's Minimap 的 xaero.lib（JiJ 内嵌 jar）在 Loom dev 下不加载 → NoClassDefFoundError。
        "modRuntimeOnly"("maven.modrinth:journeymap:1.20.1-5.10.3-forge")
    }
}

tasks {
    // 关键：用 Stonecutter 预处理后的源码构建，而非原始源码。
    // Loom 1.11 惰性注册 createMinecraftArtifacts（晚于本块求值），故用 configureEach
    // 惰性挂依赖，避免配置期 named() 抛 UnknownTaskException。
    configureEach {
        if (name == "createMinecraftArtifacts") {
            dependsOn("stonecutterGenerate")
        }
    }

    processResources {
        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "group" to project.property("mod.group"),
            "authors" to project.property("mod.authors"),
            "description" to project.property("mod.description"),
            "license" to project.property("mod.license"),
        )
        inputs.properties(props)
        filesMatching("META-INF/mods.toml") { expand(props) }
    }
    withType<JavaCompile> { options.release = javaVersion }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}
