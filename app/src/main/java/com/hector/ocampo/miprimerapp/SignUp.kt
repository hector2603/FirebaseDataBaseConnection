package com.hector.ocampo.miprimerapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hector.ocampo.miprimerapp.databinding.SignUpBinding

class SignUp : AppCompatActivity() {
    private lateinit var binding: SignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = SignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountButton.setOnClickListener {
            auth.createUserWithEmailAndPassword(
                binding.emailTI.editText?.text.toString(),
                binding.passwordTI.editText?.text.toString()
            ).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(binding.userNameTI.editText?.text.toString())
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(
                                    this::class.simpleName.toString(),
                                    "User profile updated."
                                )
                                NavUtils.navigateUpFromSameTask(this)
                            }
                        }
                } else {
                    Log.w(
                        this::class.simpleName.toString(),
                        "createUserWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.LoginViewButton.setOnClickListener { view ->
            NavUtils.navigateUpFromSameTask(this)
        }
    }

}