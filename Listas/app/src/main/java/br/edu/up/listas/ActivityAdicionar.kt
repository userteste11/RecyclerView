package br.edu.up.listas

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import java.util.ArrayList

class ActivityAdicionar : AppCompatActivity() {
    private lateinit var textoNome: EditText
    private lateinit var textoQuantidade: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adicionar)

        textoNome= findViewById(R.id.TextNome)
        textoQuantidade= findViewById(R.id.TextQtd)




    }

    fun enviarDados(view: View) {
        //val novoAluno = "novo produto"
        //val qtdproduto = 12
        val novoProduto = textoNome.text.toString()
        val quantidadeProduto = textoQuantidade.text.toString().toIntOrNull() ?: 0
        val intent = Intent()
        intent.putExtra("nome_produto", novoProduto)
        intent.putExtra("qtd_produto", quantidadeProduto)

        setResult(Activity.RESULT_OK, intent)
        finish()

    }
}


