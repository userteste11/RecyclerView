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
        //set O EditText nas variavel
        textoNome= findViewById(R.id.TextNome)

    }

    fun enviarDados(view: View) {

        val novoProduto = textoNome.text.toString()

        val intent = Intent()
        //retorno a variavel
        intent.putExtra("nome_produto", novoProduto)

        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}


