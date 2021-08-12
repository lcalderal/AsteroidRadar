package com.udacity.asteroidradar.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.NasaApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class Repository(private val database: AsteroidDatabase){

    val pictureOfDay: LiveData<PictureOfDay> = Transformations.map(
        database.asteroidDao.getPictureOfDay()){ it?.asDomainModel() }

    val todayAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getTodayAsteroids()){ it.asDomainModel()}
    val weeklyAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getWeeklyAsteroids()){ it.asDomainModel()}
    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAsteroids()){ it.asDomainModel()}

    suspend fun refreshPictureOfDay(){
        withContext(Dispatchers.IO){
            try {

                val pictureOfDay = NasaApi.retrofitMoshiService.getPictureOfDay(
                    apiKey = Constants.API_KEY)
                // convert them to array of DatabaseAsteroids and insert all
                database.asteroidDao.insertPictureOfDay(pictureOfDay.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e("ERROR: $e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val today = getFormattedDate()
                val stringResponse = NasaApi.retrofitScalarService.getAsteroids(
                    apiKey = Constants.API_KEY, startDate = today , endDate = null)
                val networkAsteroids = parseAsteroidsJsonResult(JSONObject(stringResponse))
                database.asteroidDao.insertAll(*networkAsteroids.asDatabaseModel())
            } catch (e: Exception) {
                Timber.e("Exception: $e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun clearOldAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val today = getFormattedDate()
                database.asteroidDao.clearOldAsteroids(today)
                Timber.d("Old Asteroids clear")
            } catch (e: Exception) {
                Timber.e("Excepction clearOldAsteroids: $e")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun clearOldPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                val today = getFormattedDate()
                database.asteroidDao.clearOldPictureOfDay(today)
                Timber.d("Old picture of day clear")
            } catch (e: Exception) {
                Timber.e("Exception clearOldPictureOfDay: $e")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun getFormattedDate(days: Int=0): String{
    val calendar = Calendar.getInstance()
    if(days > 0){
        calendar.add(Calendar.DAY_OF_YEAR, days)
    }
    val currentTime = calendar.time
    val dateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    } else {
        TODO("VERSION.SDK_INT < N")
    }
    return dateFormat.format(currentTime)
}