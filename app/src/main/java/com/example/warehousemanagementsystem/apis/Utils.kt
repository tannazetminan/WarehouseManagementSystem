package com.example.warehousemanagementsystem.apis

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun readBaseUrl(context: Context): String {
    val inputStream = context.assets.open("base_url.txt")
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readLine()
}
