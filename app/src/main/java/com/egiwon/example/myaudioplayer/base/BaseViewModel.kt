package com.egiwon.example.myaudioplayer.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class BaseViewModel: ViewModel() {

    protected val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            errorState.postValue(throwable)
        }

    protected val errorState = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = errorState
}
