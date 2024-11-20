package com.example.warehousemanagementsystem


data class Transaction(
    val transId: Int,
    //changed this to User type
    val user: User, //refers to the customer or admin user is who performed the transaction
    val transDateAndTime: String, //date of the transaction
    //val transTime: String, //time of transaction
    var products: Array<Product> = emptyArray(),

    val transTotal: Double = 0.0, // This could be calculated dynamically, no need to store it directly in the DB
    //removed "transProfit" for now as this shall be a calculated value derived from "salePrice" and "costPrice" of the products in the array, same for "transTotal"
    //should we also ass a field for transaction category i.e. supplyrequest or purchase
    //only admin would be able to do supply requests to add products.

) {
    // Function to calculate transTotal dynamically based on the products array
    fun calculateTransTotal(): Double {
        return products.sumOf { it.salePrice * (it.quantity ?: 0) }
    }
}
