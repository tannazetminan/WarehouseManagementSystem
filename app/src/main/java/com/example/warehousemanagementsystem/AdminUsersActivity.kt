package com.example.warehousemanagementsystem

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class AdminUsersActivity : AppCompatActivity() {

    private lateinit var sessionRecyclerView: RecyclerView
    private lateinit var apiService: ApiService
    private lateinit var sessionAdapter: SessionAdapter
    private var sessionsList = mutableListOf<Session>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)

        sessionRecyclerView = findViewById(R.id.sessionRecyclerView)
        sessionRecyclerView.layoutManager = LinearLayoutManager(this)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        fetchSessions()

        // Adapter for RecyclerView
        sessionAdapter = SessionAdapter(sessionsList)
        sessionRecyclerView.adapter = sessionAdapter


    }

    private fun fetchSessions() {
        Log.d("AdminUsersActivity", "fetching")

        apiService.getAllSessions().enqueue(object : Callback<List<Session>> {
            override fun onResponse(call: Call<List<Session>>, response: Response<List<Session>>) {
                Log.d("AdminUsersActivity", "API Response received")
                if (response.isSuccessful) {
                    Log.d("AdminUsersActivity", "Response successful, body: ${response.body()}")
                    sessionsList.clear()
                    sessionsList.addAll(response.body()!!)
                    Log.d("AdminUsersActivity", "Sessions added to the list, size: ${sessionsList.size}")
                    sessionAdapter.notifyDataSetChanged()
                } else {
                    // Handle error
                    Log.e("AdminUsersActivity", "Response not successful, code: ${response.code()}")
                    Toast.makeText(this@AdminUsersActivity, "Failed to fetch sessions", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Session>>, t: Throwable) {
                // Handle failure
                Log.e("AdminUsersActivity", "API call failed: ${t.message}")
                t.printStackTrace()  // This can give additional insights into the root cause
                Toast.makeText(this@AdminUsersActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
