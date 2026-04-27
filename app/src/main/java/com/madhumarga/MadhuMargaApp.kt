package com.madhumarga

import android.app.Application
import com.madhumarga.data.db.AppDatabase

class MadhuMargaApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
