package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var apiService: ApiService
    private var userId: String? = null
    private var cartItems = mutableListOf<CartItem>()
    private lateinit var backImg: ImageView
    private lateinit var btnCheckout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        backImg = findViewById(R.id.imgViewBackMain)
        btnCheckout = findViewById(R.id.btnCheckout)

        // Initialize Retrofit
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        Log.d("CartActivity", "API Service Initialized")

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)
        Log.d("CartActivity", "User ID retrieved: $userId")

        setupRecyclerView()
        fetchCartItems()

        backImg.setOnClickListener {
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val userType = sharedPreferences.getString("userType", null)
            Log.d("CartActivity", "User type: $userType")
            if (userType == "admin") {
                startActivity(Intent(this@CartActivity, AdminHomeActivity::class.java))
            } else {
                startActivity(Intent(this@CartActivity, CustomerHomeActivity::class.java))
            }
        }

        btnCheckout.setOnClickListener {
            checkout()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems, onRemoveFromCart = { cartItem ->
            removeFromCart(cartItem)
        })
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter
        Log.d("CartActivity", "RecyclerView setup with adapter")
    }

    private fun fetchCartItems() {
        userId?.let { userId ->
            Log.d("CartActivity", "Fetching cart items for user: $userId")
            apiService.getCartItems(userId).enqueue(object : Callback<List<CartItem>> {
                override fun onResponse(
                    call: Call<List<CartItem>>,
                    response: Response<List<CartItem>>
                ) {
                    Log.d("CartActivity", "API response received")
                    if (response.isSuccessful) {
                        val fetchedCartItems = response.body()
                        if (fetchedCartItems != null) {
                            Log.d("CartActivity", "Cart items loaded: ${fetchedCartItems.size}")
                            // Directly update cartItems list
                            cartItems.clear()
                            cartItems.addAll(fetchedCartItems.filter { it.product != null })
                            // Update RecyclerView
                            cartAdapter.updateCartItems(cartItems)
                        }
                    } else {
                        Log.e("CartActivity", "Failed to load cart items: ${response.code()}")
                        Toast.makeText(
                            this@CartActivity,
                            "Failed to load cart items",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<CartItem>>, t: Throwable) {
                    Log.e("CartActivity", "Error fetching cart items: ${t.message}", t)
                    Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }

    private fun removeFromCart(cartItem: CartItem) {
        userId?.let { userId ->
            Log.d("CartActivity", "Removing item from cart: ${cartItem.product.prodName}")
            apiService.removeCartItem(userId, cartItem.product._id)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.d("CartActivity", "API response for remove: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("CartActivity", "Cart Items before checkout: ${cartItems.size}")
                            cartItems.forEach { cartItem ->
                                Log.d("CartActivity", "Product: ${cartItem.product}")
                            }
                            cartItems.remove(cartItem)
                            cartAdapter.updateCartItems(cartItems)
                            Toast.makeText(
                                this@CartActivity,
                                "${cartItem.product.prodName} removed from cart",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.e("CartActivity", "Failed to remove item: ${response.code()}")
                            Toast.makeText(
                                this@CartActivity,
                                "Failed to remove item",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("CartActivity", "Error removing item: ${t.message}", t)
                        Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
        }
    }

    private fun checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this@CartActivity, "Your cart is empty. Please add items to proceed.", Toast.LENGTH_SHORT).show()
            return
        }
        userId?.let { userId ->
            Log.d("CartActivity", "Initiating checkout for user: $userId")
            val transaction = Transaction(
                transId = "",  // Empty string for now; transaction ID will be generated by the backend
                userId = userId,
                transDate = "",  // Empty, will be populated by the backend
                transTime = "",  // Empty, will be populated by the backend
                products = cartItems.map { it.product }  // Only send the product data for the transaction
            )

            apiService.createTransaction(transaction).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("CartActivity", "Checkout API response: ${response.code()}")
                    if (response.isSuccessful) {
                        // Clear the cart and navigate to another screen if needed
                        cartItems.clear()
                        cartAdapter.updateCartItems(cartItems)
                        Toast.makeText(
                            this@CartActivity,
                            "Checkout successful and cart cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("CartActivity", "Checkout failed: ${response.code()}")
                        Toast.makeText(this@CartActivity, "Checkout failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("CartActivity", "Error during checkout: ${t.message}", t)
                    Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}

