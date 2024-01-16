package com.example.firebase

import android.os.Parcelable
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Pokemon(
    var id: String? = null,
    var nombre: String? = null,
    var tipo1: String? = null,
    var tipo2: String?= null,
    var poder: Int? = null,
    var fecha: String?=null,
    var valueRating: Float? = null,
    var icono: String? = null,

): Parcelable
//Serializable
