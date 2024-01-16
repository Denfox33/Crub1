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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class CrearPokemon : AppCompatActivity(), CoroutineScope {

    private lateinit var nombre: EditText
    private lateinit var tipo1: EditText
    private lateinit var tipo2: EditText
    private lateinit var poder: EditText
    private lateinit var icono: ImageView
    private lateinit var crear: Button
    private lateinit var volver: Button
    private  lateinit var  estrellas:SimpleRatingBar

    private var url_poke: Uri? = null
    private lateinit var db_ref: DatabaseReference
    private lateinit var st_ref: StorageReference
    private lateinit var lista_Pokes: MutableList<Pokemon>

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_pokemon)


        val this_activity = this
        job = Job()

        nombre = findViewById(R.id.nombre)
        tipo1 = findViewById(R.id.tipo1)
        tipo2 = findViewById(R.id.tipo2)
        poder = findViewById(R.id.poder)
        icono = findViewById(R.id.icono)
        crear = findViewById(R.id.crear)
        volver = findViewById(R.id.volver)

        estrellas=findViewById(R.id.estrella)

        db_ref = FirebaseDatabase.getInstance().getReference()
        st_ref = FirebaseStorage.getInstance().getReference()
        lista_Pokes = Utilidades.obtenerListaPoke(db_ref)

        // Obtener la fecha actual y formatearla
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaActual = formatoFecha.format(Date())
        crear.setOnClickListener {
            val estrellasValor = estrellas.rating
            if (nombre.text.toString().trim().isEmpty() ||
                tipo1.text.toString().trim().isEmpty() ||
                tipo2.text.toString().trim().isEmpty() ||
                poder.text.toString().trim().isEmpty() ||

                estrellasValor < 0
            ) {
                Toast.makeText(
                    applicationContext, "Faltan datos en el " +
                            "formularion", Toast.LENGTH_SHORT
                ).show()

            } else if (url_poke == null) {
                Toast.makeText(
                    applicationContext, "Falta seleccionar el " +
                            "icono", Toast.LENGTH_SHORT
                ).show()
            } else if (Utilidades.existePokemon(lista_Pokes, nombre.text.toString().trim())) {
                Toast.makeText(applicationContext, "Ese Pokemon ya existe", Toast.LENGTH_SHORT)
                    .show()
            } else {

                var id_generado: String? = db_ref.child("Pokedex").child("Pokemons").push().key

                //GlobalScope(Dispatchers.IO)
                launch {
                    val url_poke_firebase =
                        Utilidades.guardarIcono(st_ref, id_generado!!, url_poke!!)

                    Utilidades.escribirPoke(
                        db_ref, id_generado!!,
                        nombre.text.toString().trim(),
                        tipo1.text.toString().trim(),
                        tipo2.text.toString().trim(),
                        poder.text.toString().trim().toInt(),
                        fechaActual.toString(),
                        estrellasValor,
                        url_poke_firebase
                    )
                    Utilidades.tostadaCorrutina(
                        this_activity,
                        applicationContext,
                        "Pokemon creado con exito"
                    )
                    val activity = Intent(applicationContext, MainActivity::class.java)
                    startActivity(activity)
                }

            }
        }
        volver.setOnClickListener {
            val activity = Intent(applicationContext, MainActivity::class.java)
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

    private val accesoGaleria = registerForActivityResult(ActivityResultContracts.GetContent())
    { uri: Uri ->
        if (uri != null) {
            url_poke = uri
            icono.setImageURI(uri)
        }

    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


}