package com.pilove.vodovodinfo.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.pilove.vodovodinfo.BaseApplication
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.repositories.MainRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository,
    val app: Context
): ViewModel() {

    val notices = MutableLiveData<ArrayList<Notice>>()

    fun getNotices() = viewModelScope.launch {
        delay(1000L)
        if(connectionLiveData.value == true){
            Log.d(DEBUG_TAG, "DOHVACAM SA SERVERA")
            val result = mainRepository.getNoticesFromServer()
            notices.postValue(result)
            result.forEach {
                mainRepository.insertNotice(it)
            }
        }
        else {
            Log.d(DEBUG_TAG, "DOHVACAM lokalno")
            val result = mainRepository.getLastTenNotices().value
            if(result != null)
                notices.postValue(result as ArrayList<Notice>)
        }
    }

    var connectionLiveData = ConnectionLiveData(app)

    fun getConnectionStatus(context: Context) {
        connectionLiveData = mainRepository.getConnectionLiveData(context)
    }
    //val context = BaseApplication().applicationContext

//    val connectionLiveData = liveData<Boolean> {
//        val data = mainRepository.getConnectionLiveData(app)
//        if(data != null || data.value != null)
//             emit(data.value!!)
//    }
//    val notices = liveData<ArrayList<Notice>>(Dispatchers.IO) {
//        val data = mainRepository.getNoticesFromServer()
//        emit(data)
//    }
}