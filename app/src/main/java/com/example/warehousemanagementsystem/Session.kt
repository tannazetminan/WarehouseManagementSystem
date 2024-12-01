package com.example.warehousemanagementsystem

data class Session(
    val _id: String,
    val username: String,
    val counter: Int,
    val start_time: String,
    val end_time: String?
)

