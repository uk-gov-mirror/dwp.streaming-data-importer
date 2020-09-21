rootProject.name = "streaming-data-importer"

buildCache {
    local<DirectoryBuildCache>{
        directory = File(settingsDir, "build-cache")
    }
}
