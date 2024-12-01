package com.example.warehousemanagementsystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.warehousemanagementsystem.apis.ApiService
import com.example.warehousemanagementsystem.apis.RetrofitClient
import com.example.warehousemanagementsystem.apis.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var apiService: ApiService
private var userId: String? = null
private lateinit var btnEditProduct: Button


private lateinit var etIpProductImage: EditText
private lateinit var etIpProductName: EditText
private lateinit var etIpProductDecsription: EditText
private lateinit var etIpProductCategory: EditText
private lateinit var etIpProductCostPrice: EditText
private lateinit var etIpProductSalePrice: EditText

private var product: Product? = null;

private var productId: String ? =null;

class InventoryProductEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inventory_product_edit)

        //initialize the apiservice
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        //retrieve product id from intent

        productId = intent.getStringExtra("product_id")
        //get the product
        productId?.let {
            fetchProductDetails(it)
        }

        //initialize the views
        etIpProductImage = findViewById(R.id.etIpEditImage)
        etIpProductName = findViewById(R.id.etIpEditName)
        etIpProductDecsription= findViewById(R.id.etIpEditDescription)
        etIpProductCategory = findViewById(R.id.etIpEditCategory)
        etIpProductCostPrice = findViewById(R.id.etIpEditCostPrice)
        etIpProductSalePrice = findViewById(R.id.etIpEditSalePrice)
        btnEditProduct = findViewById(R.id.btnEditProduct)
        btnEditProduct = findViewById(R.id.btnEditProduct)

        //set on click
        btnEditProduct.setOnClickListener {

            updateProduct();

        }

        //on the button click check each data field
        //if it is not empty set the product variable to the value in the et.
        // If it is empty use the exisitng value
    }


    private fun fetchProductDetails(productId: String) {
        apiService.getSingleProductById(productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                if (response.isSuccessful) {
                    val productFromApi = response.body()
                    productFromApi?.let {
                        // Assign the fetched product to the class-level product variable
                        product = it

                        // Pre-fill the UI with the existing product details
                        etIpProductImage.setText(it.image_url ?: "")
                        etIpProductName.setText(it.prodName ?: "")
                        etIpProductDecsription.setText(it.prodDescription ?: "")
                        etIpProductCategory.setText(it.prodCategory ?: "")
                        etIpProductCostPrice.setText(it.costPrice?.toString() ?: "")
                        etIpProductSalePrice.setText(it.salePrice?.toString() ?: "")
                    }
                } else {
                    Toast.makeText(this@InventoryProductEditActivity, "Error fetching product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Toast.makeText(this@InventoryProductEditActivity, "Network failure", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateProduct() {
        // Ensure that the product is not null before updating
        val currentProduct = product
        if (currentProduct != null) {
            // Get the updated values from the EditText fields or retain the existing values if no input was provided
            val updatedProduct = Product(
                _id = currentProduct._id, // Keep the existing product ID
                prodName = etIpProductName.text.toString().takeIf { it.isNotEmpty() } ?: currentProduct.prodName,
                prodDescription = etIpProductDecsription.text.toString().takeIf { it.isNotEmpty() } ?: currentProduct.prodDescription,
                prodCategory = etIpProductCategory.text.toString().takeIf { it.isNotEmpty() } ?: currentProduct.prodCategory,
                salePrice = etIpProductSalePrice.text.toString().toDoubleOrNull() ?: currentProduct.salePrice,
                costPrice = etIpProductCostPrice.text.toString().toDoubleOrNull() ?: currentProduct.costPrice,
                quantity = currentProduct.quantity, // Assuming you don't want to update quantity
                image_url = etIpProductImage.text.toString().takeIf { it.isNotEmpty() } ?: currentProduct.image_url
            )

            // Call the API to update the product
            apiService.updateSingleProduct(currentProduct._id, updatedProduct).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@InventoryProductEditActivity, "Product updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful update
                    } else {
                        Toast.makeText(this@InventoryProductEditActivity, "Failed to update product", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@InventoryProductEditActivity, "Network failure", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@InventoryProductEditActivity, "Product data is missing", Toast.LENGTH_SHORT).show()
        }
    }

    }

