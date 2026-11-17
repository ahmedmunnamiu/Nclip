package com.ahmedmunna.nclip.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class NotifItem(
    val id: Long, val pkg: String, val app: String,
    val title: String, val text: String, val time: Long
)

data class ClipItem(val id: Long, val text: String, val time: Long, val pinned: Boolean)

private class Db(ctx: Context) : SQLiteOpenHelper(ctx, "nclip.db", null, 1) {
    override fun onCreate(d: SQLiteDatabase) {
        d.execSQL("CREATE TABLE notifs(id INTEGER PRIMARY KEY AUTOINCREMENT, pkg TEXT, app TEXT, title TEXT, body TEXT, time INTEGER)")
        d.execSQL("CREATE TABLE clips(id INTEGER PRIMARY KEY AUTOINCREMENT, body TEXT, time INTEGER, pinned INTEGER DEFAULT 0)")
    }
    override fun onUpgrade(d: SQLiteDatabase, o: Int, n: Int) {}
}

/** সব ডাটাবেস কাজ একটি single-thread executor-এ হয়, তাই thread-safe। */
object Repo {
    private var db: Db? = null
    private val io: ExecutorService = Executors.newSingleThreadExecutor()

    val notifs = MutableStateFlow<List<NotifItem>>(emptyList())
    val clips = MutableStateFlow<List<ClipItem>>(emptyList())

    fun init(ctx: Context) {
        if (db == null) {
            db = Db(ctx.applicationContext)
            io.execute { refreshNotifs(); refreshClips() }
        }
    }

    private fun d(): SQLiteDatabase = db!!.writableDatabase

    private fun refreshNotifs() {
        val out = ArrayList<NotifItem>()
        d().rawQuery(
            "SELECT id,pkg,app,title,body,time FROM notifs ORDER BY id DESC LIMIT 3000", null
        ).use { c ->
            while (c.moveToNext()) out.add(
                NotifItem(
                    c.getLong(0), c.getString(1) ?: "", c.getString(2) ?: "",
                    c.getString(3) ?: "", c.getString(4) ?: "", c.getLong(5)
                )
            )
        }
        notifs.value = out
    }

    private fun refreshClips() {
        val out = ArrayList<ClipItem>()
        d().rawQuery(
            "SELECT id,body,time,pinned FROM clips ORDER BY pinned DESC, time DESC LIMIT 2000", null
        ).use { c ->
            while (c.moveToNext()) out.add(
                ClipItem(c.getLong(0), c.getString(1) ?: "", c.getLong(2), c.getInt(3) == 1)
            )
        }
        clips.value = out
    }

    // ---------- Notifications ----------
    fun addNotif(pkg: String, app: String, title: String, body: String, time: Long) {
        io.execute {
            val db = d()
            var dup = false
            db.rawQuery(
                "SELECT title, body FROM notifs WHERE pkg=? ORDER BY id DESC LIMIT 1", arrayOf(pkg)
            ).use { c -> if (c.moveToFirst() && c.getString(0) == title && c.getString(1) == body) dup = true }
            if (!dup) {
                val cv = ContentValues().apply {
                    put("pkg", pkg); put("app", app); put("title", title)
                    put("body", body); put("time", time)
                }
                db.insert("notifs", null, cv)
                db.execSQL("DELETE FROM notifs WHERE id NOT IN (SELECT id FROM notifs ORDER BY id DESC LIMIT 5000)")
                refreshNotifs()
            }
        }
    }

    fun deleteNotif(id: Long) {
        io.execute { d().delete("notifs", "id=?", arrayOf(id.toString())); refreshNotifs() }
    }

    fun clearNotifs() {
        io.execute { d().delete("notifs", null, null); refreshNotifs() }
    }

    // ---------- Clipboard ----------
    fun addClip(text: String) {
        val t = text.take(100_000)
        if (t.isBlank()) return
        io.execute {
            val db = d()
            var existing = -1L
            db.rawQuery("SELECT id FROM clips WHERE body=? LIMIT 1", arrayOf(t)).use {
                if (it.moveToFirst()) existing = it.getLong(0)
            }
            val now = System.currentTimeMillis()
            if (existing >= 0) {
                val cv = ContentValues().apply { put("time", now) }
                db.update("clips", cv, "id=?", arrayOf(existing.toString()))
            } else {
                val cv = ContentValues().apply { put("body", t); put("time", now); put("pinned", 0) }
                db.insert("clips", null, cv)
                db.execSQL("DELETE FROM clips WHERE pinned=0 AND id NOT IN (SELECT id FROM clips WHERE pinned=0 ORDER BY id DESC LIMIT 2000)")
            }
            refreshClips()
        }
    }

    fun deleteClip(id: Long) {
        io.execute { d().delete("clips", "id=?", arrayOf(id.toString())); refreshClips() }
    }

    fun togglePin(id: Long) {
        io.execute {
            d().execSQL("UPDATE clips SET pinned = 1 - pinned WHERE id=?", arrayOf<Any>(id))
            refreshClips()
        }
    }

    fun clearClips() {
        io.execute { d().delete("clips", "pinned=0", null); refreshClips() }
    }
}
