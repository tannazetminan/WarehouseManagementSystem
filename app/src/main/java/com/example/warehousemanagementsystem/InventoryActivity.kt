package com.example.warehousemanagementsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventoryActivity : AppCompatActivity() {


    private lateinit var inventoryCategorySpinner: Spinner
    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private lateinit var inventoryAdapter: InventoryAdapter
    private var userId: String? = null

    private var productsList = mutableListOf<Product>()

    private lateinit var addProductButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        inventoryCategorySpinner= findViewById(R.id.spinnerInventoryCategories)
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView)
        addProductButton = findViewById(R.id.btnAddNewProduct)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)


        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        setUpInventoryRecyclerView()
        setUpCategorySpinner()
        fetchProducts()
      //  fetchUserProfile()

        addProductButton.setOnClickListener {
//            val intent = Intent(this, AddProductFormActivity::class.java)
            val intent = Intent(this, AddproductForm2Activity::class.java)
         startActivity(intent)
        }
    }

    private fun fetchProducts() {
        apiService.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    productsList.clear()
                    productsList.addAll(response.body()!!)
                    inventoryAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@InventoryActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@InventoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpCategorySpinner() {

        val categories = listOf("All Categories", "Electronics", "Clothing", "Books", "CDs")



        val inventoryCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        inventoryCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        inventoryCategorySpinner.adapter = inventoryCategoryAdapter


        inventoryCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProductsByCategory(categories[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun filterProductsByCategory(category: String) {
        if (category == "All Categories") {
            Log.d("InventoryActivity", "Product list size: ${productsList.size}")
            inventoryAdapter.updateList(productsList)
        } else {
            val filteredList = productsList.filter { it.prodCategory == category }
            inventoryAdapter.updateList(filteredList)
        }
    }

    private fun setUpInventoryRecyclerView() {
        inventoryAdapter = InventoryAdapter(
            productList = productsList,
            onUpdateQuantity = { product, newQuantity ->
                increaseInventory(product, newQuantity)
            },
            onItemClick = { product ->
                // Navigate to Product Detail Activity
                val intent = Intent(this, InventoryProductDetailActivity::class.java)
                intent.putExtra("product_id", product._id)
                startActivity(intent)
            }
        )
        inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        inventoryRecyclerView.adapter = inventoryAdapter
    }


    //updated new logic
    private fun increaseInventory(product: Product, newQuantity: Int) {
        val quantityUpdatePayload = mapOf("quantity" to newQuantity.toString())

        // Make the API call to update the product's quantity on the server
        apiService.updateProductQuantity(product._id, quantityUpdatePayload).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Update the local product quantity to the new value
                    product.quantity = newQuantity

                    val index = productsList.indexOfFirst { it._id == product._id }
                    if (index != -1) {
                        productsList[index] = product
                        inventoryAdapter.notifyItemChanged(index)
                        Toast.makeText(this@InventoryActivity, "Quantity updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@InventoryActivity, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@InventoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}