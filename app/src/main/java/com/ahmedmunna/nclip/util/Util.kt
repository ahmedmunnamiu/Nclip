package com.ahmedmunna.nclip.util

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ahmedmunna.nclip.data.Repo

object Prefs {
    private fun sp(c: Context) = c.getSharedPreferences("nclip", Context.MODE_PRIVATE)
    fun clipEnabled(c: Context) = sp(c).getBoolean("clip", true)
    fun setClip(c: Context, v: Boolean) = sp(c).edit().putBoolean("clip", v).apply()
}

object ClipUtil {
    /** ক্লিপবোর্ড পড়ে সেভ করে। সফল হলে true। */
    fun capture(ctx: Context): Boolean {
        return try {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            if (clip == null || clip.itemCount == 0) {
                false
            } else if (clip.description?.extras?.getBoolean("android.content.extra.IS_SENSITIVE") == true) {
                // পাসওয়ার্ড ম্যানেজার "sensitive" মার্ক করলে সেভ করবো না
                false
            } else {
                val t = clip.getItemAt(0).coerceToText(ctx)?.toString()
                if (t.isNullOrBlank()) false else { Repo.addClip(t); true }
            }
        } catch (e: Throwable) {
            false
        }
    }
}

object Perms {
    fun notifAccess(c: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(c).contains(c.packageName)

    fun postNotif(c: Context): Boolean = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(c, android.Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun batteryOk(c: Context): Boolean =
        (c.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(c.packageName)

    private fun go(c: Context, i: Intent) {
        try { c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
    }

    fun openNotifAccess(c: Context) = go(c, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

    fun requestBattery(c: Context) {
        try {
            c.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${c.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
            go(c, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    fun appInfo(c: Context) = go(
        c, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${c.packageName}"))
    )

    fun openUrl(c: Context, url: String) = go(c, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    fun dial(c: Context, number: String) = go(c, Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
}
