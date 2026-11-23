package app.tiktoklitepatches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

private object LoopingDispatcher4782 : Fingerprint(
    definingClass = "Lcom/ss/ttvideoengine/TTVideoEngine;",
    name = "LILLZLZ",
    parameters = listOf("Z"),
    returnType = "V",
    strings = listOf("set looping value:"),
)

private object MediaPlayerLooping4782 : Fingerprint(
    definingClass = "Lcom/ss/ttvideoengine/TTVideoEngineImpl;",
    name = "LILJ",
    parameters = listOf("Z"),
    returnType = "V",
    strings = listOf("setLooping:"),
)

@Suppress("unused")
val stopVideoLooping = bytecodePatch(
    name = "Stop video looping",
    description = "Stops videos at the end instead of automatically repeating them.",
    default = false,
) {
    // Adapted to KyVexo's target after reviewing Kveld's Stop Video Looping patch.
    compatibleWith(Compatibility(
        name = "TikTok Lite",
        packageName = "com.zhiliaoapp.musically.go",
        targets = listOf(tikTokLite4782Target),
    ))
    execute {
        for (fingerprint in listOf(LoopingDispatcher4782, MediaPlayerLooping4782)) {
            fingerprint.method.addInstructions(0, "const/4 p1, 0x0")
        }
    }
}
