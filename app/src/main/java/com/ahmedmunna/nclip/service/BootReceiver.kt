package com.ahmedmunna.nclip.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahmedmunna.nclip.util.Prefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (Prefs.clipEnabled(c)) ClipService.start(c)
    }
}
