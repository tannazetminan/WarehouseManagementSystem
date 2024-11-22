package com.example.warehousemanagementsystem

data class Product(

    val _id: String, // This maps to the MongoDB `_id`
    val prodName: String,
    val prodDescription: String? = null,
    val prodCategory: String? = null,
    val salePrice: Double,
    val costPrice: Double,
    //val supplier: String? = null, //we do not need supplier as this is not necessary for our case as per previous discussion or do we?
    var quantity: Int? = null,//changed as this is mutable
    val image_url: String? =null,
)
