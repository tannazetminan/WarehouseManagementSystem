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

    // Getting all products
    @GET("retrieve_all_products")
    fun getAllProducts(): Call<List<Product>>

    @GET("user/{user_id}")
    fun getUserProfile(@Path("user_id") userId: String): Call<UserProfile>

    @PUT("user/{user_id}")
    fun updateUserProfile(@Path("user_id") userId: String, @Body profile: UserProfile): Call<Void>

    @POST("cart/{userId}/add")
    fun addCartItem(
        @Path("userId") userId: String,
        @Body productIdMap: Map<String, String>
    ): Call<Void>

    @GET("/cart/{userId}")
    fun getCartItems(@Path("userId") userId: String): Call<List<CartItem>>

    @DELETE("/cart/{userId}/{productId}")
    fun removeCartItem(@Path("userId") userId: String, @Path("productId") productId: String): Call<Void>

    @DELETE("delete_single_product/{product_id}")
    fun deleteSingleProduct(@Path("product_id") productId:String): Call<Void>

    //need to send updates quatity to certain product ID, not the whole Object to match backend
    @PUT("update_productQuantity/{productId}")
    fun updateProductQuantity(
        @Path("productId") productId: String,
        @Body quantityUpdate: Map<String, String>
    ): Call<Void>


    @GET("retrieve_single_product/{product_id}")
    fun getSingleProductById(@Path("product_id") productId: String): Call<Product>

    @PUT("/update_single_product/{product_id}")
    fun updateSingleProduct(
        @Path("product_id") productId: String,
        @Body product: Product
    ):Call<Void>

    @POST("create_transaction")
    fun createTransaction(@Body transaction: Transaction): Call<Void>

    @GET("retrieve_all_transactions")
    fun getAllTransactions(): Call<List<Transaction>>

    @GET("retrieve_transaction_by_id/{trans_id}")
    fun getTransactionById(@Path("trans_id") transId: String): Call<Transaction>

    // Get user by user_id for transaction
    @GET("retrieve_user_by_id/{user_id}")
    fun getUserById(@Path("user_id") userId: String): Call<User>

    //get all sessions for admin tracking
    @GET("get_all_sessions")
    fun getAllSessions(): Call<List<Session>>

}
data class LoginResponse(
    val message: String,
    val type: String,
    val user_id: String
)

//data class LoginResponse(val message: String, val type: String)
// Response model for image upload
data class ImageResponse(
    val image_url: String  // URL of the uploaded image
)