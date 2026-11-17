package com.ahmedmunna.nclip.service

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ahmedmunna.nclip.data.Repo

class NotifListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (sbn.packageName == packageName) return
            val n = sbn.notification ?: return
            if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

            val ex = n.extras
            val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
            if (title.isBlank() && text.isBlank()) return

            val app = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
            } catch (e: Exception) {
                sbn.packageName.substringAfterLast('.')
            }
            Repo.addNotif(sbn.packageName, app, title, text, sbn.postTime)
        } catch (_: Throwable) {
        }
    }

    // সিস্টেম সার্ভিস বিচ্ছিন্ন করলে নিজে থেকে আবার যুক্ত হওয়ার চেষ্টা
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try { requestRebind(ComponentName(this, NotifListener::class.java)) } catch (_: Throwable) {}
    }
}
