package com.udacity.asteroidradar.main

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.Repository
import kotlinx.coroutines.launch
import java.lang.Exception

enum class AsteroidStatus {DONE, ERROR, LOADING}

@RequiresApi(Build.VERSION_CODES.N)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val repository = Repository(database)

    private val _status = MutableLiveData<AsteroidStatus>()
    val status: LiveData<AsteroidStatus> get() = _status

    private val _filter = MutableLiveData<AsteroidApiFilter>(AsteroidApiFilter.SAVED)
    val filter: LiveData<AsteroidApiFilter> get() = _filter

    val asteroids = Transformations.switchMap(_filter) {
        when(it!!){
            AsteroidApiFilter.TODAY -> repository.todayAsteroids
            AsteroidApiFilter.WEEK -> repository.weeklyAsteroids
            else -> repository.asteroids
        }
    }

    val pictureOfDay = repository.pictureOfDay

    private val _goToDetail = MutableLiveData<Asteroid>()
    val goToDetail: LiveData<Asteroid> get() = _goToDetail

    fun onGoToDetailCompleted(){
        _goToDetail.value = null
    }

    fun onAsteroidClicked(asteroid: Asteroid){
        _goToDetail.value = asteroid
    }

    init {
        viewModelScope.launch {
            try {
                _status.value = AsteroidStatus.LOADING
                repository.refreshAsteroids()
            }catch(e: Exception){
                _status.value = AsteroidStatus.ERROR
            }finally {
                _status.value = AsteroidStatus.DONE
            }
        }
        viewModelScope.launch {
            repository.refreshPictureOfDay()
        }
    }

    fun updateFilter(filter: AsteroidApiFilter) {
        _filter.value = filter
    }

    /**
     * Factory for constructing MainViewModel with parameter (application)
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
