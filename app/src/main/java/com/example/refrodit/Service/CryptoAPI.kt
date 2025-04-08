package com.example.refrodit.Service

import com.example.refrodit.Model.CryptoModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET


//https://raw.githubusercontent.com/
interface CryptoAPI {
    @GET("atilsamancioglu/K21-JSONDataSet/master/crypto.json")

    suspend fun getData(): Response<List<CryptoModel>>
}