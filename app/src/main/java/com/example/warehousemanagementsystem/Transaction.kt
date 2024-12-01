package com.example.warehousemanagementsystem

import com.google.gson.annotations.SerializedName


data class Transaction(
    @SerializedName("_id") val transId: String, // Adjusted to match JSON field
    @SerializedName("user_id") var userId: String, // Refers to the customer/user ID
    @SerializedName("trans_date") val transDate: String, // Date of the transaction
    @SerializedName("trans_time") val transTime: String, // Time of the transaction
    @SerializedName("products") var products: List<Product>, // List of products in the transaction
) {
    // Function to calculate transTotal dynamically based on the products array
    fun calculateTransTotal(): Double {
        return products.sumOf { it.salePrice * (it.quantity ?: 0) }
    }
}