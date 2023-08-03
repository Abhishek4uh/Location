package com.example.userlocation.services


import com.example.userlocation.model.IdfyRequest
import com.example.userlocation.model.IdfyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {

    @POST("sync/profiles")
    suspend fun getProfile(@Body requestBody:IdfyRequest): Response<IdfyResponse>

}