package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productName: TextView
    private lateinit var productDescription: TextView
    private lateinit var productCategory: TextView
    private lateinit var productPrice: TextView
    private lateinit var productImage: ImageView
    private lateinit var backImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Initialize views
        productName = findViewById(R.id.productNameDetail)
        productDescription = findViewById(R.id.productDescription)
        productCategory = findViewById(R.id.productCategoryDetail)
        productPrice = findViewById(R.id.productPriceDetail)
        productImage = findViewById(R.id.productImageDetail)
        backImg = findViewById(R.id.imgViewBackMain)


        // Get the product ID passed from the previous activity
        val productId = intent.getStringExtra("product_id")

        // Fetch product details
        productId?.let {
            fetchProductDetails(it)
        }


        backImg.setOnClickListener{
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val userType = sharedPreferences.getString("userType", null)
            if (userType == "admin") {
                startActivity(Intent(this@ProductDetailActivity, AdminHomeActivity::class.java))
            } else {
                startActivity(Intent(this@ProductDetailActivity, CustomerHomeActivity::class.java))
            }
        }
    }

    private fun fetchProductDetails(productId: String) {
        // Call the API to get the product details (assuming `apiService` is initialized)
        val apiService = RetrofitClient.getRetrofitInstance("BASE_URL").create(ApiService::class.java)
        apiService.getSingleProductById(productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val product = response.body()
                    product?.let {
                        // Populate the UI with the product details
                        productName.text = it.prodName
                        productDescription.text = it.prodDescription ?: "No description available"
                        productCategory.text = it.prodCategory ?: "Unknown"
                        productPrice.text = "$${it.salePrice}"
//                        Glide.with(this@ProductDetailActivity)
//                            .load(it.image_url)
//                            .error(R.drawable.placeholder)
//                            .into(productImage)
                        // Get the image URL, removing the base URL before "https://"
                        val imageUrl = product.image_url?.let {
                            // Find where the URL starts with "https://"
                            val startIndex = it.indexOf("https://")
                            if (startIndex != -1) {
                                it.substring(startIndex) // Keep everything after "https://"
                            } else {
                                it // If no "https://" is found, use the original URL
                            }
                        }

                        // Load the image URL using Glide
//                        Glide.with(this@ProductDetailActivity)
//                            .load(imageUrl)
//                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching temporarily
//                            .error(R.drawable.placeholder)
//                            .into(productImage)
                        Glide.with(this@ProductDetailActivity)
                            .load(product.image_url ?: R.drawable.placeholder)  // Use placeholder image if image_url is null
                            .into(productImage)

                    }
                } else {
                    Toast.makeText(this@ProductDetailActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Toast.makeText(this@ProductDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
