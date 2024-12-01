package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var btnGoToUsers: Button
    private lateinit var btnGoToReports: Button
    private lateinit var topTenRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private var userId: String? = null
    private lateinit var chartView: BarChart
    private var productList = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        chartView = findViewById(R.id.chart_view)

        val btnGoToInventory: Button = findViewById(R.id.btnGoToInventory)
        val btnGoToReports: Button = findViewById(R.id.btnGoToReports)
        val btnGoToUsers: Button = findViewById(R.id.btnGoToUsers)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        fetchTopProducts()  // Fetch products from API

//        // Use a background thread for heavy work, e.g., network call
//        Thread {
//            fetchTopProducts()  // Fetch products from API
//            runOnUiThread {
//                displayChart()  // Ensure UI updates are done on the main thread
//            }
//        }.start()

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

        // Sorting and limiting to top 5 products
        productList = productList.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }
            .toMap()
            .toMutableMap()
    }

    private fun displayChart() {
        // Log the productList before creating the chart
        Log.d("AdminHomeActivity", "productList: $productList")

        // Create BarEntry list for Y-axis values
        val entries = productList.entries.mapIndexed { index, entry ->
            val barEntry = BarEntry(index.toFloat(), entry.value.toFloat())
            Log.d("AdminHomeActivity", "BarEntry - Index: $index, Value: ${entry.value}")
            barEntry
        }

        // Set up labels for X-axis
        val labels = productList.keys.toList()
        Log.d("AdminHomeActivity", "Labels for X-axis: $labels")

        // Determine the range of values for color scaling
        val minValue = productList.values.minOrNull()?.toFloat() ?: 0f
        val maxValue = productList.values.maxOrNull()?.toFloat() ?: 1f // Avoid division by zero
        Log.d("AdminHomeActivity", "Value range: Min=$minValue, Max=$maxValue")
        // Prepare the dataset
        val dataSet = BarDataSet(entries, "Quantity Sold").apply {
            // Set colors dynamically based on the value
            colors = entries.map { entry ->
                getColorForValue(entry.y, minValue, maxValue)
            }
            valueTextSize = 14f // Customize text size
            Log.d("AdminHomeActivity", "BarDataSet created with ${entries.size} entries.")
        }

//        // Prepare the dataset
//        val dataSet = BarDataSet(entries, "Quantity Sold").apply {
//            color = resources.getColor(R.color.primaryColor, null) // Customize color
//            valueTextSize = 14f // Customize text size
//            Log.d("AdminHomeActivity", "BarDataSet created with ${entries.size} entries.")
//        }

        // Attach data to the BarData
        val barData = BarData(dataSet).apply {
            barWidth = 0.9f // Optional: adjust bar width
            Log.d("AdminHomeActivity", "BarData created with bar width: $barWidth")
        }

        // Configure the BarChart
        chartView.apply {
            data = barData
            description.isEnabled = false // Disable description label
            setFitBars(true) // Adjust bars to fit

            // Customize X-axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.TOP_INSIDE
                granularity = 1f
                isGranularityEnabled = true
                setLabelRotationAngle(90f) // Rotate labels to vertical
                yOffset = 100f // Adjust the Y offset to move labels lower (increase the value as needed)
                Log.d("AdminHomeActivity", "X-axis configured with granularity: $granularity and labels: $labels")
            }

            // Customize Y-axis
            axisLeft.axisMinimum = 0f // Y-axis starts at 0
            axisRight.isEnabled = false // Disable right Y-axis
            Log.d("AdminHomeActivity", "Y-axis configured with minimum: ${axisLeft.axisMinimum}")

            // Refresh the chart
            invalidate()
            Log.d("AdminHomeActivity", "BarChart refreshed and displayed.")
        }
    }
    /**
     * Generate a color based on the value's position in the range [minValue, maxValue].
     */
    private fun getColorForValue(value: Float, minValue: Float, maxValue: Float): Int {
        val ratio = (value - minValue) / (maxValue - minValue) // Normalize value between 0 and 1
        return when {
            ratio >= 0.75 -> resources.getColor(R.color.green, null) // High value (Green)
            ratio >= 0.5 -> resources.getColor(R.color.yellow, null) // Medium value (Yellow)
            else -> resources.getColor(R.color.red, null) // Low value (Red)
        }
    }
}