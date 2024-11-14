package com.example.warehousemanagementsystem

data class Transaction(
    val transId: Int,
    val userId: Int, //refers to the customer or admin user is who performed the transaction
    val transDate: String, //date of the transaction
    val transTime: String, //time of transaction
    var products: Array<Product> = emptyArray(),
    val transTotal: Double,
    val transProfit: Double? = null//should we track the profit per transaction?
// For order transactions this would be no profit
    //should we also ass a field for transaction category i.e. supplyrequest or purchase
    //only admin would be able to do supply requests to add products.
)
