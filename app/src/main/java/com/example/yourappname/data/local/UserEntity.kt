package com.example.yourappname.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "entrada")
    val entrada: String,

    @ColumnInfo(name = "confirma_n")
    val confirmaN: String?, // Puede ser null

    @ColumnInfo(name = "nombre") // Guardamos también el nombre por si es útil
    val nombre: String?
)
