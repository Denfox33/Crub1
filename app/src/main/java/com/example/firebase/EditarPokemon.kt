package com.example.firebase

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class EditarPokemon : AppCompatActivity(), CoroutineScope {

    private lateinit var nombre: EditText
    private lateinit var tipo1: EditText
    private lateinit var tipo2: EditText
    private lateinit var poder: EditText
    private lateinit var icono: ImageView
    private lateinit var modificar: Button
    private lateinit var volver: Button
    private var url_poke: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var pojo_poke: Pokemon
    private lateinit var lista_pokes: MutableList<Pokemon>
    private lateinit var estrellas: SimpleRatingBar
    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_pokemon)

        val this_activity = this
        job = Job()

        pojo_poke = intent.getParcelableExtra("pokemon") ?: Pokemon()

        nombre = findViewById(R.id.nombre)
        tipo1 = findViewById(R.id.tipo1)
        tipo2 = findViewById(R.id.tipo2)
        poder = findViewById(R.id.poder)
        icono = findViewById(R.id.icono)
        modificar = findViewById(R.id.modificar)
        volver = findViewById(R.id.volver)
        estrellas = findViewById(R.id.estrella)

        nombre.setText(pojo_poke.nombre)
        tipo1.setText(pojo_poke.tipo1)
        tipo2.setText(pojo_poke.tipo2)
        poder.setText(pojo_poke.poder.toString())
        Glide.with(applicationContext)
            .load(pojo_poke.icono)
            .apply(Utilidades.opcionesGlide(applicationContext))
            .transition(Utilidades.transicion)
            .into(icono)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()

        lista_pokes = Utilidades.obtenerListaPoke(db_ref)

        modificar.setOnClickListener {
            val estrellasValor = estrellas.rating
            if (nombre.text.toString().trim().isEmpty() ||
                tipo1.text.toString().trim().isEmpty() ||
                tipo2.text.toString().trim().isEmpty() ||
                poder.text.toString().trim().isEmpty() ||
                estrellasValor < 0
            ) {
                Toast.makeText(
                    applicationContext, "Faltan datos en el " +
                            "formulario", Toast.LENGTH_SHORT
                ).show()

            } else {

                var url_poke_firebase: String = pojo_poke.icono!!

                launch {
                    if (url_poke != null) {
                        url_poke_firebase =
                            Utilidades.guardarIcono(st_ref, pojo_poke.id!!, url_poke!!)
                    }

                    Utilidades.escribirPoke(
                        db_ref, pojo_poke.id!!,
                        nombre.text.toString().trim(),
                        tipo1.text.toString().trim(),
                        tipo2.text.toString().trim(),
                        poder.text.toString().trim().toInt(),
                        pojo_poke.fecha!!,
                        estrellasValor,
                        url_poke_firebase
                    )

                    Utilidades.tostadaCorrutina(
                        this_activity,
                        applicationContext,
                        "Pokemon modificado con éxito"
                    )

                    val activity = Intent(applicationContext, MainActivity::class.java)
                    startActivity(activity)
                }
            }
        }

        volver.setOnClickListener {
            val activity = Intent(applicationContext, VerPokemon::class.java)
            startActivity(activity)
        }

        icono.setOnClickListener {
            accesoGaleria.launch("image/*")
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private val accesoGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                url_poke = uri
                icono.setImageURI(uri)
            }
        }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
}
