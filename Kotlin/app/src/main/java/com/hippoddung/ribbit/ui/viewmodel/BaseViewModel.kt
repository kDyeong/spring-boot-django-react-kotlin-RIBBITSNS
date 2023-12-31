package com.hippoddung.ribbit.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class BaseViewModel : ViewModel() {
    private var mJob: Job? = null

    protected fun <T> baseRequest(
        liveData: MutableLiveData<T>,
        errorHandler: CoroutinesErrorHandler,
        request: () -> Flow<T>
    ) {
        mJob = viewModelScope.launch(
            Dispatchers.IO + CoroutineExceptionHandler { _, error ->
                viewModelScope.launch(Dispatchers.Main) {
                    errorHandler.onError(
                        error.localizedMessage ?: "Error occurred! Please try again."
                    )
                }
            }
        ) {
            request().collect {
                withContext(Dispatchers.Main) {
                    liveData.value = it
                    Log.d("HippoLog, BaseViewModel", "${liveData.value}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
    }
}

interface CoroutinesErrorHandler {
    fun onError(message: String)
}