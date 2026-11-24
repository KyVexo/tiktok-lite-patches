package app.tiktoklitepatches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val skipSearchHistory = bytecodePatch(
    name = "Do not save recent searches",
    description = "Adds an off-by-default switch to stop saving new entries to the local recent searches list.",
    default = false,
) {
    compatibleWith(tikTokLiteCompatibility)
    dependsOn(modSettingsSupport)
    execute {
        enableModSetting("skipSearchHistoryAvailable")

        val write = Fingerprint(
            definingClass = "LX/QPf;",
            name = "LCC",
            parameters = listOf("LX/MG8;", "LX/MFs;", "LX/MFy;"),
            returnType = "V",
            strings = listOf("key", "repo", "data"),
        ).method
        val array = write.instructions.withIndex().single { (_, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            reference?.definingClass == "LX/MG5;" && reference.name == "asArray"
        }.index
        check(write.instructions[array + 1].opcode == Opcode.MOVE_RESULT_OBJECT)
        // Keep the bridge's success response and allow manual deletions from the existing list.
        write.addInstructionsWithLabels(array + 2, """
            invoke-static {v4}, Lapp/tiktoklitepatches/extension/ModSettings;->skipSearchHistoryWrite(Ljava/lang/String;)Z
            move-result v5
            if-eqz v5, :original_write
            invoke-interface {v0}, LX/MG7;->toList()Ljava/util/List;
            move-result-object v5
            invoke-interface {v1, v4}, LX/MP4;->LBL(Ljava/lang/String;)Ljava/lang/Object;
            move-result-object v6
            invoke-static {v5, v6}, Lapp/tiktoklitepatches/extension/ModSettings;->isSearchHistoryRemoval(Ljava/util/List;Ljava/lang/Object;)Z
            move-result v5
            if-nez v5, :original_write
            new-instance v0, Ljava/util/LinkedHashMap;
            invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V
            invoke-static {p2, v3, v0}, LX/NKo;->LF(LX/MFs;Ljava/lang/String;Ljava/util/Map;)V
            return-void
            :original_write
            nop
        """.trimIndent())

        val writePageCache = Fingerprint(
            definingClass = "LX/HRR;",
            name = "LD",
            parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
            returnType = "LX/HRL;",
        ).method
        // Lynx also saves an independent snapshot for the search landing screen.
        writePageCache.addInstructionsWithLabels(0, filteredPageCache("p0", "p1"))

        val readPageCache = Fingerprint(
            definingClass = "LX/HRR;",
            name = "LCI",
            parameters = listOf("Ljava/lang/String;"),
            returnType = "LX/HRL;",
        ).method
        val close = readPageCache.instructions.withIndex().single { (_, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            reference?.definingClass == "Ljava/io/InputStreamReader;" && reference.name == "close"
        }.index
        readPageCache.addInstructionsWithLabels(close + 1, filteredPageCache("v4", "v6"))

        val publish = Fingerprint(
            definingClass = "LX/Nj3;",
            name = "LCC",
            parameters = listOf("LX/MG8;", "LX/MFs;", "LX/MFy;"),
            returnType = "V",
            strings = listOf("eventName", "timestamp", "params"),
        ).method
        val params = publish.instructions.withIndex().single { (_, instruction) ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            reference?.definingClass == "LX/MG4;" && reference.name == "LF"
        }.index
        check(publish.instructions[params + 1].opcode == Opcode.MOVE_RESULT_OBJECT)
        // Send the saved list back to the originating Lynx view after a blocked update.
        publish.addInstructionsWithLabels(params + 2, """
            invoke-static {v4}, Lapp/tiktoklitepatches/extension/ModSettings;->correctSearchHistoryEvent(Ljava/lang/String;)Z
            move-result v1
            if-eqz v1, :publish_event
            if-eqz v0, :publish_event
            const-string v1, "search_history"
            invoke-interface {v0, v1}, LX/MG8;->hasKey(Ljava/lang/String;)Z
            move-result v5
            if-eqz v5, :publish_event
            invoke-interface {v0, v1}, LX/MG8;->getArray(Ljava/lang/String;)LX/MG7;
            move-result-object v5
            if-eqz v5, :publish_event
            invoke-interface {v5}, LX/MG7;->toList()Ljava/util/List;
            move-result-object v5
            ${readStoredHistory("v1", "v7", "v9")}
            invoke-static {v5, v1}, Lapp/tiktoklitepatches/extension/ModSettings;->correctedSearchHistoryEvent(Ljava/util/List;Ljava/lang/Object;)Lorg/json/JSONObject;
            move-result-object v1
            if-eqz v1, :publish_event
            new-instance v0, LX/MpY;
            invoke-direct {v0, v1}, LX/MpY;-><init>(Lorg/json/JSONObject;)V
            :publish_event
            nop
        """.trimIndent())
    }
}

private fun filteredPageCache(page: String, data: String) = """
            invoke-static {$page}, Lapp/tiktoklitepatches/extension/ModSettings;->skipSearchHistoryPageCache(Ljava/lang/String;)Z
            move-result v1
            if-eqz v1, :original_cache
            ${readStoredHistory("v1", "v2", "v3")}
            invoke-static {$data, v1}, Lapp/tiktoklitepatches/extension/ModSettings;->replaceCachedSearchHistory(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;
            move-result-object $data
            :original_cache
            nop
""".trimIndent()

private fun readStoredHistory(result: String, store: String, repo: String) = """
    invoke-static {}, Lcom/ss/android/ugc/aweme/mini_account_impl/AccountManager;->LJJL()Lcom/ss/android/ugc/aweme/account_api/IAccountService;
    move-result-object $result
    invoke-interface {$result}, Lcom/ss/android/ugc/aweme/account_api/IAccountService;->getCurUserId()Ljava/lang/String;
    move-result-object $result
    invoke-static {$result}, Lapp/tiktoklitepatches/extension/ModSettings;->searchHistoryStorageKey(Ljava/lang/String;)Ljava/lang/String;
    move-result-object $result
    sget-object $store, LX/0JT;->LBL:Landroid/app/Application;
    const-string $repo, ""
    invoke-static {$store, $repo}, LX/P1l;->L(Landroid/content/Context;Ljava/lang/String;)LX/MP4;
    move-result-object $store
    invoke-interface {$store, $result}, LX/MP4;->LBL(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object $result
""".trimIndent()
