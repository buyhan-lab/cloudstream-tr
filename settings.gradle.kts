rootProject.name = "CloudstreamPlugins"

File(rootDir, ".").listFiles()?.filter { it.isDirectory }?.forEach { dir ->
    if (File(dir, "build.gradle.kts").exists()) include(dir.name)
}
