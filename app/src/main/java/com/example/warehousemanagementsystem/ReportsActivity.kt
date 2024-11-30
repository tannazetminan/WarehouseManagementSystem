//package com.example.warehousemanagementsystem
//
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.Spinner
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//class ReportsActivity : AppCompatActivity() {
//
//    private lateinit var transactionRecyclerView: RecyclerView
//    private lateinit var transactionDateSpinner: Spinner
//    private lateinit var transactionFilterButton: Button
//    private lateinit var apiService: ApiService
//    private lateinit var transactionAdapter: TransactionAdapter
//    private var transactionList = mutableListOf<Transaction>()
//    private lateinit var transactionReportTextView: TextView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_reports)
//
//        transactionRecyclerView = findViewById(R.id.transactionRecyclerView)
//        transactionDateSpinner = findViewById(R.id.transactionsDateSpinner)
//        transactionFilterButton = findViewById(R.id.filterTransactionsButton)
//        transactionReportTextView = findViewById(R.id.txvTransactionReportSummary)
//
//        // Initialize API service
//        val baseUrl = readBaseUrl(this)
//        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)
//
//        setUpTransactionRecyclerView()
//
//        setUpDateSpinner()
//        transactionFilterButton.setOnClickListener {
//            val selectedDate = transactionDateSpinner.selectedItem.toString()
//            filterTransactionsByDate(selectedDate)
//        }
//
//        // Fetch all transactions initially
//        fetchTransactions()
//
//    }
//
//    private fun fetchTransactions() {
//        apiService.getAllTransactions().enqueue(object : Callback<List<Transaction>> {
//            override fun onResponse(call: Call<List<Transaction>>, response: Response<List<Transaction>>) {
//                if (response.isSuccessful) {
//                    transactionList.clear()
//                    transactionList.addAll(response.body()!!)
//                    transactionAdapter.notifyDataSetChanged()
//                } else {
//                    Toast.makeText(this@ReportsActivity, "Failed to load transactions", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {
//                Toast.makeText(this@ReportsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//
//    private fun setUpTransactionRecyclerView(){
//
//        transactionAdapter = TransactionAdapter(transactionList, this) { transaction ->
//
//            val intent = Intent(this, TransactionDetailActivity::class.java)
//            intent.putExtra("transaction_id", transaction.transId)
//            startActivity(intent)
//        }
//        transactionRecyclerView.layoutManager = LinearLayoutManager(this)
//        transactionRecyclerView.adapter = transactionAdapter
//    }
//
//    private fun setUpDateSpinner() {
//        val dateFilters = listOf("All Time", "Last Week", "Last Month", "Custom Date")
//        val dateFilterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dateFilters)
//        dateFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        transactionDateSpinner.adapter = dateFilterAdapter
//    }
//
//    private fun filterTransactionsByDate(selectedDate: String) {
//        val filteredTransactions = when (selectedDate) {
//            "Last Week" -> transactionList.filter {
//                it.transDateAndTime.toLocalDate()?.isLastWeek() == true
//            }
//            "Last Month" -> transactionList.filter {
//                it.transDateAndTime.toLocalDate()?.isLastMonth() == true
//            }
//            "Custom Date" -> {
//                // Implement custom date filtering logic (open date picker or other mechanism)
//                transactionList
//            }
//            else -> transactionList
//        }
//
//        // Update RecyclerView with filtered transactions
//        transactionAdapter.updateList(filteredTransactions)
//
//        // Generate and display the financial report for the filtered transactions
//        val report = generateFinancialReport(filteredTransactions)
//        transactionReportTextView.text = report
//        transactionReportTextView.visibility = View.VISIBLE
//
//    }
//
//    private fun generateFinancialReport(transactionsList: List<Transaction>): String {
//        val totalSales = calculateTotalSales(transactionList)
//        val totalProfit = calculateTotalProfit(transactionList)
//        val transactionCount = transactionList.size
//        val avgTransactionValue = if (transactionCount > 0) totalSales/transactionCount else 0.0
//        val mostSoldProducts = calculateMostSoldProducts(transactionList)
//
//        return """
//            Financial Report:
//            -----------------
//            Total Sales: $$totalSales
//            Total Profit: $$totalProfit
//            Number of Transactions: $transactionCount
//            Average Transaction Value: $$avgTransactionValue
//            Most Sold Products: $mostSoldProducts
//        """.trimIndent()
//    }
//
//    private fun calculateMostSoldProducts(transactionList: List<Transaction>): String {
//        val productQuantities = mutableMapOf<String, Int>()
//
//        transactionList.forEach { transaction ->
//            transaction.products.forEach { product ->
//                val currentQuantity = productQuantities[product.prodName] ?: 0
//                productQuantities[product.prodName] = currentQuantity + (product.quantity ?: 0)
//            }
//        }
//
//        val mostSoldProduct = productQuantities.maxByOrNull { it.value }
//        return mostSoldProduct?.key ?: "No products sold"
//    }
//
//    private fun calculateTotalSales(transactionList: List<Transaction>): Double {
//        return transactionList.sumOf { it.transTotal }
//    }
//
//    private fun calculateTotalProfit(transactionList: List<Transaction>): Double {
//        return transactionList.sumOf { transaction ->
//            transaction.products.sumOf { product ->
//                (product.salePrice - product.costPrice) * (product.quantity ?: 0)
//            }
//        }
//    }
//    fun String.toLocalDate(): LocalDate? {
//        return try {
//            LocalDate.parse(this, DateTimeFormatter.ISO_DATE) // Assuming format is "yyyy-MM-dd"
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    fun LocalDate.isLastWeek(): Boolean {
//        val today = LocalDate.now()
//        val oneWeekAgo = today.minusWeeks(1)
//        return this.isAfter(oneWeekAgo) && this.isBefore(today)
//    }
//
//    fun LocalDate.isLastMonth(): Boolean {
//        val today = LocalDate.now()
//        val oneMonthAgo = today.minusMonths(1)
//        return this.isAfter(oneMonthAgo) && this.isBefore(today)
//    }
//}
//
//
