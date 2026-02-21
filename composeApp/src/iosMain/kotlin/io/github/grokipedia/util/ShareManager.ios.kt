package io.github.grokipedia.util

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class ShareManager {
    actual fun share(url: String, title: String) {
        val items = listOf("$title\n$url")
        val activityVC = UIActivityViewController(
            activityItems = items,
            applicationActivities = null
        )
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
        println("[SHARE] Sharing: $url")
    }
}
