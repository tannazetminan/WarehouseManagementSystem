package com.example.warehousemanagementsystem

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddProductFormActivity : AppCompatActivity() {


    private lateinit var apiService: ApiService

    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var btnSubmitProduct: Button

    private lateinit var etProductName: EditText
    private lateinit var etProductDescription: EditText
    private lateinit var etProductCategory: EditText
    private lateinit var etProductSalePrice: EditText
    private lateinit var etProductCostPrice: EditText
    private lateinit var etProductQuantity: EditText

    private var selectedImageUri: Uri? = null
    private var imageUrl: String? = null // URL from uploaded image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product_form)

        imageView = findViewById(R.id.imageView)
        uploadButton = findViewById(R.id.uploadButton)
        btnSubmitProduct = findViewById(R.id.btnSubmitProduct)

        etProductName = findViewById(R.id.etProductName)
        etProductDescription = findViewById(R.id.etProductDescription)
        etProductCategory = findViewById(R.id.etProductCategory)
        etProductSalePrice = findViewById(R.id.etProductSalePrice)
        etProductCostPrice = findViewById(R.id.etProductCostPrice)
        etProductQuantity = findViewById(R.id.etProductQuantity)

        // Choose image from gallery
        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        // Submit product form
        btnSubmitProduct.setOnClickListener {
            if (validateForm()) {
                uploadImageAndCreateProduct()
            }
        }
    }

    // Handling image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    // Validate product form inputs
    private fun validateForm(): Boolean {
        return when {
            etProductName.text.isEmpty() -> {
                etProductName.error = "Please enter product name"
                false
            }
            etProductSalePrice.text.isEmpty() -> {
                etProductSalePrice.error = "Please enter sale price"
                false
            }
            etProductCostPrice.text.isEmpty() -> {
                etProductCostPrice.error = "Please enter cost price"
                false
            }
            etProductQuantity.text.isEmpty() -> {
                etProductCostPrice.error = "Please enter quantity"
                false
            }

            selectedImageUri == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    // Upload the image first and then create the product
    private fun uploadImageAndCreateProduct() {
        selectedImageUri?.let { uri ->
            val fileName = getFileName(uri) // Extract original file name
            val file = File(cacheDir, fileName) // Use original name
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

            // Initialize API service
            val baseUrl = readBaseUrl(this)
            apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

            apiService.uploadImage(body).enqueue(object : Callback<ImageResponse> {
                override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                    if (response.isSuccessful) {
                        imageUrl = response.body()?.image_url
                        createProduct()
                    } else {
                        Toast.makeText(this@AddProductFormActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                    Toast.makeText(this@AddProductFormActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    //get file name function
    private fun getFileName(uri: Uri): String {
        var fileName = ""
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    fileName = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                }
            }
        } else {
            fileName = uri.path?.substring(uri.path!!.lastIndexOf('/') + 1).orEmpty()
        }
        return fileName
    }

    // Create a product after image is uploaded
    private fun createProduct() {
        val product = Product(
            prodID = 0,  // Let the backend handle ID generation
            prodName = etProductName.text.toString(),
            prodDescription = etProductDescription.text.toString(),
            prodCategory = etProductCategory.text.toString(),
            salePrice = etProductSalePrice.text.toString().toDouble(),
            costPrice = etProductCostPrice.text.toString().toDouble(),
            quantity = etProductQuantity.text.toString().toInt(),
            image_url = imageUrl
        )
        // Initialize API service
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        apiService.createProduct(product).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddProductFormActivity, "Product created successfully", Toast.LENGTH_SHORT).show()
                    finish()  // Close activity
                } else {
                    Toast.makeText(this@AddProductFormActivity, "Failed to create product", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@AddProductFormActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}