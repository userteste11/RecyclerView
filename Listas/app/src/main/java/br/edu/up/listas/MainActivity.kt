package br.edu.up.listas

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var customAdapter: CustomAdapter
    private var SEU_REQUEST_CODE = 1
    private var dataset = mutableListOf<Produto>()

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, activity_Temporaria::class.java)
        startActivity(intent)

        val recicle: RecyclerView = findViewById(R.id.reciclerView)

        customAdapter = CustomAdapter(dataset, this)
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = customAdapter

    }

    class CustomAdapter(private val dataSet: MutableList<Produto>, private val context: Context) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nomeTextView: TextView
            init {
                nomeTextView = view.findViewById(R.id.nomeTextView)
            }
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CustomAdapter.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
            val produto = dataSet[position]
            holder.nomeTextView.text = produto.nome
        }
        override fun getItemCount(): Int {//trazer o tamanho da list
            return dataSet.size
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        //if para recuperar os dados e criar
        if (requestCode == SEU_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Verifica se a ActivityAdicionar retornou nome e quantidade
            val nome = data?.getStringExtra("nome_produto")

            if (nome != null && nome.isNotEmpty()) {
                val novoProduto = Produto(nome)
                dataset.add(novoProduto)
                customAdapter.notifyItemInserted(dataset.size - 1)
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this, "Adicionado", duracaoCurta).show()
                Log.d("MainActivity", "Novo Produto Criado - Nome: $nome")
            } else {
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this, "Dados Incompletos", duracaoCurta).show()
                Log.d("MainActivity", "Dados da Activity não recebidos corretamente")
            }
        }
    }

    fun PaginaAdicionarProduto(view: View) {
        val intent = Intent(this, ActivityAdicionar::class.java)
        startActivityForResult(
            intent,
            SEU_REQUEST_CODE
        )//chama uma nova activity e espera um resultado
    }
}