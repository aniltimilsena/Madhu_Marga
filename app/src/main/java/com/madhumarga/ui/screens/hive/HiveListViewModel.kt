package com.madhumarga.ui.screens.hive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Hive
import com.madhumarga.data.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HiveListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = HiveRepository(db.hiveDao())

    val hives: Flow<List<Hive>> = repository.getAllHives()

    fun deleteHive(hive: Hive) {
        viewModelScope.launch {
            repository.deleteHive(hive)
        }
    }
}
