package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

object DatabaseLocator {
    @Volatile
    private var repository: HostelRepository? = null
    private val scope = CoroutineScope(SupervisorJob())

    fun getRepository(context: Context): HostelRepository {
        return repository ?: synchronized(this) {
            val database = AppDatabase.getDatabase(context.applicationContext, scope)
            val repo = HostelRepository(database)
            repository = repo
            repo
        }
    }
}
