package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDAO {
    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate = date('now') ORDER BY closeApproachDate ASC")
    fun getTodayAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid WHERE closeApproachDate BETWEEN date('now') AND date('now', '+7 day') ORDER BY closeApproachDate ASC")
    fun getWeeklyAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * FROM databaseasteroid  ORDER BY closeApproachDate ASC")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

    //PICTURE OF THE DAY
    @Query("SELECT * FROM databasepictureofday ORDER BY created_at DESC LIMIT 1")
    fun getPictureOfDay(): LiveData<DatabasePictureOfDay>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfDay(pictureOfDay: DatabasePictureOfDay)

    @Query("DELETE FROM databaseasteroid WHERE closeApproachDate < :today")
    fun clearOldAsteroids(today: String)

    @Query("DELETE FROM databasepictureofday WHERE created_at < :today")
    fun clearOldPictureOfDay(today: String)

}

@Database(entities = [DatabaseAsteroid::class, DatabasePictureOfDay::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDAO
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {

    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            ).fallbackToDestructiveMigration().build()
        }
    }
    return INSTANCE
}