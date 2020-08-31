package com.pilove.vodovodinfo.ui.viewModels

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.repositories.MainRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*

class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository,
    @ApplicationContext private val app: Context
): ViewModel() {

    var isConnected = false

    var isDataSetFromServer = false

    val notices: LiveData<List<Notice>> = ConnectionLiveData(app).switchMap {
        isConnected = it
        if (it && !isDataSetFromServer) {
           isDataSetFromServer = true
           mainRepository.getNoticesFromServer(app)
        } else {
            isDataSetFromServer = false
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