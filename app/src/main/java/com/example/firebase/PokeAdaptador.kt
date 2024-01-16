package com.example.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iarcuschin.simpleratingbar.SimpleRatingBar

class PokeAdaptador(private val lista_club: MutableList<Pokemon>):
    RecyclerView.Adapter<PokeAdaptador.PokeViewHolder>(), Filterable {
    private lateinit var contexto: Context
    private var lista_filtrada = lista_club

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokeViewHolder {
        val vista_item =
            LayoutInflater.from(parent.context).inflate(R.layout.item_poke, parent, false)
        contexto = parent.context
        return PokeViewHolder(vista_item)
    }

    override fun onBindViewHolder(holder: PokeViewHolder, position: Int) {
        val item_actual = lista_filtrada[position]

        holder.nombre.text = item_actual.nombre
        holder.tipo1.text = item_actual.tipo1
        holder.tipo2.text = item_actual.tipo2
        holder.poder.text = item_actual.poder.toString()
        holder.rating.rating = item_actual.valueRating!!
        holder.fecha.text=item_actual.fecha


        val URL: String? = when (item_actual.icono) {
            "" -> null
            else -> item_actual.icono
        }

        Glide.with(contexto)
            .load(URL)
            .apply(Utilidades.opcionesGlide(contexto))
            .transition(Utilidades.transicion)
            .into(holder.miniatura)

        holder.editar.setOnClickListener {
            val activity = Intent(contexto, EditarPokemon::class.java)
            activity.putExtra("Pokedex", item_actual)
            contexto.startActivity(activity)
        }

        holder.eliminar.setOnClickListener {
            val db_ref = FirebaseDatabase.getInstance().getReference()
            val sto_ref = FirebaseStorage.getInstance().getReference()
            lista_filtrada.remove(item_actual)
            sto_ref.child("Pokedex").child("Pokemon")
                .child("icono").child(item_actual.id!!).delete()
            db_ref.child("Pokedex").child("Pokemon")
                .child(item_actual.id!!).removeValue()

            Toast.makeText(contexto, "Pokemon borrado con exito", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = lista_filtrada.size

    class PokeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val miniatura: ImageView = itemView.findViewById(R.id.item_miniatura)
        val nombre: TextView = itemView.findViewById(R.id.item_nombre)
        val tipo1: TextView = itemView.findViewById(R.id.item_tipo1)
        val tipo2: TextView = itemView.findViewById(R.id.item_tipo2)
        val poder: TextView = itemView.findViewById(R.id.item_poder)
        val editar: ImageView = itemView.findViewById(R.id.item_editar)
        val eliminar: ImageView = itemView.findViewById(R.id.item_borrar)
        val rating: SimpleRatingBar = itemView.findViewById(R.id.ratingBar)
        val fecha : TextView =itemView.findViewById(R.id.item_fecha)
    }

    fun sortListByOrder(order: String) {
        when (order) {
            "Nombre Ascendente" -> lista_filtrada.sortBy { it.nombre }
            "Nombre Descendente" -> lista_filtrada.sortByDescending { it.nombre }
            "Nivel Ascendente" -> lista_filtrada.sortBy { it.poder }
            "Nivel Descendente" -> lista_filtrada.sortByDescending { it.poder }
            // Agrega otros casos según tus necesidades
        }
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<Pokemon>()

                if (constraint.isNullOrBlank()) {
                    filteredList.addAll(lista_club)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (pokemon in lista_club) {
                        if (pokemon.nombre?.toLowerCase()?.contains(filterPattern) == true) {
                            filteredList.add(pokemon)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null) {
                    lista_filtrada = results.values as MutableList<Pokemon>
                    notifyDataSetChanged()
                }
            }
        }
    }
}