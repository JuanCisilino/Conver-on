package com.frost.converon.ui.converter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frost.converon.helper.Resource
import com.frost.converon.helper.SingleLiveEvent
import com.frost.converon.model.ApiResponse
import com.frost.converon.repo.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(private val mainRepo: MainRepo) : ViewModel()  {


    private val _data = SingleLiveEvent<Resource<ApiResponse>>()

    val data  =  _data
    val convertedRate = MutableLiveData<Double>()

    fun getConvertedData(access_key: String, from: String, to: String, amount: Double) {
        viewModelScope.launch {
            mainRepo.getConvertedData(access_key, from, to, amount).collect { data.value = it }
        }
    }
}