package com.example.warehousemanagementsystem

data class User(
    val fullname: String? = null,
    val phone: String? = null,
    val email: String,
    val password: String,
    val type: String? = null
)

