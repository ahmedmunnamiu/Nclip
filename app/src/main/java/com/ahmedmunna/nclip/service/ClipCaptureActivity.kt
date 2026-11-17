package com.ahmedmunna.nclip.service

import android.app.Activity
import android.widget.Toast
import com.ahmedmunna.nclip.util.ClipUtil

/**
 * Android 10+ এ ব্যাকগ্রাউন্ড অ্যাপ ক্লিপবোর্ড পড়তে পারে না।
 * এই স্বচ্ছ Activity ফোকাস পেয়ে ক্লিপবোর্ড পড়ে সেভ করে আবার বন্ধ হয়ে যায়।
 * নোটিফিকেশনের "Save clipboard" বাটন এটি চালু করে।
 */
class ClipCaptureActivity : Activity() {
    private var done = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !done) {
            done = true
            val ok = ClipUtil.capture(this)
            Toast.makeText(this, if (ok) "Clipboard saved" else "Clipboard empty", Toast.LENGTH_SHORT).show()
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
