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

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository,
    @ApplicationContext private val app: Context
): ViewModel() {

    val notices = MediatorLiveData<List<Notice>>()
    val resultFromDb = mainRepository.getLastTenNotices()
    val resultFromServer = mainRepository.getNoticesFromServer()

    init {
        notices.addSource(resultFromDb) { result->
            if(connectionLiveData.value == false)
                notices.value = result
        }
        notices.addSource(resultFromServer as LiveData<List<Notice>>) { result ->
            if(connectionLiveData.value == true)
                notices.value = result as List<Notice>
        }
    }


     fun getNotices() = viewModelScope.launch {
         delay(1000L)
         when (connectionLiveData.value) {
             false -> resultFromDb.value?.let {
                 Log.d(DEBUG_TAG, "SOURCE JE LOKALNO")
                 notices.value = it
             }
             true -> {
                 Log.d(DEBUG_TAG, "SOURCE JE server")
                 mainRepository.getNoticesFromServer()
                 (resultFromServer as LiveData<List<Notice>>).value?.let {
                     notices.value = it
                     it.forEach { notice ->
                       mainRepository.insertNotice(notice)
                     }
                 }
             }
             else -> {
                 Log.d(DEBUG_TAG, "SOURCE JE else")
             }
         }
     }


     var connectionLiveData = ConnectionLiveData(app)

    fun getConnectionStatus(context: Context) {
        connectionLiveData = mainRepository.getConnectionLiveData(context)
    }
}