package com.ahmedmunna.nclip.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedmunna.nclip.util.Perms

@Composable
fun NclipRoot(tick: Int) {
    val ctx = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !Perms.postNotif(ctx)) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            val inf = rememberInfiniteTransition(label = "pulse")
            val a by inf.animateFloat(
                0.25f, 1f,
                infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "pa"
            )
            Box(Modifier.size(8.dp).graphicsLayer { alpha = a }.background(C.acc))
            Spacer(Modifier.width(10.dp))
            Text("NCLIP", color = C.hi, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 5.sp)
        }

        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val dir = if (targetState > initialState) 1 else -1
                    (fadeIn(tween(260)) + slideInHorizontally(tween(260)) { dir * it / 10 }) togetherWith
                        (fadeOut(tween(160)) + slideOutHorizontally(tween(200)) { -dir * it / 10 })
                },
                label = "tabs"
            ) { t ->
                when (t) {
                    0 -> NotifScreen(tick) { tab = 2 }
                    1 -> ClipScreen()
                    2 -> SetupScreen(tick)
                    else -> AboutScreen()
                }
            }
        }

        TabBar(listOf("NOTIFS", "CLIPS", "SETUP", "ABOUT"), tab) { tab = it }
    }
}
