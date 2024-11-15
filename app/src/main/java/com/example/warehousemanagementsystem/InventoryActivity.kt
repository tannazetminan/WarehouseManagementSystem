package com.example.warehousemanagementsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class InventoryActivity : AppCompatActivity() {
    private lateinit var addProductButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)
        addProductButton = findViewById(R.id.btnAddNewProduct)
        addProductButton.setOnClickListener {
            val intent = Intent(this, AddProductFormActivity::class.java)
            startActivity(intent)
        }
    }
}