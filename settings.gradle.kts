rootProject.name = "specification-demo"

/**
 * 如需关闭镜像加速, 请直接移除本文件中的 `pluginManagement` 和 `dependencyResolutionManagement` 代码块,
 * 然后在 `build.gradle.kts` 文件中添加 repositories 代码块并配置 `mavenCentral()`
 *
 * 下面的 `dependencyResolutionManagement` 中的 `unstable api 警告` 目前无法避免
 */
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        mavenCentral()
        google()
    }
}