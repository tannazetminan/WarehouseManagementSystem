package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class UserEditProfileActivity : AppCompatActivity() {

    private lateinit var fullnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var apiService: ApiService
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        fullnameEditText = findViewById(R.id.fullnameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        saveProfileButton = findViewById(R.id.saveProfileButton)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)


        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            fetchUserProfile()
        } else {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        saveProfileButton.setOnClickListener {
            val updatedProfile = UserProfile(
                fullname = fullnameEditText.text.toString(),
                email = emailEditText.text.toString(),
                phone = phoneEditText.text.toString()
            )
            updateUserProfile(updatedProfile)
        }
    }

    private fun fetchUserProfile() {
        userId?.let {
            apiService.getUserProfile(it).enqueue(object : Callback<UserProfile> {
                override fun onResponse(call: Call<UserProfile>, response: Response<UserProfile>) {
                    if (response.isSuccessful) {
                        val profile = response.body()
                        fullnameEditText.setText(profile?.fullname)
                        emailEditText.setText(profile?.email)
                        phoneEditText.setText(profile?.phone)
                    } else {
                        Toast.makeText(this@UserEditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(this@UserEditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUserProfile(profile: UserProfile) {
        userId?.let {
            apiService.updateUserProfile(it, profile).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@UserEditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@UserEditProfileActivity, UserProfileActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@UserEditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@UserEditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
