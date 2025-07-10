package com.example.yourappname.data.network

import com.example.yourappname.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    // Asume que el endpoint es "dataEndpoint" y el parámetro se llama "companyCode"
    // Ejemplo: https://your.api.url/dataEndpoint?companyCode=12345
    // Ajusta "dataEndpoint" y "companyCode" según tu API real.
    @GET("dataEndpoint") // Reemplaza "dataEndpoint" con tu endpoint real
    suspend fun getUsers(
        @Query("companyCode") companyCode: String // Reemplaza "companyCode" con el nombre real del parámetro
    ): Response<ApiResponse> // ApiResponse es la clase que contiene la lista "Table"
}
