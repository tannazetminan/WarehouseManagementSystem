package com.example.warehousemanagementsystem

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class TransactionDetailActivity : AppCompatActivity() {


    private lateinit var apiService: ApiService

    private var transactionId: String ? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_detail)


        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        transactionId = intent.getStringExtra("transaction_id")

        if (transactionId != null) {
            getTransactionById(transactionId!!)
        }
    }

    private fun getTransactionById(transactionId: String) {
        apiService.getTransactionById(transactionId).enqueue(object : Callback<Transaction> {
            override fun onResponse(call: Call<Transaction>, response: Response<Transaction>) {
                if (response.isSuccessful) {
                    val transaction = response.body()
                    transaction?.let {
                        // Display product details
                        findViewById<TextView>(R.id.txvTransactionDetailId).text = it.transId
                        findViewById<TextView>(R.id.txvTransactionDetailUserId).text = it.userId
                        findViewById<TextView>(R.id.txvTransactionDetailDate).text = it.transDate
                        findViewById<TextView>(R.id.txvTransactionDetailTime).text = it.transTime
                        val total=it.calculateTransTotal()
                        val totalString =  String.format(Locale.US, "$ %.2f", total)
                        findViewById<TextView>(R.id.txvTransactionDetailTotal).text = totalString
                        val products = it.products
                        if (products.isNotEmpty()) {
                            // Create a string to display product details
                            val productDetails = StringBuilder()
                            for (product in products) {

                                val salePriceString =  String.format(Locale.US, "Price: $ %.2f", product.salePrice)
                                productDetails.append("Product Name: ${product.prodName}\n")
                                productDetails.append("Description: ${product.prodDescription}\n")
                                productDetails.append("Category: ${product.prodCategory}\n")
                                productDetails.append("Price: $${salePriceString}\n")
                                productDetails.append("Quantity: ${product.quantity}\n\n")
                            }

                            // Append the formatted string to the TextView
                            findViewById<TextView>(R.id.txvTransactionDetailProducts).text = productDetails.toString()

                        }

                    }
                } else {

                    Toast.makeText(this@TransactionDetailActivity, "Error fetching transaction", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Transaction>, t: Throwable) {

                Toast.makeText(this@TransactionDetailActivity, "Network failure", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


