package com.example.warehousemanagementsystem

import ProductsAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerHomeActivity : AppCompatActivity() {

    private lateinit var editProfileButton: Button
    private lateinit var cartButton: Button
    private lateinit var searchBar: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var priceSpinner: Spinner
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private lateinit var productsAdapter: ProductsAdapter
    private var userId: String? = null
    private var fullname: String? = null


    private var productsList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_home)

        editProfileButton = findViewById(R.id.editProfileButton)
        cartButton = findViewById(R.id.cartButton)
        searchBar = findViewById(R.id.searchBar)
        categorySpinner = findViewById(R.id.categorySpinner)
        priceSpinner = findViewById(R.id.priceSpinner)
        productsRecyclerView = findViewById(R.id.productsRecyclerView)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        setupRecyclerView()
        setupSpinners()
        fetchProducts()

        // Edit Profile Navigation
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Cart Navigation
        cartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Search Functionality
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
        })
    }

//    private fun setupRecyclerView() {
//        productsAdapter = ProductsAdapter(productsList, onAddToCart = { product ->
//            // Add product to cart
//            Toast.makeText(this, "${product.prodName} added to cart", Toast.LENGTH_SHORT).show()
//        }) { product ->
//            // Navigate to Product Detail Activity
//            val intent = Intent(this, ProductDetailActivity::class.java)
//            intent.putExtra("product_id", product.prodID)
//            startActivity(intent)
//        }
//        productsRecyclerView.layoutManager = LinearLayoutManager(this)
//        productsRecyclerView.adapter = productsAdapter
//    }

    private fun setupSpinners() {
        // Example categories and price ranges
        val categories = listOf("All Categories", "Electronics", "Clothing", "Books", "CDs")
        val priceRanges = listOf("All Prices", "Under $50", "$50 - $100", "Above $100")

        // Set up category spinner
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Set up price spinner
        val priceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priceRanges)
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        priceSpinner.adapter = priceAdapter

        // Spinner Listeners
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProductsByCategory(categories[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        priceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterProductsByPrice(priceRanges[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchProducts() {
        apiService.getAllProducts().enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                if (response.isSuccessful) {
                    productsList.clear()
                    productsList.addAll(response.body()!!)
                    productsAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CustomerHomeActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                Toast.makeText(this@CustomerHomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterProductsByCategory(category: String) {
        if (category == "All Categories") {
            productsAdapter.updateList(productsList)
        } else {
            val filteredList = productsList.filter { it.prodCategory == category }
            productsAdapter.updateList(filteredList)
        }
    }

    private fun filterProductsByPrice(priceRange: String) {
        val filteredList = when (priceRange) {
            "Under $50" -> productsList.filter { it.salePrice < 50 }
            "$50 - $100" -> productsList.filter { it.salePrice in 50.0..100.0 }
            "Above $100" -> productsList.filter { it.salePrice > 100 }
            else -> productsList
        }
        productsAdapter.updateList(filteredList)
    }

    private fun filterProducts(query: String) {
        val filteredList = productsList.filter {
            it.prodName.contains(query, ignoreCase = true) ||
                    it.prodCategory?.contains(query, ignoreCase = true) == true ||
                    it.salePrice.toString().contains(query)
        }
        productsAdapter.updateList(filteredList)
    }

    private fun setupRecyclerView() {
        productsAdapter = ProductsAdapter(productsList, onAddToCart = { product ->
            addToCart(product)
        }) { product ->
            // Navigate to Product Detail Activity
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.prodID)
            startActivity(intent)
        }
        productsRecyclerView.layoutManager = LinearLayoutManager(this)
        productsRecyclerView.adapter = productsAdapter
    }

    private fun addToCart(product: Product) {
        userId?.let { userId ->
            apiService.addCartItem(userId, product).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CustomerHomeActivity, "${product.prodName} added to cart", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CustomerHomeActivity, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@CustomerHomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}

