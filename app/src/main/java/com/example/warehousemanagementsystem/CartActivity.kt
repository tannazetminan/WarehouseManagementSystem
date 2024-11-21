package com.example.warehousemanagementsystem

import android.os.Bundle
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
    private var cartItems = mutableListOf<Product>()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        setupRecyclerView()

        // Fetch cart items from backend
        userId?.let { fetchCartItems(it) }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems) { product ->
            removeFromCart(product)
        }
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter
    }

    private fun fetchCartItems(userId: String) {
        apiService.getCartItems(userId).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    cartItems.clear()
                    cartItems.addAll(response.body()!!)
                    cartAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CartActivity, "Failed to load cart", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@CartActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFromCart(product: Product) {
        userId?.let {
            apiService.removeCartItem(it, product._id.toString()).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        cartItems.remove(product)
                        cartAdapter.notifyDataSetChanged()
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



