package com.example.warehousemanagementsystem

import android.content.Intent
import android.os.Bundle
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
                loginUser(User(email = email, password = password))
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
                    val userType = response.body()?.type
                    if (userType == "admin") {
                        startActivity(Intent(this@UserSignInActivity, AdminHomeActivity::class.java))
                    } else {
                        startActivity(Intent(this@UserSignInActivity, CustomerHomeActivity::class.java))
                    }
                    finish()  // Prevent back navigation to the login screen
                } else {
                    Toast.makeText(this@UserSignInActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@UserSignInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
