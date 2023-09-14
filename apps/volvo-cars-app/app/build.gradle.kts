import com.volvocars.dependencies.*
import com.volvocars.version.VersionCodeManager

plugins {
    id("com.android.application")
    id("com.apollographql.apollo")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.volvocars.dependencies")
    id("com.volvocars.versioning")
    kotlin("plugin.parcelize")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

fun gitBranch() = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}.standardOutput.asText.map { branch ->
    branch.replace("/", "__").trimEnd()
}.get()

android {
    val SERVER_BUILD_NUMBER = "SERVER_BUILD_NUMBER"
    val GIT_BRANCH = "GIT_BRANCH"
    val APP_CENTER_SECRET = "APP_CENTER_SECRET"
    val FIREBASE_PUSH_APP_ID = "FIREBASE_PUSH_APP_ID"
    val FIREBASE_WIRELESS_CAR_APP_ID = "FIREBASE_WIRELESS_CAR_APP_ID"

    compileSdk = sdk.compile
    ndkVersion = ndk.ndk

    defaultConfig {
        applicationId = "se.volvo.vcc"
        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
        resValue("string", "branded_app_name", "Volvo Cars")
        resourceConfigurations.addAll(
            listOf(
                "cs",
                "da",
                "de",
                "de-rLU",
                "el",
                "en",
                "en-rUS",
                "es",
                "et",
                "fi",
                "fr",
                "fr-rCA",
                "fr-rLU",
                "hu",
                "it",
                "lt",
                "lv-rLV",
                "nb",
                "nl",
                "pl",
                "pt",
                "pt-rBR",
                "ru",
                "sv",
                "tr",
                "uk",
                "zh-rCH",
                "ja",
                "ko",
                "zh-rTW",
                "vi"
            )
        )
        minSdk = sdk.min
        targetSdk = sdk.target
        multiDexEnabled = true
        testApplicationId = "se.volvo.vcc.test"
        testInstrumentationRunner = "se.volvo.vcc.testUtils.CustomTestRunner"
        buildConfigField("String", SERVER_BUILD_NUMBER, "\"\"")
        buildConfigField("String", APP_CENTER_SECRET, "\"\"")
        buildConfigField("String", GIT_BRANCH, "\"${gitBranch()}\"")
        versionName = "5.31.0"
        versionCode = VersionCodeManager.currentVersionCode(project)
    }

    signingConfigs {
        create("release") {
            keyAlias = "voc_key"
            keyPassword = "77e_oa9ezq6"
            storeFile =
                file("/Users/" + System.getProperty("user.name") + "/.android/oncall-release-key.keystore")
            storePassword = "77e_oa9ezq6"
        }
        create("beta") {
            keyAlias = "vocbeta"
            keyPassword = "77e_oa9ezq6"
            storeFile =
                file("/Users/" + System.getProperty("user.name") + "/.android/oncall-release-key.keystore")
            storePassword = "77e_oa9ezq6"
        }

        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("$rootDir/debug.keystore")
            storePassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders.putAll(
                mapOf(
                    "volvoAppIcon" to "@mipmap/ic_launcher_voc",
                    "crashlyticsCollectionEnabled" to "true",
                    "APPLOG_SCHEME" to "rangersapplog.1bc624689955d81b"
                )
            )
            buildConfigField("String", FIREBASE_PUSH_APP_ID, "\"1:972165137265:android:3dad6b43733bdbd3\"")
            buildConfigField("String", FIREBASE_WIRELESS_CAR_APP_ID, "\"se.volvo.vcc\"")
        }

        debug {
            // This build type is used as the Develop variant for automation and app center
            signingConfig = signingConfigs.getByName("debug")
            isTestCoverageEnabled = project.hasProperty("coverage")
            // When testCoverage is enabled, debugging won't be able to debug incoming parameters
            isMinifyEnabled = false

            val buildNumber = System.getenv("BUILD_NUMBER") ?: "0"
            buildConfigField("String", SERVER_BUILD_NUMBER, "\".$buildNumber\"")
            versionNameSuffix = ".$buildNumber-develop"
            manifestPlaceholders.putAll(
                mapOf(
                    "volvoAppIcon" to "@mipmap/ic_launcher_voc_dev",
                    "crashlyticsCollectionEnabled" to "true",
                    "APPLOG_SCHEME" to "rangersapplog.0e5eddb681f90589"
                )
            )
            val alwaysUpdateBuildId by extra { false }
            extra["alwaysUpdateBuildId"] = false
            buildConfigField("String", FIREBASE_PUSH_APP_ID, "\"1:32607580518:android:8123a996bca520313a18a7\"")
            buildConfigField("String", FIREBASE_WIRELESS_CAR_APP_ID, "\"se.volvo.vcc.beta\"")
        }

        create("internal") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            val buildNumber = System.getenv("BUILD_NUMBER") ?: "0"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ".$buildNumber-internal"
            matchingFallbacks.add("release")
            manifestPlaceholders.putAll(
                mapOf(
                    "volvoAppIcon" to "@mipmap/ic_launcher_voc_internal",
                    "crashlyticsCollectionEnabled" to "true",
                    "APPLOG_SCHEME" to "rangersapplog.1bc624689955d81b"
                )
            )
            buildConfigField("String", FIREBASE_PUSH_APP_ID, "\"1:32607580518:android:ae38b5bb75b0556f3a18a7\"")
            buildConfigField("String", FIREBASE_WIRELESS_CAR_APP_ID, "\"se.volvo.vcc.beta\"")
        }

        create("releaseCandidate") {
            signingConfig = signingConfigs.getByName("beta")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val buildNumber = System.getenv("BUILD_NUMBER") ?: "0"
            buildConfigField("String", SERVER_BUILD_NUMBER, "\".$buildNumber\"")
            versionNameSuffix = ".$buildNumber-releaseCandidate"
            matchingFallbacks.add("release")
            manifestPlaceholders.putAll(
                mapOf(
                    "volvoAppIcon" to "@mipmap/ic_launcher_voc",
                    "crashlyticsCollectionEnabled" to "true",
                    "APPLOG_SCHEME" to "rangersapplog.0e5eddb681f90589"
                )
            )
            buildConfigField("String", FIREBASE_PUSH_APP_ID, "\"1:32607580518:android:ae38b5bb75b0556f3a18a7\"")
            buildConfigField("String", FIREBASE_WIRELESS_CAR_APP_ID, "\"se.volvo.vcc.beta\"")
        }

        create("experimental") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    flavorDimensions += "store"

    productFlavors {
        create("gcm") {
            dimension = "store"
        }

        create("china") {
            dimension = "store"
        }
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }

    packagingOptions {
        resources {
            pickFirsts += "META-INF/androidx.compose.*"

            excludes += "MANIFEST.MF"
            excludes += "LICENSE-junit.txt"
            excludes += "**/attach_hotspot_windows.dll"

            excludes += "META-INF/androidx.[a-z]*"

            excludes += "META-INF/NOTICE**"
            excludes += "META-INF/LICENSE**"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/README.md"
            excludes += "META-INF/CHANGES"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/licenses/**"
            excludes += "META-INF/kotlinx**"
            excludes += "META-INF/com.google.**"
        }

        jniLibs {
            pickFirsts += "lib/arm64-v8a/libJni_wgs2gcj.so"
            pickFirsts += "lib/armeabi/libJni_wgs2gcj.so"
            pickFirsts += "lib/armeabi-v7a/libJni_wgs2gcj.so"
            pickFirsts += "lib/x86/libJni_wgs2gcj.so"
            pickFirsts += "lib/x86_64/libJni_wgs2gcj.so"
            jniLibs.keepDebugSymbols.add("*/mips/*.so")
            jniLibs.keepDebugSymbols.add("*/mips64/*.so")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("test") {
            java.srcDirs("$projectDir/src/testShared")
        }
        getByName("debug") {
            java.srcDirs("src/debug/java")
        }
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = androidx.compose.compilerVersion
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    if (name.startsWith("testChina")) {
        jvmArgs = listOf("-noverify")
    }
}

val gcmImplementation by configurations
val chinaImplementation by configurations

dependencies {
    implementation(project(":graphql"))
    // Desugaring for Java 8 language features
    coreLibraryDesugaring(desugar.desugar)

    // Kotlin
    implementation(kotlinx.serialization.json)

    // Lifecycle
    implementation(androidx.lifecycle.common.java8)
    implementation(androidx.lifecycle.runtime.ktx)
    debugImplementation(androidx.lifecycle.runtime.ktx)
    implementation(androidx.lifecycle.viewmodel.ktx)
    implementation(androidx.lifecycle.livedata.ktx)
    implementation(androidx.lifecycle.proccess)

    // AndroidX
    implementation(androidx.annotation)
    implementation(androidx.browser)
    implementation(androidx.core.ktx)
    implementation(androidx.fragment.ktx)
    implementation(androidx.appcompat)
    implementation(androidx.multidex)
    implementation(androidx.exifinterface)
    implementation(androidx.datastore.preferences)
    implementation(androidx.navigation.compose)

    implementation(Android.material)

    // Coil (image loader)
    implementation(coil.compose)

    // AMap
    // This is gonna removed when 4.0 map tab is gone. 5.0 use Rest API instead of SDK and is included in vocmaps.
    chinaImplementation(amap.threeDmap)
    chinaImplementation(amap.search)

    implementation(google.code.gson)

    // Java API to read, write, and modify Excel spreadsheets dynamically
    implementation(sourceforge.jexcelapi)

    // Firebase dependencies
    implementation(platform(firebase.bom))
    gcmImplementation(firebase.messaging)
    gcmImplementation(firebase.analytics)
    implementation(firebase.crashlytics)

    implementation(google.android.playservices.analytics)

    // Local modules
    implementation(project(":account-profile-apis"))

    implementation(project(":analytics"))
    implementation(project(":analytics:data"))
    implementation(project(":analytics:domain"))
    implementation(project(":analytics:parameters"))

    implementation(project(":app-lifecycle-domain"))

    implementation(project(":app-communication:api"))

    implementation(project(":app-communication:inbox:data"))
    implementation(project(":app-communication:inbox:domain"))
    implementation(project(":app-communication:inbox:ui"))

    implementation(project(":app-communication:messages:data"))
    implementation(project(":app-communication:messages:domain"))
    implementation(project(":app-communication:messages:ui"))

    implementation(project(":app-communication:user:data"))
    implementation(project(":app-communication:user:domain"))

    implementation(project(":app-communication:token:data"))
    implementation(project(":app-communication:token:domain"))

    implementation(project(":application:buildinfo:domain"))
    implementation(project(":application:buildinfo:data"))
    implementation(project(":application:deviceinfo:domain"))

    implementation(project(":cas-api"))

    implementation(project(":car:assets:data"))
    implementation(project(":car:assets:domain"))

    implementation(project(":car:air-purification:data"))
    implementation(project(":car:air-purification:domain"))
    implementation(project(":car:air-purification:ui"))

    implementation(project(":car:availability:data"))
    implementation(project(":car:availability:domain"))

    implementation(project(":car:c3-analytics:domain"))
    implementation(project(":car:c3-analytics:data"))

    implementation(project(":car:c3-lifecycle:domain"))
    implementation(project(":car:c3-lifecycle:data"))

    implementation(project(":car:climate:data"))
    implementation(project(":car:climate:domain"))
    implementation(project(":car:climate:ui"))

    implementation(project(":car:cloud:classic"))
    implementation(project(":car:cloud:invocation"))
    implementation(project(":car:cloud:services"))
    implementation(project(":car:cloud:status"))
    implementation(project(":car:cloud:usermanagement"))

    implementation(project(":car:datasharing:domain"))
    implementation(project(":car:datasharing:data"))

    implementation(project(":car:statistics:domain"))
    implementation(project(":car:statistics:data"))
    implementation(project(":car:statistics:ui"))

    implementation(project(":car:engine-remote-start:data"))
    implementation(project(":car:engine-remote-start:domain"))
    implementation(project(":car:engine-remote-start:ui"))

    implementation(project(":car:exterior:data"))
    implementation(project(":car:exterior:domain"))

    implementation(project(":car:information:data"))
    implementation(project(":car:information:domain"))

    implementation(project(":car:home:data"))
    implementation(project(":car:home:domain"))
    implementation(project(":car:home:ui"))

    implementation(project(":car:locks:data"))
    implementation(project(":car:locks:domain"))
    implementation(project(":car:locks:ui"))

    implementation(project(":car:charge:home-charging:domain"))
    implementation(project(":car:charge:home-charging:data"))
    implementation(project(":car:charge:home-charging:ui"))

    implementation(project(":car:charge:remote-charging:domain"))
    implementation(project(":car:charge:remote-charging:data"))
    implementation(project(":car:charge:remote-charging:ui"))

    implementation(project(":car:locator:data"))
    implementation(project(":car:locator:domain"))
    implementation(project(":car:locator:ui"))

    implementation(project(":car:feedback:domain"))
    implementation(project(":car:feedback:data"))

    implementation(project(":car:fuel:domain"))
    implementation(project(":car:fuel:data"))

    implementation(project(":car:journal:domain"))
    implementation(project(":car:journal:data"))

    implementation(project(":car:garage:data"))
    implementation(project(":car:garage:domain"))
    implementation(project(":car:garage:ui"))

    implementation(project(":car:remote-functions:domain"))

    implementation(project(":root:tabs:car"))
    implementation(project(":root:tabs:support"))

    implementation(project(":cardetails:ui"))

    implementation(project(":cbvs-apis"))
    implementation(project(":cfs-apis"))
    implementation(project(":consent-api"))
    implementation(project(":cosmocalendar"))
    implementation(project(":educational-component:data"))
    implementation(project(":educational-component:domain"))
    implementation(project(":extensions"))

    implementation(project(":feature-flags:data"))
    implementation(project(":feature-flags:domain"))
    implementation(project(":feature-flags:features"))

    implementation(project(":force-update:ui"))

    implementation(project(":headerslibrary"))
    implementation(project(":inappengagement"))
    implementation(project(":koin"))
    implementation(project(":logger"))
    implementation(project(":marketing-consent:ui"))
    implementation(project(":marketing-consent:domain"))
    implementation(project(":marketing-consent:data"))
    implementation(project(":migration:data"))
    implementation(project(":migration:domain"))
    implementation(project(":network:cnepmob"))
    implementation(project(":network:region:data"))
    implementation(project(":network:region:domain"))
    implementation(project(":network:region:ui"))
    implementation(project(":navigation"))

    implementation(project(":auth:data"))
    implementation(project(":auth:domain"))
    implementation(project(":auth:ui"))
    implementation(project(":auth:quicksignin:data"))
    implementation(project(":auth:quicksignin:domain"))
    implementation(project(":auth:quicksignin:ui"))

    implementation(project(":parch"))

    implementation(project(":performance:data"))
    implementation(project(":performance:domain"))

    implementation(project(":publiccharging-data"))
    implementation(project(":publiccharging-domain"))
    implementation(project(":publiccharging-ui"))
    implementation(project(":remotecharging:ui"))
    implementation(project(":remotecharging:data"))
    implementation(project(":remotecharging:domain"))


    implementation(project(":push:registration"))
    implementation(project(":push:pushhandling"))
    implementation(project(":push:iterable"))

    implementation(project(":recharge-apis"))
    implementation(project(":retrofit"))
    implementation(project(":result"))
    implementation(project(":searchview"))
    implementation(project(":servicebooking"))
    implementation(project(":servicesapi"))
    implementation(project(":settings:calendar:data"))
    implementation(project(":settings:calendar:domain"))
    implementation(project(":settings:credits:data"))
    implementation(project(":settings:credits:domain"))
    implementation(project(":settings:ui"))
    implementation(project(":settings:units:data"))
    implementation(project(":settings:units:domain"))
    implementation(project(":settings:legal:ui"))
    implementation(project(":settings:legal:domain"))
    implementation(project(":settings:legal:data"))
    implementation(project(":support-apis"))
    implementation(project(":support-domain"))
    implementation(project(":ui-components-legacy"))
    implementation(project(":ui-profile"))
    implementation(project(":ui-cbv-subscriptions"))
    implementation(project(":ui-components:volvo-ui"))
    implementation(project(":ui-components:volvo-app-ui"))
    implementation(project(":ui-order-tracking"))
    implementation(project(":ui-support"))
    implementation(project(":ui-device-manager"))
    implementation(project(":uiviews"))
    implementation(project(":vcc-common"))
    implementation(project(":vcc-server"))
    implementation(project(":viewpagerindicator"))
    implementation(project(":vocconfigurationapi"))
    implementation(project(":voccommon"))
    implementation(project(":voccomponentframework"))
    implementation(project(":vocexternalapi"))
    implementation(project(":vocinternal"))
    implementation(project(":voclogger"))
    implementation(project(":vocnetwork"))
    implementation(project(":voctranslations"))
    implementation(project(":voctheme"))
    implementation(project(":vocutils"))
    implementation(project(":vocwidgets"))
    implementation(project(":voclocation"))
    implementation(project(":in-app-feedback:data"))
    implementation(project(":in-app-feedback:domain"))
    implementation(project(":in-app-feedback:ui"))
    implementation(project(":icup-pairing:data"))
    implementation(project(":icup-pairing:domain"))
    implementation(project(":icup-pairing:ui"))
    implementation(project(":pairing-guide:ui"))
    implementation(project(":order-tracking:data"))
    implementation(project(":order-tracking:domain"))
    implementation(project(":order-tracking:ui"))
    implementation(project(":car:software:ui"))
    implementation(project(":car:software:domain"))
    implementation(project(":car:software:data"))
    implementation(project(":drivingjournal:ui"))
    implementation(project(":drivingjournal:data"))
    implementation(project(":drivingjournal:domain"))
    implementation(project(":subscriptions:domain"))
    implementation(project(":subscriptions:data"))
    implementation(project(":subscriptions:ui"))
    implementation(project(":payment:data"))
    implementation(project(":payment:domain"))
    implementation(project(":payment:ui"))
    implementation(project(":invoices:domain"))
    implementation(project(":helper:price"))

    implementation(project(":markets:data"))
    implementation(project(":markets:domain"))

    implementation(project(":codev:domain"))
    implementation(project(":codev:data"))
    implementation(project(":codev:ui"))
    implementation(project(":environment:data"))
    implementation(project(":environment:domain"))
    implementation(project(":environment:ui"))

    implementation(project(":user:data"))
    implementation(project(":user:domain"))
    implementation(project(":welcome:ui"))

    implementation(project(":widget"))

    implementation(project(":invoices:data"))
    implementation(project(":invoices:ui"))

    implementation(project(":exterior-air-quality:data"))
    implementation(project(":exterior-air-quality:domain"))
    implementation(project(":exterior-air-quality:ui"))

    implementation(project(":permissions:data"))
    implementation(project(":permissions:domain"))
    implementation(project(":permissions:ui"))

    implementation(project(":weather:data"))
    implementation(project(":weather:domain"))

    implementation(project(":support:data"))
    implementation(project(":support:domain"))
    implementation(project(":support:ui"))

    implementation(project(":sales-support:data"))
    implementation(project(":sales-support:domain"))
    implementation(project(":sales-support:ui"))

    // Koin
    implementation(koin.android)

    implementation(swagger.annotations)
    implementation(volley.volley)

    // Should be removed (Use Kotlin duration API)
    implementation(joda.convert)
    implementation(joda.time)

    // Google Play Services
    implementation(google.android.playservices.maps)
    implementation(google.android.playservices.location)
    implementation(google.android.playservices.tagmanager)
    implementation(google.maps.utils)
    implementation(google.maps.mapscompose)

    // Vocmo
    implementation(volvo.vocmo)

    implementation(platform(squareup.okhttp.bom))
    implementation(squareup.okhttp.loggingInterceptor)

    // Image loader
    implementation(glide.glide)
    kapt(glide.compiler)

    // Compose
    implementation(androidx.compose.compiler)
    implementation(androidx.compose.foundation.foundation)
    implementation(androidx.compose.material)
    implementation(androidx.compose.runtime)
    implementation(androidx.compose.ui.tooling)
    implementation(androidx.compose.ui.ui)
    implementation(androidx.constraintlayout.compose)
    implementation(androidx.activity.compose)
    debugImplementation(androidx.compose.ui.manifest)

    implementation(google.accompanist.navigation.material)

    // DeepLink
    implementation(branch.sdk)

    // Native Chat
    implementation(libphonenumber.libPhoneNumber)
    implementation(salesforce.chatui)
    implementation(rxjava.rxjava2.rxjava)
    implementation(rxjava.rxandroid2.rxandroid)

    implementation(jsoup.jsoup)

    // Leanplum
    implementation(leanplum.core)
    implementation(leanplum.location)

    // Apollo implementation - User Awareness - (ServiceBooking required)
    implementation(Apollo.runtime)
    implementation(Apollo.coroutinesSupport)

    // Room
    implementation(androidx.room.runtime)
    implementation(androidx.room.ktx)
    kapt(androidx.room.compiler)

    // Markwon
    implementation(noties.markwon)

    // Crypto shared prefs
    implementation(androidx.security.securitycrypto)

    // data store
    implementation(androidx.datastore.preferences)

    // china
    chinaImplementation(files("libs/pushservice-7.0.0.27.jar"))
    chinaImplementation(files("libs/oaid_sdk_1.0.25.aar"))

    // for unit tests on local dev machine
    testImplementation(kotlin("test-junit"))
    testImplementation(junit.junit)
    testImplementation(mockito.core)
    testImplementation(mockito.inline)
    testImplementation(mockito.kotlin)
    testImplementation(mockito.mockitokotlin2)
    testImplementation(androidx.lifecycle.test.runtimeTesting)
    testImplementation(robolectric.robolectric)
    testImplementation(robolectric.shadow.multidex)
    testImplementation(squareup.mockWebServicerDsb.mockwebserver)
    testImplementation(androidx.test.core)
    testImplementation(androidx.arch.core.testing)
    testImplementation(kotlinx.coroutines.test)
    testImplementation(koin.test)
    testImplementation(koin.junit)
    testImplementation(koin.junit5)
    testImplementation(junit.jupiter.api)
    testImplementation(junit.jupiter.params)
    testRuntimeOnly(junit.jupiter.engine)
    testRuntimeOnly(junit.vintage.engine)
    testImplementation(mockK.mockK)
    testImplementation(kotest.assertionsCore)
    testImplementation(turbine.turbine)
    testImplementation(project(":voclocation"))
    testImplementation(project(":vocutils"))
    testImplementation(project(":test-utils"))
    testImplementation(project(":analytics:test"))
    testImplementation(project(":feature-flags:test"))

    // Android runner and rules support
    androidTestImplementation(kotlin("test-junit"))
    androidTestImplementation(androidx.compose.ui.test)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.runner)
    androidTestImplementation(mockK.android)
    androidTestImplementation(mockito.android)
    androidTestImplementation(hamcrest.library)
    androidTestImplementation(mockito.mockitokotlin2)
    androidTestImplementation(dexopener.dexopener)
    androidTestImplementation(androidx.annotation)
    androidTestImplementation(androidx.test.espresso.intents)
    androidTestImplementation(androidx.test.uiautomator.uiautomator)
    androidTestImplementation(androidx.test.core)
    androidTestImplementation(agoda.kakao)

    androidTestImplementation(androidx.test.espresso.contrib) {
        exclude(group = "com.android.support", module = "support-annotations")
        exclude(group = "com.android.support", module = "support-v4")
        exclude(group = "com.android.support", module = "design")
        exclude(group = "com.android.support", module = "recyclerview-v7")
        exclude(module = "protobuf-lite")
    }

    androidTestImplementation(androidx.test.rules) {
        exclude(module = "support-annotations")
    }

    androidTestImplementation(androidx.test.espresso.core) {
        exclude(module = "rules")
        exclude(module = "javax.annotation-api")
        exclude(module = "support-annotations")
    }
}


tasks.register("copyJsonDirectoryForTest") {
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/gcm/debug/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/gcm/release/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/china/debug/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/china/release/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/gcmReleaseCandidate/debug/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/gcmReleaseCandidate/release/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/chinaReleaseCandidate/debug/res/")
    }
    copy {
        from("${projectDir}/src/main/assets/json")
        into("${buildDir}/intermediates/classes/test/chinaReleaseCandidate/release/res/")
    }
}


gradle.taskGraph.whenReady {
    tasks.whenTaskAdded {
        println("Task name: $name")
        getTasksByName("copyJsonDirectoryForTest", false).first().onlyIf {
            println("Copy: json")
            it.name.contains("UnitTest")
        }

    }
    println("whenReady")
}
