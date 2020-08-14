package com.pilove.vodovodinfo.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilove.vodovodinfo.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    fun getNotices() = viewModelScope.launch {
         mainRepository.getNoticesFromServer()
    }

}