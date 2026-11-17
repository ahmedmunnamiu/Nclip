package com.ahmedmunna.nclip

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SavedItem(val id: Long, val kind: String, val title: String, val body: String, val app: String, val time: Long)

class Store(context: Context) : SQLiteOpenHelper(context, "nclip.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE entries (id INTEGER PRIMARY KEY AUTOINCREMENT, kind TEXT NOT NULL, title TEXT, body TEXT, app TEXT, time INTEGER)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(kind: String, title: String, body: String, app: String = "") {
        val v = ContentValues().apply {
            put("kind", kind); put("title", title); put("body", body); put("app", app); put("time", System.currentTimeMillis())
        }
        writableDatabase.insert("entries", null, v)
    }
    fun list(kind: String): List<SavedItem> {
        val result = mutableListOf<SavedItem>()
        readableDatabase.query("entries", null, "kind=?", arrayOf(kind), null, null, "time DESC", "300").use { c ->
            while (c.moveToNext()) result.add(SavedItem(c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("kind")), c.getString(c.getColumnIndexOrThrow("title")) ?: "", c.getString(c.getColumnIndexOrThrow("body")) ?: "", c.getString(c.getColumnIndexOrThrow("app")) ?: "", c.getLong(c.getColumnIndexOrThrow("time"))))
        }
        return result
    }
    fun delete(id: Long) { writableDatabase.delete("entries", "id=?", arrayOf(id.toString())) }
    fun clear(kind: String) { writableDatabase.delete("entries", "kind=?", arrayOf(kind)) }
}
