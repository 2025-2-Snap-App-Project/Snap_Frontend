pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        resolutionStrategy {
            eachPlugin {
                if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
                    useModule("com.google.android.gms:oss-licenses-plugin:0.10.7")
                }
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SnapProject"
include(":app")
