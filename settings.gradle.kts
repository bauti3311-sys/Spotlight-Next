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
  ivy {
   name = "SpotifyOfficialPinned"
   url = uri("https://raw.githubusercontent.com/spotify/android-sdk/5aa4d62465f61a0677081ae9a3108177d0365fc3/app-remote-lib")
   patternLayout { artifact("spotify-app-remote-release-[revision].[ext]") }
   metadataSources { artifact() }
   content { includeModule("com.spotify.android", "app-remote") }
  }
 }
}
rootProject.name = "Spotlight"
include(":app")
