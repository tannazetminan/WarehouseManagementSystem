package com.example.warehousemanagementsystem

data class Transaction(
    val transId: Int,
    val userId: Int, //refers to the customer or admin user is who performed the transaction
    val transDate: String, //date of the transaction
    //how to handle items with prod it, name, quantity and price per item?
    val transTotal: Double
)
