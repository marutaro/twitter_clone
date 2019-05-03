package com.example.kensuke.twitterclone

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.twitterclone.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid

        // if user is not null, then start new activity
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setTextChangeListener(emailEditText, emailTextInputLayout)
        setTextChangeListener(passwordEditText, passwordTextInputLayout)

        // This doesn't allow a user to do any actions while login progress shows up
        loginProgressLayout.setOnTouchListener { view, motionEvent -> true }
    }

    fun setTextChangeListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // hide error message when user starts to input
                til.isErrorEnabled = false
            }

        })
    }

    // login process when clicking login button
    fun onLogin(v: View) {
        var proceed = true
        if (emailEditText.text.isNullOrEmpty()) {
            emailTextInputLayout.error = "Email is required"
            emailTextInputLayout.isErrorEnabled = true
            proceed = false
        }

        if (passwordEditText.text.isNullOrEmpty()) {
            passwordTextInputLayout.error = "Password is required"
            passwordTextInputLayout.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            loginProgressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        loginProgressLayout.visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            "Login error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    loginProgressLayout.visibility = View.GONE
                }
        }
    }

    fun goToSignup(v: View) {

    }

    override fun onStart() {
        super.onStart()
        // add Listener to start new activity after login success
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        // remove Listener to start new activity after login success
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}
