package com.hector.ocampo.miprimerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hector.ocampo.miprimerapp.databinding.HomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding: HomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = HomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nameTV.text = auth.currentUser?.displayName ?: "Hola NN"

        binding.logOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}