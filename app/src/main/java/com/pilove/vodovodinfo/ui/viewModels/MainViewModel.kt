package com.pilove.vodovodinfo.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.repositories.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val notices = MutableLiveData<ArrayList<Notice>>()

    fun getNotices() = viewModelScope.launch {
        notices.postValue(mainRepository.getNoticesFromServer())
    }

//    val notices = liveData<ArrayList<Notice>>(Dispatchers.IO) {
//        val data = mainRepository.getNoticesFromServer()
//        emit(data)
//    }
}