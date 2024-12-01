package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
//import com.anychart.charts.Pie
//import com.anychart.charts.Bar
import com.anychart.chart.common.dataentry.ValueDataEntry
import android.util.Log
import com.anychart.chart.common.dataentry.DataEntry

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var btnGoToUsers: Button
    private lateinit var btnGoToReports: Button
    private lateinit var topTenRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private var userId: String? = null
    private lateinit var anyChartView: AnyChartView
    private var productList = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        anyChartView = findViewById(R.id.any_chart_view)

        val btnGoToInventory: Button = findViewById(R.id.btnGoToInventory)
        val btnGoToReports: Button = findViewById(R.id.btnGoToReports)
        val btnGoToUsers: Button = findViewById(R.id.btnGoToUsers)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        fetchTopProducts()

        // Navigate to Users Activity
        btnGoToUsers.setOnClickListener {
            val intent = Intent(this, AdminUsersActivity::class.java)
            startActivity(intent)
        }

        // Set an OnClickListener to navigate to InventoryActivity
        btnGoToInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }

        btnGoToReports.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Call displayChart here to ensure the view is initialized
        displayChart()
    }
    private fun fetchTopProducts() {
        apiService.getAllTransactions().enqueue(object : Callback<List<Transaction>> {
            override fun onResponse(call: Call<List<Transaction>>, response: Response<List<Transaction>>) {
                if (response.isSuccessful) {
                    Log.d("AdminHomeActivity", "Response successful, body: ${response.body()}")
                    val transactionList = response.body()!!
                    calculateMostSoldProducts(transactionList)
                    Log.d("AdminHomeActivity", "Sessions added to the list, size: ${transactionList.size}")
                     displayChart()
                } else {
                    Log.e("AdminHomeActivity", "Response not successful, code: ${response.code()}")
                    Toast.makeText(this@AdminHomeActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
                Toast.makeText(this@AdminHomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateMostSoldProducts(transactionList: List<Transaction>) {
        productList.clear()

        transactionList.forEach { transaction ->
            transaction.products.forEach { product ->
                val currentQuantity = productList[product.prodName] ?: 0
                productList[product.prodName] = currentQuantity + (product.quantity ?: 0)
            }
        }

        // Log productList size and contents
        Log.d("AdminHomeActivity", "productList size: ${productList.size}")
        productList.forEach {
            Log.d("AdminHomeActivity", "Product: ${it.key}, Quantity: ${it.value}")
        }

        // Sorting and limiting to top 10 products
        productList = productList.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key to it.value }
            .toMap()
            .toMutableMap()
    }

    private fun displayChart() {
//        val data = mutableListOf<DataEntry>()
//        data.add(ValueDataEntry("Product A", 50))
//        data.add(ValueDataEntry("Product B", 30))
//        data.add(ValueDataEntry("Product C", 70))
//        data.add(ValueDataEntry("Product D", 90))
//        data.add(ValueDataEntry("Product E", 20))
//
//        // Log the data to check if it's being created correctly
//        Log.d("AdminHomeActivity", "Data for chart: $data")
//
//        val chart = AnyChart.bar()
//        chart.data(data)
//
//        // Ensure the chart is set to the view
//        anyChartView.setChart(chart)
//
//        // Log to confirm the chart is set
//        Log.d("AdminHomeActivity", "Chart set to AnyChartView")


        val bar = AnyChart.bar()

        // Use ValueDataEntry to represent the data
        val data = productList.map {
            ValueDataEntry(it.key, it.value)
        }

        // Log the data size and first few entries
        Log.d("AdminHomeActivity", "Data size for chart: ${data.size}")
        if (data.isNotEmpty()) {
            Log.d("AdminHomeActivity", "First data entry value: ${data.first().getValue("value")}")
        }

        // Check if the data is not empty before setting the chart
        if (data.isNotEmpty()) {
            bar.data(data)
            bar.title("Top 10 Products Sold")
            bar.labels().position("inside")
            Log.d("AdminHomeActivity", "First data entry value again: ${data.first().getValue("value")}")
            //anyChartView.clear()
            anyChartView.setChart(bar)
            Log.d("AdminHomeActivity", "Chart View Width: ${anyChartView.width}, Height: ${anyChartView.height}")
        } else {
            Toast.makeText(this@AdminHomeActivity, "No data available for chart", Toast.LENGTH_SHORT).show()
        }
    }
}