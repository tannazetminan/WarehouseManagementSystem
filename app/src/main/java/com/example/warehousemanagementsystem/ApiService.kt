package com.example.warehousemanagementsystem

import retrofit2.Call
import retrofit2.http.*
import okhttp3.MultipartBody


interface ApiService {
    @POST("register")
    fun registerUser(@Body user: User): Call<Void>

    @POST("login")
    fun loginUser(@Body user: User): Call<LoginResponse>
    // New endpoint to upload image
    @Multipart
    @POST("upload_image")
    fun uploadImage(@Part file: MultipartBody.Part): Call<ImageResponse>

    // Create product with the image URL
    @POST("create_product")
    fun createProduct(@Body product: Product): Call<Void>
}

data class LoginResponse(val message: String, val type: String)
// Response model for image upload
data class ImageResponse(
    val image_url: String  // URL of the uploaded image
)