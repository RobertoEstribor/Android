package com.example.userapp.data.network

import com.example.userapp.data.model.User // Ensure this is the updated User class
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("ilionservices4/wsmantenimiento/wsmantenimiento.asmx/GetUsuarios")
    @Headers(
        "Accept: application/json, text/javascript, */*; q=0.01",
        "Content-Type: application/x-www-form-urlencoded; charset=UTF-8"
    )
    suspend fun getUsuarios(@Field("nempresa") nempresa: String): Response<List<User>>
}
