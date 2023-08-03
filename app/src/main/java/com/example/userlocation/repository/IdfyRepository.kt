package com.example.userlocation.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userlocation.model.IdfyRequest
import com.example.userlocation.model.IdfyResponse
import com.example.userlocation.services.ApiServices


class IdfyRepository(private val apiServices: ApiServices){

    //Success
    private val responseLiveData= MutableLiveData<IdfyResponse?>()
    val response: LiveData<IdfyResponse?>
    get()= responseLiveData


    //Error
    private val errorLiveData= MutableLiveData<Boolean>()
    val errorResponse: LiveData<Boolean>
        get()= errorLiveData


    //loader
    val loaderState = MutableLiveData<Boolean>()



    suspend fun getUrl(idfyRequest: IdfyRequest) {
        try {
            Log.d("APPDATA_1",idfyRequest.toString())

            loaderState.postValue(true)

            val result = apiServices.getProfile(idfyRequest)
            //200->
            if (result.isSuccessful) {
                if (result.body() != null) {
                    loaderState.postValue(false)
                    responseLiveData.postValue(result.body())
                    Log.d("APPDATA_2","Success")
                }
            } else {
                //400->
                loaderState.postValue(false)
                errorLiveData.postValue(true)
                Log.d("APPDATA_3","Error")
            }
        } catch (e: Exception) {
            loaderState.postValue(false)
            errorLiveData.postValue(true)
            Log.d("APPDATA_4","Error Exception")
        }
    }
}