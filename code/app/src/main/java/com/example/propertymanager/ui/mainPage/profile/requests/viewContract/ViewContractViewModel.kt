package com.example.propertymanager.ui.mainPage.profile.requests.viewContract

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.propertymanager.data.model.ClientRequest
import com.example.propertymanager.data.repository.ClientRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class ViewContractViewModel @Inject constructor(
    private val repository: ClientRequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _actionSuccess = MutableLiveData<Boolean>()
    val actionSuccess: LiveData<Boolean> = _actionSuccess

    fun acceptRequest(request: ClientRequest) {
        _loading.value = true
        repository.acceptRequestAndMarkContractAccepted(
            request,
            onSuccess = {
                _loading.postValue(false)
                _actionSuccess.postValue(true)
            },
            onFailure = {
                _loading.postValue(false)
                _actionSuccess.postValue(false)
            }
        )
    }

    fun denyRequest(request: ClientRequest) {
        _loading.value = true
        repository.denyRequestAndMarkContractDenied(
            request,
            onSuccess = {
                _loading.postValue(false)
                _actionSuccess.postValue(true)
            },
            onFailure = {
                _loading.postValue(false)
                _actionSuccess.postValue(false)
            }
        )
    }
}
