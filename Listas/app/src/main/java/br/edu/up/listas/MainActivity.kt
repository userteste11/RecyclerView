package br.edu.up.listas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var customAdapter: CustomAdapter
    private var SEU_REQUEST_CODE = 1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var dataset = mutableListOf<Produto>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val recicle: RecyclerView = findViewById(R.id.reciclerView)
        sharedPreferences = getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        //pego o ultimo id
        var contadorProdutos = sharedPreferences.getInt("contador_produtos", 0)
        Log.d("MainActivity", "produtos cadastrados: $contadorProdutos")

;       //preencho a list dataset com os produtos
        for (i in 1..contadorProdutos) {
            val idProduto = sharedPreferences.getString("produto_${i}_id", "")?:""
            val nomeProduto = sharedPreferences.getString("produto_${i}_nome", "")?:""
            val quantidadeProduto = sharedPreferences.getInt("produto_${i}_quantidade", 0)
            val marcado = sharedPreferences.getBoolean("produto_${i}_marcado", false)

            if (nomeProduto.isNotEmpty() && quantidadeProduto > 0) {
                val produto = Produto(idProduto, nomeProduto, quantidadeProduto, marcado)
                dataset.add(produto)
            }
        }

        customAdapter = CustomAdapter(dataset, this)
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = customAdapter

        val itemTouchHelper = ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition

                // Recuperar as informações do produto que está sendo movido/deslizado
                val produtoRemovido = dataset[position]
                val produtoId = produtoRemovido.id

                // Remover o produto do dataset
                dataset.removeAt(position)
                customAdapter.notifyItemRemoved(position)

                // Remova as informações do produto que está sendo movido/deslizado no sharedPreferences
                if (produtoId != null) {
                    editor.remove("produto_${produtoId}_id")
                    editor.remove("produto_${produtoId}_nome")
                    editor.remove("produto_${produtoId}_quantidade")
                    editor.remove("produto_${produtoId}_marcado")
                    editor.apply()
                }
                var contadorProdutos = sharedPreferences.getInt("contador_produtos", 0)

                editor.putInt("contador_produtos", contadorProdutos)
                editor.apply()

            }
        }
        )
        itemTouchHelper.attachToRecyclerView(recicle)
    }

    class CustomAdapter(private val dataSet: MutableList<Produto>, private val context: Context) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>(){
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val nomeTextView: TextView
            val qtdTextView: TextView
            val marcadocheckboxView: CheckBox

            init {
                nomeTextView = view.findViewById(R.id.nomeTextView)
                qtdTextView = view.findViewById(R.id.cursoTextView)
                marcadocheckboxView = view.findViewById(R.id.marcadocheckbox)
            }
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CustomAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
            val produto = dataSet[position]
            holder.nomeTextView.text = produto.nome
            holder.qtdTextView.text = produto.qtd.toString()

            holder.marcadocheckboxView.isChecked = produto.marcado

            holder.marcadocheckboxView.setOnCheckedChangeListener { _, isChecked ->
                // Atualize a propriedade "marcado" do produto quando o CheckBox for alterado
                produto.marcado = isChecked

                // Agora, você pode salvar o estado marcado no SharedPreferences usando o contexto
                val sharedPreferences = context.getSharedPreferences("MinhasPreferencias", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("produto_${produto.id}_marcado", isChecked) // Use o ID do produto para identificar a entrada no SharedPreferences
                editor.apply()
            }
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
            val quantidadep = data?.getIntExtra("qtd_produto", 0)

            if (nome != null && nome.isNotEmpty() && quantidadep != null) {
                var contadorProdutos = sharedPreferences.getInt("contador_produtos", 0)
                //cadastro
                contadorProdutos++
                editor.putInt("contador_produtos", contadorProdutos)
                editor.putString("produto_${contadorProdutos}_id", contadorProdutos.toString())
                editor.putString("produto_${contadorProdutos}_nome", nome)
                editor.putInt("produto_${contadorProdutos}_quantidade", quantidadep)
                editor.putBoolean("produto_${contadorProdutos}_marcado", false)
                editor.apply()
                //cadastro na list
                val novoProduto = Produto(contadorProdutos.toString() ,nome, quantidadep, false)
                dataset.add(novoProduto)
                customAdapter.notifyItemInserted(dataset.size - 1)
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this, "Cadastrado", duracaoCurta).show()

                Log.d("MainActivity", "Novo Produto Criado - Nome: $nome, Quantidade: $quantidadep, id: $contadorProdutos")
            }else{
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this, "Dados Incompletos", duracaoCurta).show()

                Log.d("MainActivity", "Dados da Activity não recebidos corretamente")
            }
        }
    }

    fun PaginaAdicionarProduto(view: View) {
        val intent = Intent(this, ActivityAdicionar::class.java)
        startActivityForResult(intent, SEU_REQUEST_CODE)//chama uma nova activity e espera um resultado

    }

    fun LimparProduto(view: View)
    {
        val iterator = dataset.iterator()
        while (iterator.hasNext()) {//percorro a list
            val produto = iterator.next()
            if (produto.marcado) {
                iterator.remove() // Remove produtos marcados da lista e depois do sharedPreference
                editor.remove("produto_${produto.id}_id")
                editor.remove("produto_${produto.id}_nome")
                editor.remove("produto_${produto.id}_quantidade")
                editor.remove("produto_${produto.id}_marcado")
                editor.apply()
            }
        }
        customAdapter.notifyDataSetChanged()
        val duracaoCurta = Toast.LENGTH_LONG
        Toast.makeText(this, "Removido com Sucesso", duracaoCurta).show()
    }
}