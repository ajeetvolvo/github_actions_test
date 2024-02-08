minSdk = (libs.versions.minSdk.get() as String).toInt()
        targetSdk = (libs.versions.targetSdk.get() as String).toInt()
        multiDexEnabled = true
        testApplicationId = "se.volvo.vcc.test"
        testInstrumentationRunner = "se.abcd.vcc.testUtils.CustomTestRunner"
        buildConfigField("String", SERVER_BUILD_NUMBER, "\"\"")
        buildConfigField("String", APP_CENTER_SECRET, "\"\"")
        buildConfigField("String", GIT_BRANCH, "\"${gitBranch()}\"")
        versionName = "5.38.0"
        versionCode = VersionCodeManager.currentVersionCode(project)
