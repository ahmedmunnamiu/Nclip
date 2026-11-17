package com.ahmedmunna.nclip.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ahmedmunna.nclip.MainActivity
import com.ahmedmunna.nclip.R
import com.ahmedmunna.nclip.util.ClipUtil

/** ব্যাকগ্রাউন্ডে ক্লিপবোর্ড শোনার foreground service (কোনো polling/wakelock নেই, তাই ব্যাটারি খরচ ন্যূনতম)। */
class ClipService : Service() {

    private lateinit var cm: ClipboardManager
    private val listener = ClipboardManager.OnPrimaryClipChangedListener { ClipUtil.capture(this) }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH, "Nclip service", NotificationManager.IMPORTANCE_MIN).apply {
                setShowBadge(false)
            }
        )
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(ID, n)
        }
        cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener(listener)
    }

    private fun buildNotification(): Notification {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), flags)
        val save = PendingIntent.getActivity(this, 1, Intent(this, ClipCaptureActivity::class.java), flags)
        return NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("Nclip is active")
            .setContentText("Saving clipboard & notifications")
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(open)
            .addAction(0, "Save clipboard", save)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try { cm.removePrimaryClipChangedListener(listener) } catch (_: Throwable) {}
        super.onDestroy()
    }

    companion object {
        private const val CH = "nclip_service"
        private const val ID = 1

        fun start(c: Context) {
            try { ContextCompat.startForegroundService(c, Intent(c, ClipService::class.java)) } catch (_: Throwable) {}
        }

        fun stop(c: Context) {
            try { c.stopService(Intent(c, ClipService::class.java)) } catch (_: Throwable) {}
        }
    }
}
