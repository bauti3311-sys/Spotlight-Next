pluginManagement {
 repositories {
  google { url = uri("https://dl.google.com/android/maven2"); content { includeGroupByRegex("com\\.android.*"); includeGroupByRegex("androidx.*"); includeGroupByRegex("com\\.google.*") } }
  mavenCentral(); gradlePluginPortal()
 }
}
dependencyResolutionManagement {
 repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
 repositories {
  google { url = uri("https://dl.google.com/android/maven2"); content { includeGroupByRegex("com\\.android.*"); includeGroupByRegex("androidx.*"); includeGroupByRegex("com\\.google.*") } }
  mavenCentral()
 }
}
rootProject.name = "Spotlight"
include(":app")
