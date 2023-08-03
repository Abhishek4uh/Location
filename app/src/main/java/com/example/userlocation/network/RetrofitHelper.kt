package com.example.userlocation.network


import com.example.userlocation.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitHelper{

    private const val DEFAULT_TIME_OUT = 30L
    private const val ACCOUNT_ID_HEADER = "account_id"
    private const val API_KEY_HEADER = "api-key"
    private const val ACCOUNT_ID_VALUE = "1f7d58de6ad9/6ce42acd-f75f-4fc0-95bc-1fb9c939244a"
    private const val API_KEY_VALUE = "323a1834-d20a-46b4-89ac-e83b07b70de5"

    fun getInstance(): Retrofit {

        val mHttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        val mOkHttpClient = OkHttpClient.Builder()
                            .connectTimeout(DEFAULT_TIME_OUT,TimeUnit.SECONDS)
                            .readTimeout(   DEFAULT_TIME_OUT,TimeUnit.SECONDS)
                            .writeTimeout(  DEFAULT_TIME_OUT,TimeUnit.SECONDS)
                            .addInterceptor(mHttpLoggingInterceptor)
                            .addInterceptor { chain->
                                val request = chain.request().newBuilder()
                                    .addHeader(ACCOUNT_ID_HEADER,ACCOUNT_ID_VALUE)
                                    .addHeader(API_KEY_HEADER,API_KEY_VALUE)
                                    .build()
                                chain.proceed(request)
                            }
                            .build()

        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(mOkHttpClient)
                .build()
    }

}