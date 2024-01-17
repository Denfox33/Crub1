package com.example.firebase
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SearchView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.view.View


class VerPokemon : AppCompatActivity() {

    private lateinit var volver: Button
    private lateinit var recycler: RecyclerView
    private lateinit var lista: MutableList<Pokemon>
    private lateinit var adaptador: PokeAdaptador
    private lateinit var db_ref: DatabaseReference




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_pokemon)

        volver = findViewById(R.id.volver_inicio)
        lista = mutableListOf()
        db_ref = FirebaseDatabase.getInstance().getReference()

        db_ref.child("Pokedex")
            .child("Pokemon")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach { hijo: DataSnapshot?
                        ->
                        val pojo_poke = hijo?.getValue(Pokemon::class.java)
                        lista.add(pojo_poke!!)
                    }
                    recycler.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }

            })


        adaptador = PokeAdaptador(lista)
        recycler = findViewById(R.id.lista_poke)
        recycler.adapter = adaptador
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.setHasFixedSize(true)

        volver.setOnClickListener {
            val activity = Intent(applicationContext, MainActivity::class.java)
            startActivity(activity)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pokemon, menu)
        val itemSearch = menu?.findItem(R.id.search)
        val itemSpinner = menu?.findItem(R.id.filtrado)
        val searhView = itemSearch?.actionView as SearchView

        // Configurar el SearchView
        searhView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adaptador.filter.filter(newText)
                return true
            }
        })

        itemSearch.setOnMenuItemClickListener {
            if (itemSpinner != null) {
                itemSpinner.collapseActionView()
            }
            true
        }
        // goala
        if (itemSpinner != null) {
            itemSpinner.setOnMenuItemClickListener {
                itemSearch.collapseActionView()
                true
            }
        }

        // Configurar el Spinner
        val orderSpinner = itemSpinner?.actionView as Spinner
        val orderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.order_options,
            android.R.layout.simple_spinner_item
        )
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        orderSpinner.adapter = orderAdapter

        orderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: android.view.View?,
                position: Int,
                id: Long
            ) {
                // Acciones a realizar cuando se selecciona un elemento en el Spinner
                val selectedOrder = parentView?.getItemAtPosition(position).toString()
                // Ordenar la lista según la selección del usuario
                adaptador.sortListByOrder(selectedOrder)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Acciones a realizar cuando no se selecciona nada en el Spinner (opcional)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

}

