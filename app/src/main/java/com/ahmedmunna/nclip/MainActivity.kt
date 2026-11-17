package com.ahmedmunna.nclip

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.ahmedmunna.nclip.service.ClipService
import com.ahmedmunna.nclip.ui.NclipRoot
import com.ahmedmunna.nclip.ui.NclipTheme
import com.ahmedmunna.nclip.util.ClipUtil
import com.ahmedmunna.nclip.util.Prefs

class MainActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent { NclipTheme { NclipRoot(tick) } }
        if (Prefs.clipEnabled(this)) ClipService.start(this)
    }

    override fun onResume() {
        super.onResume()
        tick++
    }

    // অ্যাপ খুললেই (ফোকাস পেলে) বর্তমান ক্লিপবোর্ড সেভ হয়
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) ClipUtil.capture(this)
    }
}
