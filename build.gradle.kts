plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
}

// 加载器由节点 gradle.properties 的 loom.platform 决定（当前节点为 forge）
val loader = property("loom.platform").toString()
val mcVersion = property("vers.mcVersion").toString()
// 预先取出 mod.id（Project 作用域）：runConfigs 的 lambda 里 property(String) 会命中
// RunConfigSettings.property（设 VM 属性、返回 Unit）而非 Project.property，故在外层取好。
val modId = property("mod.id").toString()


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
        // Forge datagen 运行配置（GatherDataEvent）。data() 套用 Forge userdev 的 "data" 运行模板
        // （BootstrapLauncher + --launchTarget forgedatauserdev），再补 datagen 参数：
        // 输出到独立的根 src/generated/resources（与手写 src/main/resources 分开！Forge datagen 会
        // 修剪 --output 目录下未生成的文件，若就地输出会删光手写 JSON）；--existing 指向
        // src/main/resources 以校验已有贴图/模型引用。仅 Forge 节点可用（data() 仅 Forge-like）。
        runConfigs.create("data") {
            data()
            programArgs(
                "--mod", modId,
                "--all",
                "--output", rootProject.file("src/generated/resources").absolutePath,
                "--existing", rootProject.file("src/main/resources").absolutePath,
            )
        }
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
    maven {                                    // Team Resourceful 基础库（bytecodecs / yabn）
        name = "TeamResourceful"
        url = uri("https://maven.teamresourceful.com/repository/maven-public/")
        content { includeGroup("com.teamresourceful") }
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

        // Ad Astra 及其前置一律 modImplementation：tiny-remapper 重映射 Ad Astra 时需要完整的
        // CodecRecipe(resourceful-lib)→Recipe 层级在 remap classpath 上，否则 SpaceStationRecipe
        // 的 getType()/getSerializer() 覆写不会被重映射（残留 SRG m_6671_）→ 运行期 AbstractMethodError。
        "modImplementation"("maven.modrinth:ad-astra:1.15.20")
        "modImplementation"("maven.modrinth:resourceful-lib:2.1.29")     // Ad Astra 前置（≥2.1.23）
        // Resourceful Config 用 Forge 版本 ID 锁定：按版本号 2.1.3 会命中同号的 Fabric 包 → 崩溃。
        "modImplementation"("maven.modrinth:resourceful-config:DERs8u7v") // Ad Astra 前置（Forge 2.1.3）
        "modImplementation"("maven.modrinth:botarium:2.3.4")             // Ad Astra 前置（≥2.3.0）
        // 以下 JiJ 内嵌库 Loom dev 不解压，需显式补齐到 Forge 运行库。
        "forgeRuntimeLibrary"("io.github.llamalad7:mixinextras-common:0.3.2") // Ad Astra mixin @WrapOperation
        "forgeRuntimeLibrary"("com.teamresourceful:bytecodecs:1.0.2")   // resourceful-lib 内嵌
        "forgeRuntimeLibrary"("com.teamresourceful:yabn:1.0.3")         // resourceful-lib 内嵌
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

// 将 datagen 生成的资源（根 src/generated/resources）并入主源集，随构建打包。
// 生成目录与手写 src/main/resources 分开；两者不得有同名文件（否则 processResources 重复）。
sourceSets["main"].resources.srcDir(rootProject.file("src/generated/resources"))
