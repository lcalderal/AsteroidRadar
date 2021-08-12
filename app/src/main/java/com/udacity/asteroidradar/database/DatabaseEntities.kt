package com.udacity.asteroidradar.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay

@Entity
data class DatabaseAsteroid constructor(
    @PrimaryKey
    val id:Long,
    val codename: String,
    val closeApproachDate: String,
    val absoluteMagnitude: Double,
    val estimatedDiameter: Double,
    val relativeVelocity: Double,
    val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean)

@Entity
data class DatabasePictureOfDay constructor(
    @PrimaryKey
    val url: String,

    val mediaType: String,
    val title: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long)

fun DatabasePictureOfDay.asDomainModel(): PictureOfDay {
    return PictureOfDay(
        url = this.url,
        mediaType = this.mediaType,
        title = this.title
    )

}

fun List<DatabaseAsteroid>.asDomainModel(): List<Asteroid> {
    return map {
        Asteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}