package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.warehousemanagementsystem.apis.ApiService
import com.example.warehousemanagementsystem.apis.RetrofitClient
import com.example.warehousemanagementsystem.apis.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class CartActivity : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var apiService: ApiService
    private var userId: String? = null
    private var cartItems = mutableListOf<Product>()
    private lateinit var backImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        backImg = findViewById(R.id.imgViewBackMain)


        // Initialize Retrofit
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        setupRecyclerView()
        fetchCartItems()

        backImg.setOnClickListener{
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val userType = sharedPreferences.getString("userType", null)
            if (userType == "admin") {
                startActivity(Intent(this@CartActivity, AdminHomeActivity::class.java))
            } else {
                startActivity(Intent(this@CartActivity, CustomerHomeActivity::class.java))
            }
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems, onRemoveFromCart = { product ->
            removeFromCart(product)
        })
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter
    }

    private fun fetchCartItems() {
        userId?.let { userId ->
            apiService.getCartItems(userId).enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful) {
                        cartItems.clear()
                        cartItems.addAll(response.body()!!)
                        cartAdapter.updateCartItems(cartItems)
                    } else {
                        Toast.makeText(this@CartActivity, "Failed to load cart items", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun removeFromCart(product: Product) {
        userId?.let { userId ->
            apiService.removeCartItem(userId, product._id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        cartItems.remove(product)
                        cartAdapter.updateCartItems(cartItems)
                        Toast.makeText(this@CartActivity, "${product.prodName} removed from cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CartActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}
