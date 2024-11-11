package com.example.warehousemanagementsystem

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    fun registerUser(@Body user: User): Call<Void>

    @POST("login")
    fun loginUser(@Body user: User): Call<LoginResponse>
}

data class LoginResponse(val message: String, val type: String)
