package com.example.firebase


import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utilidades {
    companion object {

        fun existePokemon(Poke: List<Pokemon>, nombre: String): Boolean {
            return Poke.any { it.nombre!!.lowercase() == nombre.lowercase() }
        }

        val fechaCreacion = obtenerFechaActual()
        fun escribirPoke(
            db_ref: DatabaseReference,
            id: String,
            nombre: String,
            tipo1: String,
            tipo2: String,
            poder: Int,
            fecha:String,
            ratingBar: Float,
            url_firebase: String

        ) {
            // Asegúrate de que estás creando un objeto Pokemon correctamente
            val nuevoPokemon = Pokemon(id, nombre, tipo1, tipo2, poder,fecha, ratingBar,url_firebase,)
            db_ref.child("Pokedex").child("Pokemon").child(id).setValue(nuevoPokemon)
        }

        suspend fun guardarIcono(sto_ref: StorageReference, id: String, imagen: Uri): String {
            lateinit var url_poke_firebase: Uri

            url_poke_firebase = sto_ref.child("Pokedex").child("iconos").child(id)
                .putFile(imagen).await().storage.downloadUrl.await()

            return url_poke_firebase.toString()
        }

        fun obtenerListaPoke(db_ref: DatabaseReference): MutableList<Pokemon> {
            val lista = mutableListOf<Pokemon>()

            db_ref.child("Pokedex")
                .child("Pokemon")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            val pojo_poke = childSnapshot.getValue(Pokemon::class.java)
                            if (pojo_poke != null) {
                                lista.add(pojo_poke)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar errores de lectura de datos
                    }
                })

            return lista
        }


        fun tostadaCorrutina(activity: AppCompatActivity, contexto: Context, texto: String) {
            activity.runOnUiThread {
                Toast.makeText(
                    contexto,
                    texto,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun animacion_carga(contexto: Context): CircularProgressDrawable {
            val animacion = CircularProgressDrawable(contexto)
            animacion.strokeWidth = 5f
            animacion.centerRadius = 30f
            animacion.start()
            return animacion
        }

        val transicion = DrawableTransitionOptions.withCrossFade(500)

        fun opcionesGlide(context: Context): RequestOptions {
            val options = RequestOptions()
                .placeholder(animacion_carga(context))
                .fallback(R.drawable.poke_generico)
                .error(R.drawable.error_404)
            return options
        }
        fun obtenerFechaActual(): String {
            val fechaActual = LocalDate.now()
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return fechaActual.format(formato)
        }
    }
}
