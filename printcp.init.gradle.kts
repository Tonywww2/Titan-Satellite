gradle.projectsEvaluated {
    rootProject.subprojects.filter { it.path == ":1.21.1-neoforge" }.forEach { p ->
        p.tasks.register("dumpCompileCp") {
            doLast {
                val ss = p.extensions.getByType(org.gradle.api.tasks.SourceSetContainer::class.java)
                val cp = ss.getByName("main").compileClasspath.asPath
                java.io.File(rootProject.projectDir, "build/neoforge-cp.txt").writeText(cp)
                println("WROTE build/neoforge-cp.txt (" + cp.length + " chars)")
            }
        }
    }
}
