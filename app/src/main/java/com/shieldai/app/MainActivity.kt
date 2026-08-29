package com.shieldai.app

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import android.provider.Settings
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Criar o layout principal via código
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }

        val title = TextView(this).apply {
            text = "ShieldAI Ativo"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "Ativar Serviço de Acessibilidade"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(button)

        setContentView(layout)
    }
}
