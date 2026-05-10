package com.example.diplom.ui

import android.content.Context
import com.example.diplom.R

/** Событие для snackbar импорта/экспорта — текст через [toImportTransferMessage]. */
sealed class ImportTransferNotification {
    data object ExportReady : ImportTransferNotification()
    data object TrainerImportSuccess : ImportTransferNotification()
    data object ShareImportSuccess : ImportTransferNotification()
    data class Failure(val detail: String?) : ImportTransferNotification()
}

fun ImportTransferNotification.toImportTransferMessage(context: Context): String = when (this) {
    is ImportTransferNotification.ExportReady ->
        context.getString(R.string.export_trainer_json_ready)
    is ImportTransferNotification.TrainerImportSuccess ->
        context.getString(R.string.import_trainer_success) + " " +
            context.getString(R.string.import_trainer_replaces_plan)
    is ImportTransferNotification.ShareImportSuccess ->
        context.getString(R.string.import_from_share_success) + " " +
            context.getString(R.string.import_trainer_replaces_plan)
    is ImportTransferNotification.Failure ->
        detail?.takeIf { it.isNotBlank() } ?: context.getString(R.string.import_unknown_error)
}
