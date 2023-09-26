package br.edu.up.listas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class DetalhesAlunoActivity : AppCompatActivity() {
    private lateinit var editTextNome: EditText
    private lateinit var editTextQtd: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_aluno)
        editTextNome= findViewById(R.id.editTextNome)
        val nomeProduto = intent.getStringExtra("nome")
        editTextNome.setText(nomeProduto)
        editTextQtd= findViewById(R.id.editTextquantidade)
        val qtdProduto = intent.getIntExtra("qtd", 0).toString()
        editTextQtd.setText(qtdProduto)
    }
    fun voltarLista(view: View){

        finish()

    }
}

