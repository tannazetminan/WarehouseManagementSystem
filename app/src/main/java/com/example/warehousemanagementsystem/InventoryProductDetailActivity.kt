package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryProductDetailActivity : AppCompatActivity() {


    private lateinit var apiService: ApiService
    private var userId: String? = null
    private lateinit var btnGoToEditProduct: Button
    private lateinit var btnDeleteProduct: Button


    private var productId: String ? =null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inventory_product_detail)
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        productId = intent.getStringExtra("product_id")

        if (productId != null) {
            fetchProductDetails(productId!!)
        }

        btnDeleteProduct =findViewById(R.id.btnDeleteProduct);
        btnGoToEditProduct = findViewById(R.id.btnGoToEditProduct);

        btnGoToEditProduct.setOnClickListener {
            //put product id in intent and pass it to new activity. start new activity
            val intent = Intent(this, InventoryProductEditActivity::class.java)
            intent.putExtra("product_id", productId)
            startActivity(intent)

        }


        btnDeleteProduct.setOnClickListener{

            //have user verify they want to delete the product
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Are you sure you want to delete this product?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    deleteSingleProduct(productId!!)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                }

            val alert = dialogBuilder.create()
            alert.show()

        }

    }

    private fun deleteSingleProduct(productId: String) {
        apiService.deleteSingleProduct(productId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@InventoryProductDetailActivity, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                    // Optionally, go back to previous screen or refresh product list
                    finish() // Close the activity
                } else {
                    Toast.makeText(this@InventoryProductDetailActivity, "Failed to delete product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@InventoryProductDetailActivity, "Network failure", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchProductDetails(productId: String) {
        apiService.getSingleProductById(productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val product = response.body()
                    product?.let {
                        // Display product details
                        findViewById<TextView>(R.id.ipdProductName).text = it.prodName
                        findViewById<TextView>(R.id.ipdProductCategory).text = "Category: ${it.prodCategory ?: "N/A"}"
                        findViewById<TextView>(R.id.ipdProductDescription).text = it.prodDescription ?: "No description available"
                        findViewById<TextView>(R.id.ipdProductCostPrice).text = "Cost Price: $${it.costPrice}"
                        findViewById<TextView>(R.id.ipdProductSalePrice).text = "Sale Price: $${it.salePrice}"

                        // Load the image using Glide (if image_url is available)
                        it.image_url?.let { imageUrl ->
                            Glide.with(this@InventoryProductDetailActivity)
                                .load(imageUrl)
                                .into(findViewById(R.id.ipdProductImage)) // Assuming you have an ImageView with id 'ipdProductImage'
                        }
                    }
                } else {
                    // Handle error response
                    Toast.makeText(this@InventoryProductDetailActivity, "Error fetching product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                // Handle failure (network issues, etc.)
                Toast.makeText(this@InventoryProductDetailActivity, "Network failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

}