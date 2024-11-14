package com.example.warehousemanagementsystem

data class Product(

    val prodID: Int,
    val prodName: String,
    val prodDescription: String? = null,
    val prodCategory: String? = null,
    val salePrice: Double,
    val costPrice: Double,
    val supplier: String? = null,
    val quantity: Int? = null,
    val image_url: String? =null,
)
