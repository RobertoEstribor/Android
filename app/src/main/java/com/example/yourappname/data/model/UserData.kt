package com.example.yourappname.data.model

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("ENTRADA")
    val entrada: String?, // Puede ser null si la API lo permite

    @SerializedName("CONFIRMA_N")
    val confirmaN: String?, // Puede ser null

    @SerializedName("NOMBRE")
    val nombre: String?,

    @SerializedName("CONFIRMA")
    val confirma: String?
)

// Clase contenedora para la respuesta JSON que tiene una clave "Table"
data class ApiResponse(
    @SerializedName("Table")
    val table: List<UserData>?
)
