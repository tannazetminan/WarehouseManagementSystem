package com.example.warehousemanagementsystem

import android.content.Intent
import android.net.Uri
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

class AddproductForm2Activity : AppCompatActivity() {



    private lateinit var apiService: ApiService


    private lateinit var btnSubmitAddProduct: Button

    private lateinit var etAddImage: EditText
    private lateinit var etAddProductName: EditText
    private lateinit var etAddProductDescription: EditText
    private lateinit var etAddProductCategory: EditText
    private lateinit var etAddProductSalePrice: EditText
    private lateinit var etAddProductCostPrice: EditText
    private lateinit var etAddProductQuantity: EditText

    private var selectedImageUri: Uri? = null
    private var imageUrl: String? = null // URL from uploaded image
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_addproduct_form2)




        btnSubmitAddProduct = findViewById(R.id.btnSubmitAddProduct)

        etAddImage = findViewById(R.id.etAddPImageURL)
        etAddProductName = findViewById(R.id.etAddProductName)
        etAddProductDescription = findViewById(R.id.etAddProductDescription)
        etAddProductCategory = findViewById(R.id.etAddProductCategory)
        etAddProductSalePrice = findViewById(R.id.etAddProductSalePrice)
        etAddProductCostPrice = findViewById(R.id.etAddProductCostPrice)
        etAddProductQuantity = findViewById(R.id.etAddProductQuantity)



        // Submit product form
        btnSubmitAddProduct.setOnClickListener {
            if (validateForm()) {
                createProduct()
                val intent = Intent(this, InventoryActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun validateForm(): Boolean {
        return when {

            etAddImage.text.isEmpty() -> {
                etAddImage.error = "Please enter product name"
                false
            }
            etAddProductName.text.isEmpty() -> {
                etAddProductName.error = "Please enter product name"
                false
            }
            etAddProductSalePrice.text.isEmpty() -> {
                etAddProductSalePrice.error = "Please enter sale price"
                false
            }
            etAddProductCostPrice.text.isEmpty() -> {
                etAddProductCostPrice.error = "Please enter cost price"
                false
            }
            etAddProductQuantity.text.isEmpty() -> {
                etAddProductCostPrice.error = "Please enter quantity"
                false
            }


            else -> true
        }
    }

    private fun createProduct() {
        val product = Product(
            _id= null.toString(),
            prodName = etAddProductName.text.toString(),
            prodDescription = etAddProductDescription.text.toString(),
            prodCategory = etAddProductCategory.text.toString(),
            salePrice = etAddProductSalePrice.text.toString().toDouble(),
            costPrice = etAddProductCostPrice.text.toString().toDouble(),
            quantity = etAddProductQuantity.text.toString().toInt(),
            image_url = etAddImage.text.toString()
        )
        // Initialize API service
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        apiService.createProduct(product).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddproductForm2Activity, "Product created successfully", Toast.LENGTH_SHORT).show()
                    finish()  // Close activity
                } else {
                    Toast.makeText(this@AddproductForm2Activity, "Failed to create product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddproductForm2Activity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}