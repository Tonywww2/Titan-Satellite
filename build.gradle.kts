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
    mixin { defaultRefmapName = "${property("mod.id")}.refmap.json" }
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
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())

    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${property("vers.deps.fml")}")
    } else {
        "forge"("net.minecraftforge:forge:$mcVersion-${property("vers.deps.fml")}")
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
