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
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var customAdapter: CustomAdapter
    private var SEU_REQUEST_CODE = 1
    private var dataset = mutableListOf<Produto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, activity_Temporaria::class.java)
        startActivity(intent)

        val recicle: RecyclerView = findViewById(R.id.reciclerView)

        // Use uma Coroutine para buscar os produtos
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = db.collection("Produtos")
                    .get()
                    .await()

                val novosProdutos = mutableListOf<Produto>()
                for (document in querySnapshot) {
                    val nome = document.getString("nome") ?: "erro"
                    val quantidade = document.getLong("qtd")?.toInt() ?: 0
                    val marcado = document.getBoolean("marcado") ?: false

                    val produto = Produto(nome, quantidade, marcado)
                    novosProdutos.add(produto)
                }
                // Atualizar o dataset com os novos produtos na thread principal
                withContext(Dispatchers.Main) {
                    dataset.clear()
                    dataset.addAll(novosProdutos)
                    customAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                // Lidar com erros, como conexão com o Firestore
                Log.e("MainActivity", "Erro ao buscar produtos: $e")
            }
        }



        customAdapter = CustomAdapter(dataset, this)
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter = customAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
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
                val produtoNome = produtoRemovido.nome

                // Remover o produto do dataset
                dataset.removeAt(position)
                customAdapter.notifyItemRemoved(position)

                // Execute a exclusão em uma Coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    // Código de exclusão no Firestore
                    val db = FirebaseFirestore.getInstance()
                    val produtosCollection = db.collection("Produtos")

                    val querySnapshot = produtosCollection
                        .whereEqualTo("nome", produtoNome)
                        .get()
                        .await()

                    for (document in querySnapshot) {
                        produtosCollection.document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d("MainActivity", "Produto excluído do Firestore: $produtoNome")
                            }
                            .addOnFailureListener { e ->
                                Log.w("MainActivity", "Erro ao excluir o produto do Firestore", e)
                            }
                    }
                }
            }
        }
        )
        itemTouchHelper.attachToRecyclerView(recicle)
    }

    class CustomAdapter(private val dataSet: MutableList<Produto>, private val context: Context) :
        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.text_row_item, parent, false)
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
                // Inicie uma coroutine para atualizar o produto no Firebase
                CoroutineScope(Dispatchers.IO).launch {
                    atualizarProdutoNoFirebase(produto)
                }
            }
        }

        private suspend fun atualizarProdutoNoFirebase(produto: Produto) {
            val db = FirebaseFirestore.getInstance()
            val produtosCollection = db.collection("Produtos")
            // Consulte o documento correspondente ao produto no Firestore com base no nome do produto
            val querySnapshot = produtosCollection
                .whereEqualTo("nome", produto.nome)
                .get()
                .await()

            for (document in querySnapshot) {
                val documentId = document.id
                // Atualize o campo "marcado" no documento correspondente
                produtosCollection.document(documentId)
                    .update("marcado", produto.marcado)
                    .await()

                Log.d("MainActivity", "Produto atualizado no Firebase: $produto")
            }
        }
        override fun getItemCount(): Int {//trazer o tamanho da list
            return dataSet.size
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val db = FirebaseFirestore.getInstance()
        super.onActivityResult(requestCode, resultCode, data)
        //if para recuperar os dados e criar
        if (requestCode == SEU_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Verifica se a ActivityAdicionar retornou nome e quantidade
            val nome = data?.getStringExtra("nome_produto")
            val quantidadep = data?.getIntExtra("qtd_produto", 0)

            if (nome != null && nome.isNotEmpty() && quantidadep != null) {
                val novoProduto = Produto(nome, quantidadep, false)

                db.collection("Produtos") // Substitua "produtos" pelo nome da sua coleção
                    .add(novoProduto)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "MainActivity",
                            "Produto adicionado com o ID: ${documentReference.id}"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.w("MainActivity", "Erro ao adicionar Produto", e)
                    }
                dataset.add(novoProduto)
                customAdapter.notifyItemInserted(dataset.size - 1)
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this, "Adicionado", duracaoCurta).show()

                Log.d("MainActivity", "Novo Produto Criado - Nome: $nome, Quantidade: $quantidadep")
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

    fun LimparProduto(view: View) {
        val produtosParaExcluir = mutableListOf<Produto>()
        for (produto in dataset) {
            if (produto.marcado) {
                produtosParaExcluir.add(produto)
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            val sucesso = excluirProdutosMarcadosNoFirebase(produtosParaExcluir)
            if (sucesso) {
                // Remova os produtos da lista após a exclusão
                dataset.removeAll(produtosParaExcluir)
                customAdapter.notifyDataSetChanged()

                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this@MainActivity, "Produtos excluídos com sucesso", duracaoCurta)
                    .show()
            } else {
                val duracaoCurta = Toast.LENGTH_LONG
                Toast.makeText(this@MainActivity, "Falha ao excluir produtos", duracaoCurta).show()
            }
        }
    }
    private suspend fun excluirProdutosMarcadosNoFirebase(produtosParaExcluir: List<Produto>): Boolean {
        return withContext(Dispatchers.IO) {
            val db = FirebaseFirestore.getInstance()
            val produtosCollection = db.collection("Produtos")
            try {
                for (produto in produtosParaExcluir) {
                    val querySnapshot = produtosCollection
                        .whereEqualTo("nome", produto.nome)
                        .get()
                        .await()

                    for (document in querySnapshot) {
                        val documentId = document.id

                        // Exclua o documento correspondente
                        produtosCollection.document(documentId)
                            .delete()
                            .await()
                        Log.d("MainActivity", "Produto excluído do Firebase: ${produto.nome}")
                    }
                }
                return@withContext true
            } catch (e: Exception) {
                Log.w("MainActivity", "Erro ao excluir produtos do Firebase", e)
                return@withContext false
            }
        }
    }
}