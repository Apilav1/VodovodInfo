package com.pilove.vodovodinfo.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val notices = MutableLiveData<ArrayList<Notice>>()

    fun getNotices() = viewModelScope.launch {
         notices.value = mainRepository.getNoticesFromServer()
    }

}