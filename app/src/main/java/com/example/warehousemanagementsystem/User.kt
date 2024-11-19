package com.example.warehousemanagementsystem

data class User(
    val id: Int,
    val fullname: String? = null,
    val phone: String? = null,
    val email: String,
    val password: String,
    val type: String? = null
)


data class UserProfile(
    val fullname: String,
    val email: String,
    val phone: String
)
