import java.util.Properties
plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose"); id("io.github.takahirom.roborazzi") }
val releaseIdentity = Properties().apply {
    rootProject.file("release.properties").inputStream().use { load(it) }
}

// Opt-in diagnostic profile only; standard Preview/Release builds keep R8 enabled.
val optimizeRaycastPreview = providers.gradleProperty("spotlight.preview.optimize").orElse("true").get().let {
 require(it == "true" || it == "false") { "spotlight.preview.optimize must be true or false" }
 it == "true"
}

android {
 namespace = "dev.bautista.spotlight"
 compileSdk = 35
 buildToolsVersion = "35.0.0"
 defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; applicationId = "dev.bautista.spotlight.next"; minSdk = 26; targetSdk = 35; versionCode = releaseIdentity.getProperty("version.code").toInt(); versionName = releaseIdentity.getProperty("version.name") }
 testOptions { unitTests.isIncludeAndroidResources = true }
 // Compile the isolated WhatsApp provider against Android in tests only; never package it.
 sourceSets.getByName("test").java.srcDir(rootProject.file("experiments/whatsapp/android"))
 buildFeatures { compose = true; aidl = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 val privateSigning = Properties().apply {
  val source = rootProject.file("signing/credentials.properties")
  if (source.exists()) source.inputStream().use { load(it) }
 }
 val hasPrivateSigning = privateSigning.getProperty("storePassword") != null && rootProject.file("signing/spotlight-debug.keystore").exists()
 if (hasPrivateSigning) signingConfigs {
  create("projectPrivate") {
   storeFile = rootProject.file("signing/spotlight-debug.keystore")
   storePassword = privateSigning.getProperty("storePassword")
   keyAlias = privateSigning.getProperty("keyAlias")
   keyPassword = privateSigning.getProperty("keyPassword")
  }
 }
 buildTypes { getByName("debug") { if (hasPrivateSigning) signingConfig = signingConfigs.getByName("projectPrivate") }; release { isMinifyEnabled = true; isShrinkResources = true; ndk { abiFilters += "arm64-v8a" }; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") }
  // Independent Raycast candidate; preserve its recovered historical certificate.
  // Never replaces the production or Review packages.
  create("raycastPreview") {
   initWith(getByName("debug"))
   isDebuggable = false
   isMinifyEnabled = optimizeRaycastPreview
   isShrinkResources = optimizeRaycastPreview
   proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
   ndk { abiFilters += "arm64-v8a" } // Personal preview targets Galaxy Tab A9+ and Moto G86.
   applicationIdSuffix = ".raycastpreview"
   versionNameSuffix = if (optimizeRaycastPreview) "-raycast-preview-native57-v66" else "-raycast-preview-native57-v66-experimental"
   // Never substitute a generated debug certificate for the historical Preview identity.
   // CI/tests can configure the project without a key; that case yields unsigned output.
   signingConfig = if (hasPrivateSigning) signingConfigs.getByName("projectPrivate") else null
   matchingFallbacks += listOf("debug")
  }
  // Independent, release-optimized preview. Never an update of the historically signed app.
  create("preview") {
   initWith(getByName("release"))
   applicationIdSuffix = ".preview"
   versionNameSuffix = "-preview"
   signingConfig = signingConfigs.getByName("debug")
   matchingFallbacks += listOf("release")
  }
  // A separate local-signature sandbox; preserves both installed production and old Preview.
  create("universalPreview") {
   initWith(getByName("preview"))
   applicationIdSuffix = ".preview.universal"
   versionNameSuffix = "-universal-preview"
   matchingFallbacks += listOf("release")
  }
  // The previous transient Preview key is unavailable. Keep its installed data intact.
  create("functionalPreview") {
   initWith(getByName("universalPreview"))
   applicationIdSuffix = ".preview.p1"
   versionNameSuffix = "-p1-preview"
   matchingFallbacks += listOf("release")
  }
  // Independent review explicitly authorized on 17/10. Does not reuse lost P1 identity.
  create("review") {
   initWith(getByName("release"))
   applicationIdSuffix = ".review"
   versionNameSuffix = "-review"
   signingConfig = null // signed externally with the dedicated Review key, never historical secrets
   matchingFallbacks += listOf("release")
  }

 }
}
// Review updates keep their original certificate and advance independently of production.
androidComponents {
 onVariants(selector().withBuildType("raycastPreview")) { variant ->
  // Native57 validation; avoid experimental58/59 numbering. Only this Preview changes.
  variant.outputs.forEach { output -> output.versionCode.set(66) }
 }
 onVariants(selector().withBuildType("review")) { variant ->
  variant.outputs.forEach { output -> output.versionCode.set(releaseIdentity.getProperty("version.code").toInt() + 1) }
 }
}
dependencies {
 implementation("com.spotify.android:app-remote:0.8.0@aar")
 implementation("com.google.code.gson:gson:2.11.0")
 implementation("com.microsoft.onnxruntime:onnxruntime-android:1.23.2")
 androidTestImplementation("androidx.test.ext:junit:1.2.1")
 androidTestImplementation("androidx.test:runner:1.6.2")
 implementation("dev.rikka.shizuku:api:13.1.5")
 implementation("dev.rikka.shizuku:provider:13.1.5")
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.animation:animation")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.lifecycle:lifecycle-service:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.savedstate:savedstate-ktx:1.2.1")
 implementation("androidx.datastore:datastore-preferences:1.1.1")
 implementation("androidx.documentfile:documentfile:1.0.1")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
 implementation("com.squareup.okhttp3:okhttp:4.12.0")
 implementation("com.google.mlkit:text-recognition:16.0.1")
 implementation("com.google.mlkit:image-labeling:17.0.9")
 implementation("com.google.mlkit:translate:17.0.3")
 implementation("com.google.mlkit:language-id:17.0.6")
 implementation("com.tom-roush:pdfbox-android:2.0.27.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
 testImplementation("io.github.takahirom.roborazzi:roborazzi:1.29.0")
 testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.29.0")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.compose.ui:ui-test-junit4")
 debugImplementation("androidx.compose.ui:ui-test-manifest")
 add("raycastPreviewImplementation", "androidx.compose.ui:ui-test-manifest")
}

// Robolectric fetches its Android runtime inside the test JVM, outside Gradle's repositories.
tasks.withType<Test>().configureEach {
 if (providers.gradleProperty("spotlight.test.verbose").orNull == "true") {
  testLogging.showStandardStreams = true
  testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
 }
 systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
 listOf("http.proxyHost", "http.proxyPort", "https.proxyHost", "https.proxyPort", "javax.net.ssl.trustStore").forEach { key ->
  System.getProperty(key)?.let { systemProperty(key, it) }
 }
}
