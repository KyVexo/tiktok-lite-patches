package app.tiktoklitepatches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.Opcode
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

private object SparkResume : Fingerprint(
    definingClass = "Lcom/bytedance/hybrid/spark/page/SparkActivity;",
    name = "onResume",
    returnType = "V",
    parameters = emptyList(),
)

@Suppress("unused")
val profileSettingsEntry = bytecodePatch(
    name = "Open mod settings from profile",
    description = "Adds a separate entry to open mod settings from the native settings page.",
    default = false,
) {
    compatibleWith(Compatibility(
        name = "TikTok Lite",
        packageName = "com.zhiliaoapp.musically.go",
        targets = listOf(AppTarget(version = "47.8.15", minSdk = 21)),
    ))
    execute {
        val method = SparkResume.method
        val returns = method.instructions.indices.filter { method.instructions[it].opcode == Opcode.RETURN_VOID }
        check(returns.isNotEmpty())
        for (index in returns.asReversed()) {
            method.addInstructions(index,
                "invoke-static/range {p0 .. p0}, Lapp/tiktoklitepatches/extension/SettingsPageEntry;->attach(Landroid/app/Activity;)V"
            )
        }
    }
}