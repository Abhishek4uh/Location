package com.example.userlocation.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.userlocation.model.IdfyRequest
import com.example.userlocation.model.IdfyResponse
import com.example.userlocation.repository.IdfyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IdfyViewmodel(private  val idfyRepository: IdfyRepository):ViewModel(){

    fun call(idfyRequest: IdfyRequest){
        viewModelScope.launch(Dispatchers.IO) {
            idfyRepository.getUrl(idfyRequest)
        }
    }

    val response: LiveData<IdfyResponse?>
    get()= idfyRepository.response


    val errorMsg:LiveData<Boolean>
    get()= idfyRepository.errorResponse

    val loader:LiveData<Boolean>
    get()= idfyRepository.loaderState

}