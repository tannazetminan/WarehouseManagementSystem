package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.warehousemanagementsystem.apis.ApiService
import com.example.warehousemanagementsystem.apis.RetrofitClient
import com.example.warehousemanagementsystem.apis.readBaseUrl
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileActivity : AppCompatActivity() {

    private lateinit var fullnameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var backImg: ImageView
    private lateinit var logoutButton: Button
    private lateinit var apiService: ApiService
    private var userId: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        fullnameTextView = findViewById(R.id.fullnameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        logoutButton = findViewById(R.id.logoutButton)
        backImg = findViewById(R.id.imgViewBackMain)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        fetchUserProfile()

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, UserEditProfileActivity::class.java))
        }

        logoutButton.setOnClickListener {
            logout()
        }

        backImg.setOnClickListener{
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val userType = sharedPreferences.getString("userType", null)
            if (userType == "admin") {
                startActivity(Intent(this@UserProfileActivity, AdminHomeActivity::class.java))
            } else {
                startActivity(Intent(this@UserProfileActivity, CustomerHomeActivity::class.java))
            }
        }
    }

    private fun fetchUserProfile() {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            apiService.getUserProfile(userId).enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val profile = response.body()
                        fullnameTextView.text = "Full Name: ${profile?.fullname}"
                        emailTextView.text = "Email: ${profile?.email}"
                        phoneTextView.text = "Phone: ${profile?.phone}"

                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    // Logout logic: clear SharedPreferences and redirect to the sign-in activity
    private fun logout() {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // Clear all saved preferences
        editor.apply()

        // Redirect to sign-in activity
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, UserSignInActivity::class.java)
        startActivity(intent)
        finish()  // Finish current activity to prevent user from returning to the profile screen
    }
}
