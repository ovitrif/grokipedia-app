package io.github.grokipedia.util

import android.content.Context
import android.content.Intent

private lateinit var appContext: Context

fun initShareManager(context: Context) {
    appContext = context.applicationContext
}

actual class ShareManager {
    actual fun share(url: String, title: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$title\n$url")
                putExtra(Intent.EXTRA_SUBJECT, title)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share article")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(shareIntent)
            println("[SHARE] Sharing: $url")
        } catch (e: Exception) {
            println("[SHARE] Error: ${e.message}")
        }
    }
}
