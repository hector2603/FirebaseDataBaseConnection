package com.hector.ocampo.miprimerapp

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val nombre:String? = null,
    val apellido: String? = null,
    val email:String? = null
)
