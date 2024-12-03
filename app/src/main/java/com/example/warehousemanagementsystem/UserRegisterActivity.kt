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

class UserRegisterActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var registerButton: Button
    private lateinit var signinButton: Button
    private lateinit var fullNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var adminCodeEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userregister)

        registerButton = findViewById(R.id.registerButton)
        signinButton = findViewById(R.id.signinButton)
        fullNameEditText = findViewById(R.id.fullnameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        adminCodeEditText = findViewById(R.id.adminCodeEditText)


        // Initialize API service
        val baseUrl = readBaseUrl(this)
        apiService = RetrofitClient.getRetrofitInstance(baseUrl).create(ApiService::class.java)

        registerButton.setOnClickListener {
            val fullname = fullNameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val adminCode = adminCodeEditText.text.toString().trim()


            if (validateInput(fullname, phone, email, password)) {
                // Determine user type based on admin code
                val userType = if (adminCode == "CSIS4280") "admin" else "customer"
                val user = User(fullname, phone, email, password, userType)
                registerUser(user)
            }
        }

        signinButton.setOnClickListener {
            startActivity(Intent(this@UserRegisterActivity, UserSignInActivity::class.java))

        }
    }

    private fun validateInput(fullname: String, phone: String, email: String,  password: String): Boolean {
        if (fullname.isEmpty()) {
            fullNameEditText.error = "Full Name is required"
            return false
        }

        if (phone.isEmpty() || !android.util.Patterns.PHONE.matcher(phone).matches()) {
            phoneEditText.error = "Enter a valid phone number"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters long"
            return false
        }

        return true
    }

    private fun registerUser(user: User) {
        apiService.registerUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UserRegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    // Redirect to the SignInActivity
                    startActivity(Intent(this@UserRegisterActivity, UserSignInActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@UserRegisterActivity, "Registration Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UserRegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
