package com.ahmedmunna.nclip

import android.app.Application
import com.ahmedmunna.nclip.data.Repo

class NclipApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.init(this)
    }
}
