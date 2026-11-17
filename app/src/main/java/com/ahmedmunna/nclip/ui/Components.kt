package com.ahmedmunna.nclip.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** চৌকো বাটন, প্রেস করলে স্মুথ স্কেল + রং অ্যানিমেশন */
@Composable
fun SqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
    compact: Boolean = false,
    danger: Boolean = false
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(110), label = "scale")
    val accent = if (danger) C.danger else C.hi
    val bg by animateColorAsState(
        when {
            filled && pressed -> accent.copy(alpha = 0.8f)
            filled -> accent
            pressed -> C.press
            else -> Color.Black
        }, tween(150), label = "bg"
    )
    val fg by animateColorAsState(if (filled) Color.Black else accent, tween(150), label = "fg")
    val dim = if (enabled) 1f else 0.35f
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; alpha = dim }
            .background(bg)
            .border(1.dp, if (filled) accent else C.line2)
            .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = if (compact) 12.dp else 18.dp, vertical = if (compact) 8.dp else 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text.uppercase(), color = fg, maxLines = 1,
            fontSize = if (compact) 10.sp else 12.sp,
            letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SqCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var m = modifier.fillMaxWidth().background(C.card).border(1.dp, C.line)
    if (onClick != null) {
        m = m.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    }
    Column(m.padding(14.dp), content = content)
}

@Composable
fun SqSearch(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val border by animateColorAsState(if (focused) C.acc else C.line, tween(200), label = "sb")
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = C.hi, fontSize = 14.sp),
        cursorBrush = SolidColor(C.acc),
        modifier = modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().background(C.card).border(1.dp, border)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (value.isEmpty()) Text(hint, color = C.lo, fontSize = 14.sp)
                inner()
            }
        }
    )
}

@Composable
fun SqSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val x by animateDpAsState(if (checked) 25.dp else 3.dp, tween(220), label = "x")
    val bg by animateColorAsState(if (checked) C.acc else Color.Black, tween(220), label = "sbg")
    val thumb by animateColorAsState(if (checked) Color.Black else C.lo, tween(220), label = "st")
    Box(
        Modifier.size(48.dp, 26.dp).background(bg)
            .border(1.dp, if (checked) C.acc else C.line2)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onChange(!checked) }
    ) {
        Box(Modifier.offset(x, 3.dp).size(20.dp).background(thumb))
    }
}

@Composable
fun TabBar(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color.Black).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(C.line))
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { i, t ->
                val active = i == selected
                val col by animateColorAsState(if (active) C.hi else C.lo, tween(220), label = "tc")
                val bar by animateFloatAsState(if (active) 1f else 0f, tween(280), label = "tb")
                Column(
                    Modifier.weight(1f).clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.fillMaxWidth(bar).height(2.dp).background(C.acc))
                    Spacer(Modifier.height(14.dp))
                    Text(t, color = col, fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
fun ScreenTitle(title: String, sub: String) {
    Column(Modifier.padding(top = 4.dp, bottom = 14.dp)) {
        Text(title.uppercase(), color = C.hi, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(4.dp))
        Text(sub, color = C.lo, fontSize = 12.sp)
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
        Text(text, color = C.lo, fontSize = 13.sp)
    }
}

@Composable
fun ConfirmDialog(title: String, body: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Sq,
        containerColor = C.card,
        title = { Text(title.uppercase(), color = C.hi, fontSize = 15.sp, letterSpacing = 1.5.sp) },
        text = { Text(body, color = C.mid, fontSize = 13.sp) },
        confirmButton = { SqButton(confirm, { onConfirm(); onDismiss() }, filled = true, danger = true, compact = true) },
        dismissButton = { SqButton("Cancel", onDismiss, compact = true) }
    )
}

/** ফেড + নিচ থেকে স্লাইড-ইন (delay দিয়ে stagger করা যায়) */
@Composable
fun Modifier.appear(delayMs: Int = 0): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(450, delayMs, FastOutSlowInEasing)) }
    return this.graphicsLayer { alpha = a.value; translationY = (1f - a.value) * 28f }
}
