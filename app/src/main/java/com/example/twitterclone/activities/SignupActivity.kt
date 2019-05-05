package com.example.twitterclone.activities

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.kensuke.twitterclone.LoginActivity
import com.example.kensuke.twitterclone.R
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
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
        setContentView(R.layout.activity_signup)

        setTextChangeListener(userNameEditText, userNameTextInputLayout)
        setTextChangeListener(emailEditText, emailTextInputLayout)
        setTextChangeListener(passwordEditText, passwordTextInputLayout)

        // This doesn't allow a user to do any actions while login progress shows up
        signupProgressLayout.setOnTouchListener { view, motionEvent -> true }
    }

    fun onSignup(v: View) {
        var proceed = true

        if (userNameEditText.text.isNullOrEmpty()) {
            userNameTextInputLayout.error = "Username is required"
            userNameTextInputLayout.isErrorEnabled = true
            proceed = false
        }

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
            signupProgressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this@SignupActivity,
                            "Signup error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val email = emailEditText.text.toString()
                        val name = userNameEditText.text.toString()
                        val user = User(email, name, "", arrayListOf(), arrayListOf())
                        firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }

                    signupProgressLayout.visibility = View.GONE
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    signupProgressLayout.visibility = View.GONE
                }
        }
    }

    fun goToLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
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

    override fun onStart() {
        super.onStart()
        // add Listener to start new activity after signup process success
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        // remove Listener to start new activity after signup process success
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}
