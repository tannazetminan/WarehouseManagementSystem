package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class AdminHomeActivity : AppCompatActivity() {


    private lateinit var btnGoToUsers: Button
    private lateinit var btnGoToReports: Button


    private lateinit var topTenRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private lateinit var topTenInventoryAdapter: InventoryAdapter
    private var userId: String? = null

    private var productsList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)



        topTenRecyclerView = findViewById(R.id.top10RecyclerView)


        val btnGoToInventory: Button = findViewById(R.id.btnGoToInventory)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        // Set an OnClickListener to navigate to InventoryActivity
        btnGoToInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }
    }
}