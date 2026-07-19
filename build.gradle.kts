plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
    id("me.modmuss50.mod-publish-plugin") version "2.1.1"
}

// 加载器由节点 gradle.properties 的 loom.platform 决定
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
    // Mixin（Loom 内建），两版共用同一配置；Loom 生成 refmap。
    // useLegacyMixinAp = true 是 Loom 配置 defaultRefmapName 的前置（两版都需要）。
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
    maven("https://maven.covers1624.net/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${property("vers.deps.fml")}")

        // 饰品 API（Curios）：本模组饰品的实现依赖。modImplementation=编译期+dev 运行期（Loom 重映射）；
        // 发布版由 neoforge.mods.toml 声明 required 依赖、用户自行安装 Curios。
        "modImplementation"("maven.modrinth:curios:yohfFbgD")      // Curios 9.5.1+1.21.1 (NeoForge)

        // GeckoLib（实体动画库）：甲烷微浮群的模型/动画渲染依赖。modImplementation=编译期+dev 运行期；
        // 发布版由 neoforge.mods.toml 声明 required 依赖、用户自行安装 GeckoLib。用 Modrinth 版本 ID 锁定。
        "modImplementation"("maven.modrinth:geckolib:tPkJmim6")    // GeckoLib 4.9.2 (NeoForge 1.21.1)

        // 可选前置（编译期 + dev 运行期）。modCompileOnly=编译期访问；modLocalRuntime=仅 dev 运行期
        // （不进发布产物、不强制用户装）。Mek/FD 的 NeoForge 包自包含（无 JiJ、仅需 minecraft+neoforge），
        // dev 运行期可用；Create 保持 compileOnly（同 Forge：client mixin 在 Loom dev 重映射崩溃）。
        // 发布版运行期联动由 mods.toml 可选依赖 + mod_loaded 门控的数据配方保证。
        // 用 Modrinth 版本 ID 锁定具体文件，避开 Fabric/Forge 同号包命中的坑。
        "modCompileOnly"("maven.modrinth:create:UjX6dr61")          // Create 6.0.10 (NeoForge 1.21.1)
        "modCompileOnly"("maven.modrinth:mekanism:5KzzycBT")        // Mekanism 10.7.19.85 (NeoForge 1.21.1)
        "modCompileOnly"("maven.modrinth:farmers-delight:GbNuOZ4S") // Farmer's Delight 1.21.1-1.3.2 (NeoForge)
        "modLocalRuntime"("maven.modrinth:mekanism:5KzzycBT")
        "modLocalRuntime"("maven.modrinth:farmers-delight:GbNuOZ4S")
    } else {
        "forge"("net.minecraftforge:forge:$mcVersion-${property("vers.deps.fml")}")

        // 饰品 API（Curios）：本模组饰品的实现依赖。modImplementation=编译期+dev 运行期（Loom 重映射）；
        // 发布版由 mods.toml 声明 mandatory 依赖、用户自行安装 Curios。
        "modImplementation"("maven.modrinth:curios:IPQlZkz1")      // Curios 5.14.1+1.20.1 (Forge)

        // GeckoLib（实体动画库）：甲烷微浮群的模型/动画渲染依赖。modImplementation=编译期+dev 运行期；
        // 发布版由 mods.toml 声明 mandatory 依赖、用户自行安装 GeckoLib。用 Modrinth 版本 ID 锁定。
        "modImplementation"("maven.modrinth:geckolib:aC5KMoNg")    // GeckoLib 4.8.4 (Forge 1.20.1)
        // GeckoLib 4.8.4 的 MoLang 依赖 mclib 以 JiJ 内嵌；Loom dev 不解压 JiJ，需从 jar 解压到 libs/ 并显式
        // 补到 Forge 运行库，否则运行期 NoClassDefFoundError: com/eliotlash/mclib/math/IValue。
        // (解压来源：geckolib jar 的 META-INF/jarjar/mclib-20.jar。NeoForge 4.9.2 已内置 MoLang，无需此步。)
        "forgeRuntimeLibrary"(files(rootProject.file("libs/mclib-20.jar")))

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
        "forgeRuntimeLibrary"("io.github.llamalad7:mixinextras-common:0.4.1") // Ad Astra + Farmer's Delight mixin（FD 内嵌 0.3.6，0.4.1 向下兼容 Ad Astra 的 ≥0.3.2）
        "forgeRuntimeLibrary"("com.teamresourceful:bytecodecs:1.0.2")   // resourceful-lib 内嵌
        "forgeRuntimeLibrary"("com.teamresourceful:yabn:1.0.3")         // resourceful-lib 内嵌

        // 可选前置（编译期 + dev 运行期，供联动配方实测）。modCompileOnly=编译期访问；
        // modLocalRuntime=仅 dev 运行期（不进发布产物、不强制用户装）。发布版运行期联动由
        // mods.toml 可选依赖 + mod_loaded 门控的数据配方保证。
        // 用 Modrinth 版本 ID 锁定具体文件，避开 Fabric/Forge 同号包命中的坑。
        "modCompileOnly"("maven.modrinth:create:8amzvn9x")          // Create 6.0.8 (Forge 1.20.1)
        "modCompileOnly"("maven.modrinth:mekanism:uxe1WQp4")        // Mekanism 10.4.16.80 (Forge 1.20.1)
        "modCompileOnly"("maven.modrinth:farmers-delight:CsjS7EkP") // Farmer's Delight 1.20.1-1.3.2 (Forge)
        // Mekanism / FD 自包含（无级联 JiJ mod 依赖），dev 运行期可用。
        // Create 不上 dev 运行期：其 client mixin（HumanoidArmorLayerMixin）在 Loom dev 重映射环境下
        // 描述符不匹配崩溃（同 Jade/FTB/Xaero 的 dev-mixin 坑）；datagen 走 client 环境故一并被拖崩。
        // Create 联动配方仍对发布版用户生效（mods.toml 可选依赖 + mod_loaded 数据配方）。
        "modLocalRuntime"("maven.modrinth:mekanism:uxe1WQp4")
        "modLocalRuntime"("maven.modrinth:farmers-delight:CsjS7EkP")
        // Thermal 系列（CoFH Core → Thermal Foundation → Thermal Expansion；配方类型/序列化器 thermal:*
        // 由 mod id `thermal` = Thermal Core 注册）。核心 mod `thermal`（thermal_core）以 JiJ 内嵌于
        // Thermal Foundation，Loom dev 不解压内嵌 jar → 需手动解压到 libs/（见 build/extract_thermal_jij.ps1）
        // 再以 modLocalRuntime(files(...)) 显式补到 dev 运行期（Loom 会重映射本地 mod jar），否则缺
        // `thermal` 依赖致加载失败。发布版联动由 mod_loaded:thermal 门控的数据配方保证。
        "modCompileOnly"("maven.modrinth:cofh-core:kglS53Hd")           // CoFH Core 11.0.2 (Forge 1.20.1)
        "modCompileOnly"("maven.modrinth:thermal-foundation:44ilyZbi") // Thermal Foundation 11.0.6
        "modCompileOnly"("maven.modrinth:thermal-expansion:Ux2Z0ow1")  // Thermal Expansion 11.0.1
        "modLocalRuntime"("maven.modrinth:cofh-core:kglS53Hd")
        "modLocalRuntime"("maven.modrinth:thermal-foundation:44ilyZbi")
        "modLocalRuntime"("maven.modrinth:thermal-expansion:Ux2Z0ow1")
        // thermal_core（mod id `thermal`）：从 Thermal Foundation 的 JiJ 解压到 libs/ 后显式补齐。
        "modLocalRuntime"(files(rootProject.file("libs/thermal_core-1.20.1-11.0.6.24.jar")))
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
            "pack_format" to project.property("vers.packFormat"),
            "fluid_loader" to project.property("vers.fluidLoader"),
            "loader" to loader,
        )
        inputs.properties(props)
        // Forge 读 META-INF/mods.toml，NeoForge 读 META-INF/neoforge.mods.toml；两者都做占位展开，
        // 再按当前加载器排除另一个，避免多余元数据进包。
        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "pack.mcmeta", "assets/titan_moon/models/item/liquid_methane_bucket.json", "assets/titan_moon/models/item/liquid_ammonia_bucket.json", "data/titan_moon/dimension/titan_orbit.json")) { expand(props) }
        exclude(if (loader == "neoforge") "META-INF/mods.toml" else "META-INF/neoforge.mods.toml")
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

// ---------------------------------------------------------------------------------------------------
// CurseForge 自动发布（me.modmuss50.mod-publish-plugin）。
// 上传当前加载器节点的 remap 产物；根 stonecutter.gradle.kts 的 `publishAllVersions` 一次发布所有加载器。
// 机密/id 惰性读取（仅发布任务执行时），普通 build 不受影响：
//   - CURSEFORGE_TOKEN 环境变量（CI 首选），或用户级 ~/.gradle/gradle.properties 的 curseforge.token（切勿提交）；
//   - curseforge.projectId 数字 id 来自 CurseForge 项目页「About Project」（非机密，放 gradle.properties；暂留空待填）。
// 用法：
//   ./gradlew publishAllVersions                         # 所有加载器
//   ./gradlew :1.20.1-forge:publishMods                  # 单个加载器
//   ./gradlew publishAllVersions -Ppublish.dryRun=true   # 走完整流程但不上传（见 scripts/dryrun.ps1）
// ---------------------------------------------------------------------------------------------------
publishMods {
    // Architectury Loom 的最终（remap 后）产物——不是原始 jar 任务输出。
    file = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile }
    version = project.version.toString() // 如 0.1.0+1.20.1——各加载器经 mcVersion 唯一
    displayName = "${property("mod.name")} ${property("mod.version")} - MC $mcVersion ($loader)"
    modLoaders.add(loader)
    type = STABLE

    // 校验整条流程但不上传：-Ppublish.dryRun=true
    dryRun = providers.gradleProperty("publish.dryRun").map { it.toBoolean() }.orElse(false)

    changelog = providers.environmentVariable("CHANGELOG")
        .orElse(providers.provider { rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() })
        .orElse("See the GitHub releases page.")

    curseforge {
        projectId = providers.gradleProperty("curseforge.projectId")
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            .orElse(providers.gradleProperty("curseforge.token"))
        minecraftVersions.add(mcVersion)
        javaVersions.add(JavaVersion.toVersion(javaVersion))
        // 维度内容 mod：客户端 + 服务端都需要（插件要求至少一个环境）。
        client = true
        server = true
    }
}
