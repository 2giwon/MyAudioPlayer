package com.egiwon.example.myaudioplayer.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class BaseAndroidViewModel(
    application: Application
): AndroidViewModel(application) {

    protected val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            errorState.postValue(throwable)
        }

    protected val errorState = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = errorState
}
