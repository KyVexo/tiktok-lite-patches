package app.tiktoklitepatches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal val tikTokLite4782Target = AppTarget(
    version = "47.8.2",
    versionCode = 470802,
    minSdk = 21,
    isExperimental = true,
    description = "APKMirror nodpi APK. LIVE playback module is unavailable in this build.",
)

internal val tikTokLiteCompatibility = Compatibility(
    name = "TikTok Lite",
    packageName = "com.zhiliaoapp.musically.go",
    targets = listOf(
        AppTarget(version = "47.8.15", minSdk = 21),
        tikTokLite4782Target,
    ),
)
