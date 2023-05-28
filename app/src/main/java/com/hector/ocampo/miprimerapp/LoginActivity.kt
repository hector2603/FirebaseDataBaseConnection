package com.hector.ocampo.miprimerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hector.ocampo.miprimerapp.databinding.LoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.filledButton.setOnClickListener { view ->
            auth.signInWithEmailAndPassword(
                binding.emailTI.editText?.text.toString(),
                binding.passwordTI.editText?.text.toString()
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(this::class.simpleName.toString(), "createUserWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(
                        baseContext,
                        "Authentication success.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    changeToHome()
                } else {
                    Log.w(
                        this::class.simpleName.toString(),
                        "createUserWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //updateUI(null)
                }
            }
        }

        binding.createAccountButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

    }

    fun changeToHome() {
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
    }


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            changeToHome()
            Toast.makeText(this, "ya se inició sesión", Toast.LENGTH_SHORT).show()
        }
    }
}