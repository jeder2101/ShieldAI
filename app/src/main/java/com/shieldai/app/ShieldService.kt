package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ShieldService : AccessibilityService() {

    private var lastActionTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        // Processamento em tempo real com debounce minimo (100ms)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 100) return

        scanAndShieldRealTime(rootNode)
    }

    private fun scanAndShieldRealTime(node: AccessibilityNodeInfo) {
        // 1. Ação de Clique Direto em Botões de Pulo/Fechamento
        if (AdClassifier.isCloseOrSkipButton(node)) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio bloqueado em tempo real!")
                return
            }
        }

        // 2. Detecção Contextual de Anúncio na Tela
        if (AdClassifier.isAdElement(node)) {
            // Tenta fechar o anúncio buscando o botão correspondente no elemento pai
            var parent = node.parent
            var depth = 0
            while (parent != null && depth < 5) {
                if (clickCloseChild(parent)) {
                    lastActionTime = System.currentTimeMillis()
                    notifyUser("ShieldAI: Elemento publicitário neutralizado!")
                    return
                }
                parent = parent.parent
                depth++
            }
        }

        // Varredura recursiva
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanAndShieldRealTime(child)
        }
    }

    private fun clickCloseChild(container: AccessibilityNodeInfo): Boolean {
        for (i in 0 until container.childCount) {
            val child = container.getChild(i) ?: continue
            if (AdClassifier.isCloseOrSkipButton(child)) {
                return child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            if (child.childCount > 0) {
                if (clickCloseChild(child)) return true
            }
        }
        return false
    }

    private fun notifyUser(msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {}
}
