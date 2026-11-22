package app.tiktoklitepatches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.resourceXml
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val feedList = "Lcom/ss/android/ugc/aweme/minilite/feed/data/bean/FeedItemList;"

private object FeedAdapter : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/gsonopt/OptJsonAdapterFor\$com\$ss\$android\$ugc\$aweme\$minilite\$feed\$data\$bean\$FeedItemList;",
    returnType = "Z",
    strings = listOf("aweme_list"),
)

private const val fullFeedList = "Lcom/ss/android/ugc/aweme/main/homepage/fragment/data/model/FeedItemList;"

private object FullFeedAdapter : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/gsonopt/OptJsonAdapterFor\$com\$ss\$android\$ugc\$aweme\$main\$homepage\$fragment\$data\$model\$FeedItemList;",
    returnType = "Z",
    strings = listOf("aweme_list", "appendCache"),
)

private object MainWindowFocus : Fingerprint(
    definingClass = "Lcom/zhiliaoapp/musically/go/mini/MainActivity;",
    name = "onWindowFocusChanged",
    returnType = "V",
    parameters = listOf("Z"),
)

private object NativeShortcutRefresh : Fingerprint(
    definingClass = "LX/0iL;",
    name = "LF",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/util/List;"),
)

private val settingsManifest = resourcePatch {
    execute {
        resourceXml("AndroidManifest.xml").use { xml ->
            xml.root.children("application").single().appendChild("activity") {
                setAttribute("android:name", "app.tiktoklitepatches.extension.ModSettingsActivity")
                setAttribute("android:exported", "false")
                setAttribute("android:theme", "@android:style/Theme.DeviceDefault.NoActionBar")
            }
        }
    }
}

@Suppress("unused")
val feedAdFilter = bytecodePatch(
    name = "Hide feed ads",
    description = "Filters sponsored entries flagged by the MiniLite feed model.",
    default = false,
) {
    compatibleWith(Compatibility(
        name = "TikTok Lite",
        packageName = "com.zhiliaoapp.musically.go",
        targets = listOf(AppTarget(version = "47.8.15", minSdk = 21)),
    ))
    dependsOn(settingsManifest)
    extendWith("extensions/extension.mpe")
    execute {
        MainWindowFocus.method.addInstructions(0, """
            invoke-static/range {p0 .. p1}, Lapp/tiktoklitepatches/extension/ModSettings;->onWindowFocus(Landroid/app/Activity;Z)V
        """)
        val shortcuts = NativeShortcutRefresh.method
        val nativeRegistration = shortcuts.instructions.withIndex().single { (_, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            reference?.definingClass == "Landroid/content/pm/ShortcutManager;" &&
                reference.name == "setDynamicShortcuts"
        }.index
        check(shortcuts.instructions[nativeRegistration + 1].opcode == Opcode.MOVE_RESULT)
        shortcuts.addInstructions(nativeRegistration + 2, """
            invoke-static/range {p0 .. p0}, Lapp/tiktoklitepatches/extension/ModSettings;->registerShortcut(Landroid/content/Context;)V
        """)
        for ((target, listType) in listOf(FeedAdapter.method to feedList, FullFeedAdapter.method to fullFeedList)) {
            val (index, instruction) = target.instructions.withIndex().single { (_, candidate) ->
                val field = (candidate as? ReferenceInstruction)?.reference as? FieldReference
                candidate.opcode == Opcode.IPUT_OBJECT && field?.definingClass == listType &&
                    field.name == "items" && field.type == "Ljava/util/List;"
            }
            val register = (instruction as TwoRegisterInstruction).registerA
            target.addInstructions(index, """
                invoke-static/range {v${register} .. v${register}}, Lapp/tiktoklitepatches/extension/FeedFilter;->removeAds(Ljava/util/List;)Ljava/util/List;
                move-result-object v${register}
            """)
        }
    }
}
