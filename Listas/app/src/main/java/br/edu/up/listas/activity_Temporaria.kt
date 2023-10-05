package br.edu.up.listas

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class activity_Temporaria : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temporaria)
        Handler().postDelayed({
            finish()
        }, 1500) // 1000 milissegundos = 1 segundo
    }
}