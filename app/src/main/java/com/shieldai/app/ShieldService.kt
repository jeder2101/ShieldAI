package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.os.Handler
import android.os.Looper

class ShieldService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // Obtém o nome do pacote do aplicativo em execução (ex: com.whatsapp, com.android.chrome)
            val packageName = it.packageName?.toString() ?: "App desconhecido"

            // Extrai o texto contido no evento de acessibilidade
            val eventTexts = it.text.filterNotNull().joinToString(" ")

            // Se houver texto capturado, exibe um alerta temporário
            if (eventTexts.isNotBlank()) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "[$packageName]: $eventTexts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onInterrupt() {
        // Método chamado se o sistema interromper o serviço
    }
}
