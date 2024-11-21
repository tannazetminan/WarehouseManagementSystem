package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSignInActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var loginButton: Button
    private lateinit var loginToRegisterButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usersignin)

        loginButton = findViewById(R.id.loginButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginToRegisterButton = findViewById(R.id.loginToRegisterButton)

        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(User( email = email, password = password))
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }


        loginToRegisterButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@UserSignInActivity, UserRegisterActivity::class.java))
        })
    }


    private fun loginUser(user: User) {
        apiService.loginUser(user).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val userType = loginResponse?.type
                    val userId = loginResponse?.user_id

                    Log.d("Login", "Response: $loginResponse")

                    if (userId != null) {
                        // Save user_id to SharedPreferences
                        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("user_id", userId)
                        editor.apply()

                        Toast.makeText(this@UserSignInActivity, "Login successful", Toast.LENGTH_SHORT).show()

                        // Redirect based on user type
                        when (userType) {
                            "admin" -> {
                                val intent = Intent(this@UserSignInActivity, AdminHomeActivity::class.java)
                                intent.putExtra("user_id", userId)
                                startActivity(intent)
                            }
                            "customer" -> {
                                val intent = Intent(this@UserSignInActivity, CustomerHomeActivity::class.java)
                                intent.putExtra("user_id", userId)
                                startActivity(intent)
                            }
                            else -> {
                                Toast.makeText(this@UserSignInActivity, "Unknown user type", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@UserSignInActivity, "Failed to retrieve user ID", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@UserSignInActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login", "Error: ${t.message}")
                Toast.makeText(this@UserSignInActivity, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
