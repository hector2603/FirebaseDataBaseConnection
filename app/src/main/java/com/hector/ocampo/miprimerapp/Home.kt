package com.hector.ocampo.miprimerapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.hector.ocampo.miprimerapp.databinding.HomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding: HomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageRef: StorageReference

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

        database = Firebase.database.reference
        // obtener un valor de la base de datos una única vez
        database.child("user").child("hector").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }

        //Obtener un valor de la base de datos cada vez que cambie el valor
        val userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue<User>()
                //binding.nameTV.text = user?.nombre
                Log.i(
                    this@Home::class.simpleName.toString(),
                    "user: ${user?.nombre} ${user?.apellido} ${user?.email}"
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("firebase", "loadUset:onCancelled", databaseError.toException())
            }
        }
        val userReference = database.child("user").child("hector")
        userReference.addValueEventListener(userListener)// con este listener, si el dispositivos se queda sin conexión por un momento, y los datos cambiaron, cuando el dispositivo tenga internet nuevamente se actualiza nuevamente

        //Guardar Valores en la base de datos, cuando se guarda, o actualiza un valor en la base de datos también se tiene en cuenta que cuando no se tenga conexión a internet
        val newUser = User("Otra persona", "apellido", "email@email.com")
        database.child("user").child("otro").setValue(newUser)
            .addOnSuccessListener {
                Log.i(
                    this@Home::class.simpleName.toString(),
                    "user updated"
                )
            }
            .addOnFailureListener {
                Log.e(
                    this@Home::class.simpleName.toString(),
                    "user not updated"
                )
            }

        // Eliminar de la base de datos
        //database.child("user").child("otro").removeValue()

        ////////////////--------------------/////////////////////////////

        // obtener un valor único
        val db = Firebase.firestore
        val docRef = db.collection("user").whereEqualTo("apellido", "ocampo")
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                Log.d(
                    this@Home::class.simpleName.toString(),
                    "DocumentSnapshot data: ${document.documents.get(0).data}"
                )
            } else {
                Log.d(this@Home::class.simpleName.toString(), "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(this@Home::class.simpleName.toString(), "get failed with ", exception)
        }

        //Obtener un valor de la base de datos cada vez que cambie el valor

        db.collection("user").whereEqualTo("apellido", "ocampo")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(this@Home::class.simpleName.toString(), "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    doc.getString("nombre")?.let {
                        binding.nameTV.text = it
                    }
                }
                Log.d(this@Home::class.simpleName.toString(), "Se actualizó un usuario $value")
            }
        ///  Agregar nuevos elementos a la collection
        db.collection("user")
            .add(newUser)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    this@Home::class.simpleName.toString(),
                    "DocumentSnapshot written with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w(this@Home::class.simpleName.toString(), "Error adding document", e)
            }

        // Borrar datos
        //db.collection("user").document("LA_LLAVE_DEL_DOCUMETO").delete()

        //---------------------------------------------------------

        storageRef = Firebase.storage.reference

        binding.imagePicker.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            // here item is type of image
            galleryIntent.type = "image/*"
            // ActivityResultLauncher callback
            imagePickerActivityResult.launch(galleryIntent)
        }

    }


    private var imagePickerActivityResult: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                val imageUri: Uri? = result.data?.data

                // val fileName = imageUri?.pathSegments?.last()

                // extract the file name with extension
                val sd = getFileName(applicationContext, imageUri!!)

                // Upload Task with upload to directory 'file'
                // and name of the file remains same
                val uploadTask = storageRef.child("imagenes/$sd").putFile(imageUri)

                // On success, download the file URL and display it
                uploadTask.addOnSuccessListener {
                    // using glide library to display the image
                    val db = Firebase.firestore


                    storageRef.child("imagenes/$sd").downloadUrl.addOnSuccessListener {

                        var newProduct = Product("Televisor", it.toString())
                        db.collection("producto").add(newProduct)

                        Glide.with(this@Home)
                            .load(it)
                            .into(binding.imageView)

                        Log.e("Firebase", "download passed")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Failed in downloading")
                    }
                }.addOnFailureListener {
                    Log.e("Firebase", "Image Upload fail")
                }
            }
        }


    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        var columnNumber = if(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)!=-1) cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) else 0
                        return cursor.getString(columnNumber)
                    }
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }
}