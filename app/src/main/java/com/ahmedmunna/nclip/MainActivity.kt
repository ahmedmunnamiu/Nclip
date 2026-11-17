package com.ahmedmunna.nclip

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var store: Store
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var clipboard: ClipboardManager
    private var current = "notification"
    private val bg = Color.rgb(0, 0, 0)
    private val panel = Color.rgb(17, 17, 17)
    private val white = Color.WHITE
    private val muted = Color.rgb(150, 150, 150)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = Store(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener {
            if (hasWindowFocus()) saveClipboard()
        }
        buildShell()
        showItems()
    }

    override fun onResume() {
        super.onResume()
        if (::content.isInitialized) showItems()
    }

    private fun buildShell() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(18, 24, 18, 12) }
        val header = TextView(this).apply {
            text = "Nclip"; textSize = 30f; setTextColor(white); typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(header)
        root.addView(label("NOTIFICATION & CLIPBOARD MANAGER"))
        val tabs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 20, 0, 14) }
        tabs.addView(actionButton("NOTIFICATIONS") { current = "notification"; showItems() }, LinearLayout.LayoutParams(0, 52, 1f))
        tabs.addView(actionButton("CLIPBOARD") { current = "clipboard"; showItems() }, LinearLayout.LayoutParams(0, 52, 1f).apply { leftMargin = 8 })
        root.addView(tabs)
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(actionButton("ACCESS SETTINGS") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, LinearLayout.LayoutParams(0, 46, 1f))
        actions.addView(actionButton("CLEAR ALL") { store.clear(current); showItems() }, LinearLayout.LayoutParams(0, 46, 1f).apply { leftMargin = 8 })
        root.addView(actions)
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 14, 0, 10) }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(label("LOCAL STORAGE ONLY  •  NCLIP 1.0"))
        root.addView(actionButton("ABOUT NCLIP") { showAbout() }, LinearLayout.LayoutParams(-1, 44).apply { topMargin = 8 })
        setContentView(root)
        window.statusBarColor = bg
        window.navigationBarColor = bg
    }

    private fun showItems() {
        if (!::content.isInitialized) return
        content.removeAllViews()
        val rows = store.list(current)
        content.addView(label("${if (current == "notification") "SAVED NOTIFICATIONS" else "CLIPBOARD HISTORY"}  •  ${rows.size}"))
        if (rows.isEmpty()) {
            val empty = TextView(this).apply {
                text = if (current == "notification") "No notifications saved yet.\nEnable Nclip in Notification Access." else "No clipboard entries yet.\nCopy text while Nclip is open."
                setTextColor(muted); textSize = 15f; setPadding(14, 28, 14, 28)
            }
            content.addView(empty)
        }
        rows.forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setBackgroundColor(panel); setPadding(14, 12, 14, 12)
            }
            card.addView(TextView(this).apply {
                text = if (item.kind == "notification") item.title.ifBlank { item.app } else item.body
                setTextColor(white); textSize = 16f; typeface = Typeface.DEFAULT_BOLD
            })
            if (item.kind == "notification") card.addView(TextView(this).apply {
                text = item.body; setTextColor(Color.LTGRAY); textSize = 14f; setPadding(0, 6, 0, 4)
            })
            card.addView(TextView(this).apply {
                text = "${item.app}  •  ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(item.time))}"
                setTextColor(muted); textSize = 11f; setPadding(0, 8, 0, 8)
            })
            val rowActions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            if (item.kind == "clipboard") rowActions.addView(actionButton("COPY AGAIN") {
                clipboard.setPrimaryClip(ClipData.newPlainText("Nclip", item.body))
            }, LinearLayout.LayoutParams(0, 40, 1f))
            rowActions.addView(actionButton("DELETE") { store.delete(item.id); showItems() },
                LinearLayout.LayoutParams(0, 40, 1f).apply { leftMargin = 8 })
            card.addView(rowActions)
            val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 10
            content.addView(card, lp)
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("Nclip")
            .setMessage("Developer: Ahmed Munna\\nPhone: 01625104847\\nFacebook: facebook.com/ahmedmunna0\\nGitHub: github.com/ahmedmunnamiu\\n\\nNclip stores notification and clipboard history locally on this device.")
            .setPositiveButton("CLOSE", null)
            .show()
    }

    private fun saveClipboard() {
        try {
            if (clipboard.hasPrimaryClip()) {
                val clip = clipboard.primaryClip ?: return
                val text = clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
                if (text.isNotBlank() && text.length <= 100_000) {
                    val last = store.list("clipboard").firstOrNull()
                    if (last?.body != text) {
                        store.add("clipboard", "Copied text", text)
                        if (current == "clipboard") showItems()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun label(value: String) = TextView(this).apply {
        text = value; textSize = 10f; letterSpacing = 0.12f; setTextColor(muted); setPadding(0, 5, 0, 5)
    }

    private fun actionButton(value: String, action: () -> Unit) = Button(this).apply {
        text = value; textSize = 11f; isAllCaps = false; setTextColor(white)
        setBackgroundColor(Color.rgb(35, 35, 35)); setOnClickListener { action() }
        minHeight = 0; minimumHeight = 0; stateListAnimator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        try { clipboard.removePrimaryClipChangedListener {} } catch (_: Exception) {}
    }
}
