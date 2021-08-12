package com.udacity.asteroidradar.work

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.Repository
import retrofit2.HttpException

class RefreshData(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshData"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = Repository(database)

        return try {
            repository.refreshAsteroids()
            repository.refreshPictureOfDay()
            repository.clearOldAsteroids()
            repository.clearOldPictureOfDay()
            Result.success()
        } catch (e: HttpException) {
            Result.retry()
        }
    }


}