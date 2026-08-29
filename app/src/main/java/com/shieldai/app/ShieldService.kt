package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ShieldService : AccessibilityService() {

    private var lastActionTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isWaitingForTimer = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 100) return

        scanAndShieldRealTime(rootNode)
    }

    private fun scanAndShieldRealTime(node: AccessibilityNodeInfo) {
        // 1. Tenta clicar imediatamente se o botão já estiver liberado/clicável
        if (AdClassifier.isCloseOrSkipButton(node) && node.isClickable) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                lastActionTime = System.currentTimeMillis()
                isWaitingForTimer = false
                notifyUser("ShieldAI: Anúncio pulado com sucesso!")
                return
            }
        }

        // 2. Se detectar um temporizador rodando, ativa a checagem contínua acelerada
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        if (hasTimerIndicator(text) && !isWaitingForTimer) {
            isWaitingForTimer = true
            notifyUser("ShieldAI: Temporizador detectado. Aguardando liberação...")
            scheduleFastCheck()
        }

        // Varredura nos nós filhos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanAndShieldRealTime(child)
        }
    }

    private fun hasTimerIndicator(text: String): Boolean {
        return text.contains("pular em") || text.contains("skip in") || 
               text.contains("anúncio em") || text.matches(Regex(".*\\b[0-9]{1,2}\\b.*"))
    }

    private fun scheduleFastCheck() {
        if (!isWaitingForTimer) return
        
        handler.postDelayed({
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                scanAndShieldRealTime(rootNode)
            }
            // Mantém a verificação contínua enquanto o anúncio estiver na tela
            if (isWaitingForTimer) {
                scheduleFastCheck()
            }
        }, 150) // Checa a cada 150 milissegundos
    }

    private fun notifyUser(msg: String) {
        handler.post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {
        isWaitingForTimer = false
    }
}
