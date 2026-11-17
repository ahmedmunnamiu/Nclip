package com.ahmedmunna.nclip.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedmunna.nclip.data.ClipItem
import com.ahmedmunna.nclip.data.NotifItem
import com.ahmedmunna.nclip.data.Repo
import com.ahmedmunna.nclip.service.ClipService
import com.ahmedmunna.nclip.util.ClipUtil
import com.ahmedmunna.nclip.util.Perms
import com.ahmedmunna.nclip.util.Prefs
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fmt = SimpleDateFormat("dd MMM · hh:mm a", Locale.getDefault())

// =============================== NOTIFICATIONS ===============================
@Composable
fun NotifScreen(tick: Int, onSetup: () -> Unit) {
    val ctx = LocalContext.current
    val all by Repo.notifs.collectAsState()
    var q by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val hasAccess = remember(tick) { Perms.notifAccess(ctx) }
    val list = remember(all, q) {
        if (q.isBlank()) all else all.filter {
            it.app.contains(q, true) || it.title.contains(q, true) || it.text.contains(q, true)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenTitle("Notifications", "${all.size} saved")
        AnimatedVisibility(
            visible = !hasAccess,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Column {
                SqCard {
                    Text("Notification access is OFF", color = C.danger, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Text("Nclip can't save notifications until you grant access.", color = C.mid, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    SqButton("Open setup", onSetup, compact = true, filled = true)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SqSearch(q, { q = it }, "Search app, title or text", Modifier.weight(1f))
            SqButton("Clear", { confirm = true }, enabled = all.isNotEmpty())
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (list.isEmpty()) item { EmptyState(if (all.isEmpty()) "Nothing saved yet" else "No match") }
            items(list, key = { it.id }) { n -> NotifCard(n, Modifier.animateItem()) }
        }
    }
    if (confirm) ConfirmDialog(
        "Clear all?", "All saved notifications will be deleted.", "Delete",
        { Repo.clearNotifs() }, { confirm = false }
    )
}

@Composable
private fun NotifCard(n: NotifItem, modifier: Modifier) {
    var open by remember { mutableStateOf(false) }
    SqCard(modifier.animateContentSize(tween(250)), onClick = { open = !open }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                n.app.uppercase(), color = C.acc, fontSize = 10.sp, letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(fmt.format(Date(n.time)), color = C.lo, fontSize = 10.sp)
        }
        if (n.title.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                n.title, color = C.hi, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                maxLines = if (open) 10 else 1, overflow = TextOverflow.Ellipsis
            )
        }
        if (n.text.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                n.text, color = C.mid, fontSize = 13.sp,
                maxLines = if (open) 60 else 2, overflow = TextOverflow.Ellipsis
            )
        }
        if (open) {
            Spacer(Modifier.height(12.dp))
            SqButton("Delete", { Repo.deleteNotif(n.id) }, compact = true, danger = true)
        }
    }
}

// =============================== CLIPBOARD ===============================
@Composable
fun ClipScreen() {
    val ctx = LocalContext.current
    val all by Repo.clips.collectAsState()
    var q by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val list = remember(all, q) { if (q.isBlank()) all else all.filter { it.text.contains(q, true) } }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenTitle("Clipboard", "${all.size} saved")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SqButton("Save current", { ClipUtil.capture(ctx) }, filled = true, modifier = Modifier.weight(1f))
            SqButton("Clear", { confirm = true }, enabled = all.any { !it.pinned }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        SqSearch(q, { q = it }, "Search clips")
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (list.isEmpty()) item { EmptyState(if (all.isEmpty()) "Nothing copied yet" else "No match") }
            items(list, key = { it.id }) { c -> ClipCard(c, Modifier.animateItem()) }
        }
    }
    if (confirm) ConfirmDialog(
        "Clear clips?", "All unpinned clips will be deleted. Pinned ones stay.", "Delete",
        { Repo.clearClips() }, { confirm = false }
    )
}

@Composable
private fun ClipCard(c: ClipItem, modifier: Modifier) {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(1300); copied = false } }

    SqCard(modifier.animateContentSize(tween(250)), onClick = { open = !open }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (c.pinned) "PINNED" else "CLIP",
                color = if (c.pinned) C.acc else C.lo, fontSize = 10.sp,
                letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
            )
            Text("${c.text.length} chars · ${fmt.format(Date(c.time))}", color = C.lo, fontSize = 10.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            c.text, color = C.hi, fontSize = 13.sp,
            maxLines = if (open) 40 else 3, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SqButton(if (copied) "Copied" else "Copy", {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("Nclip", c.text))
                copied = true
            }, compact = true, filled = copied)
            SqButton(if (c.pinned) "Unpin" else "Pin", { Repo.togglePin(c.id) }, compact = true)
            SqButton("Delete", { Repo.deleteClip(c.id) }, compact = true, danger = true)
        }
    }
}

// =============================== SETUP ===============================
@Composable
fun SetupScreen(tick: Int) {
    val ctx = LocalContext.current
    var clipOn by remember { mutableStateOf(Prefs.clipEnabled(ctx)) }
    val access = remember(tick) { Perms.notifAccess(ctx) }
    val post = remember(tick) { Perms.postNotif(ctx) }
    val battery = remember(tick) { Perms.batteryOk(ctx) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScreenTitle("Setup", "Permissions & background")

        SetupRow(
            "Notification access", "Required to read and save notifications from all apps.",
            access, "Open settings", { Perms.openNotifAccess(ctx) }, Modifier.appear(0)
        )
        if (Build.VERSION.SDK_INT >= 33) {
            SetupRow(
                "Post notifications", "Lets Nclip show its small service notification.",
                post, "Allow", { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }, Modifier.appear(60)
            )
        }
        SetupRow(
            "Battery: unrestricted", "Stops the system from killing Nclip in the background.",
            battery, "Allow", { Perms.requestBattery(ctx) }, Modifier.appear(120)
        )

        SqCard(Modifier.appear(180)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("CLIPBOARD SERVICE", color = C.hi, fontSize = 12.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Background service that listens for copied text.", color = C.mid, fontSize = 12.sp)
                }
                Spacer(Modifier.width(12.dp))
                SqSwitch(clipOn) {
                    clipOn = it
                    Prefs.setClip(ctx, it)
                    if (it) ClipService.start(ctx) else ClipService.stop(ctx)
                }
            }
        }

        SqCard(Modifier.appear(240)) {
            Text("GOOD TO KNOW", color = C.acc, fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(
                "• Android 10+ blocks background apps from reading the clipboard. Nclip saves copies when the app is open, " +
                    "and the \"Save clipboard\" button in its status notification saves whatever you just copied in one tap.\n\n" +
                    "• If notification access is greyed out (sideloaded app, Android 13+): App info → ⋮ menu → Allow restricted settings.\n\n" +
                    "• On Xiaomi/Oppo/Vivo/Samsung etc. also enable Autostart and set battery to No restrictions.",
                color = C.mid, fontSize = 12.sp, lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            SqButton("App info", { Perms.appInfo(ctx) }, compact = true)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SetupRow(
    title: String, desc: String, ok: Boolean, action: String,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val dot by animateColorAsState(if (ok) C.acc else C.danger, tween(300), label = "dot")
    SqCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(dot))
            Spacer(Modifier.width(10.dp))
            Text(
                title.uppercase(), color = C.hi, fontSize = 12.sp, letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)
            )
            Text(if (ok) "ON" else "OFF", color = dot, fontSize = 11.sp, letterSpacing = 1.4.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(desc, color = C.mid, fontSize = 12.sp)
        if (!ok) {
            Spacer(Modifier.height(12.dp))
            SqButton(action, onClick, compact = true, filled = true)
        }
    }
}

// =============================== ABOUT ===============================
@Composable
fun AboutScreen() {
    val ctx = LocalContext.current
    val ver = remember {
        try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0" } catch (e: Exception) { "1.0" }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScreenTitle("About", "Version $ver")

        Column(Modifier.fillMaxWidth().appear(0).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(84.dp).border(1.dp, C.acc), contentAlignment = Alignment.Center) {
                Text("N", color = C.acc, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text("NCLIP", color = C.hi, fontSize = 22.sp, letterSpacing = 6.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Every notification. Everything you copy.\nSaved locally, on your device only.",
                color = C.lo, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp
            )
        }

        SqCard(Modifier.appear(120)) {
            Text("DEVELOPER", color = C.acc, fontSize = 10.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            Text("Ahmed Munna", color = C.hi, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Line()
            InfoRow("Phone", "01625104847", "Call") { Perms.dial(ctx, "01625104847") }
            Line()
            InfoRow("Facebook", "facebook.com/ahmedmunna0", "Open") { Perms.openUrl(ctx, "https://facebook.com/ahmedmunna0") }
            Line()
            InfoRow("Github", "github.com/ahmedmunnamiu", "Open") { Perms.openUrl(ctx, "https://github.com/ahmedmunnamiu") }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Line() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(C.line))
}

@Composable
private fun InfoRow(label: String, value: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label.uppercase(), color = C.lo, fontSize = 10.sp, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(3.dp))
            Text(value, color = C.hi, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(10.dp))
        SqButton(action, onClick, compact = true)
    }
}
