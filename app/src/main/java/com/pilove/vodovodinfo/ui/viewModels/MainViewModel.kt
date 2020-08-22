package com.pilove.vodovodinfo.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.repositories.MainRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository,
    @ApplicationContext private val app: Context
): ViewModel() {

    var isConnected = false

    val notices: LiveData<List<Notice>> = ConnectionLiveData(app).switchMap {
        isConnected = it
        if (it) {
           mainRepository.getNoticesFromServer()
        } else {
            mainRepository.getNoticesFromDb()
        }
    }


    fun insertNotices(notices: List<Notice>?) = viewModelScope.launch(Dispatchers.IO) {
        if(!isConnected) return@launch
        notices?.let {
            it.forEach { notice ->
                mainRepository.insertNotice(notice)
            }
        }
    }

}