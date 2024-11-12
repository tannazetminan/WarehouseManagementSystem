package com.example.warehousemanagementsystem

data class Transaction(
    val transId: Int,
    val userId: Int, //refers to the customer or admin user is who performed the transaction
    val transDate: String, //date of the transaction
    val transTime: String, //time of transaction
    var products: Array<Product> = emptyArray(),
    val transTotal: Double
)
