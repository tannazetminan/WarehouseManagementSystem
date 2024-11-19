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

    // Getting all produtcs
    @GET("retrieve_all_products")
    fun getAllProducts(): Call<List<Product>>

    @GET("user/{user_id}")
    fun getUserProfile(@Path("user_id") userId: String): Call<UserProfile>

    @PUT("user/{user_id}")
    fun updateUserProfile(@Path("user_id") userId: String, @Body profile: UserProfile): Call<Void>

    @GET("cart/{user_id}")
    fun getCartItems(@Path("user_id") userId: String): Call<List<Product>>

    @POST("cart/{user_id}")
    fun addCartItem(@Path("user_id") userId: String, @Body product: Product): Call<Void>

    @DELETE("cart/{user_id}/{product_id}")
    fun removeCartItem(@Path("user_id") userId: String, @Path("product_id") productId: String): Call<Void>

}

data class LoginResponse(val message: String, val type: String)
// Response model for image upload
data class ImageResponse(
    val image_url: String  // URL of the uploaded image
)