package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ShieldService : AccessibilityService() {

    private var lastActionTime: Long = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        // Evita rajadas excessivas de verificação (debounce de 300ms)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 300) return

        // Executa a varredura inteligente na tela
        scanAndShield(rootNode)
    }

    private fun scanAndShield(node: AccessibilityNodeInfo) {
        // 1. Prioridade: Se houver um botão de fechar/pular visível, clica imediatamente
        if (AdClassifier.isCloseOrSkipButton(node)) {
            val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clicked) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio pulado/fechado automaticamente!")
                return
            }
        }

        // 2. Detecção Contextual: Se identificar elemento marcado como Patrocinado/Ad
        if (AdClassifier.isAdElement(node)) {
            // Tenta localizar um botão de fechar dentro ou próximo a este contêiner de anúncio
            val parent = node.parent
            if (parent != null && findAndClickCloseInContainer(parent)) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio contextual neutralizado!")
                return
            }
        }

        // Continua a varredura recursiva nos nós filhos da tela
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                scanAndShield(child)
            }
        }
    }

    private fun findAndClickCloseInContainer(container: AccessibilityNodeInfo): Boolean {
        for (i in 0 until container.childCount) {
            val child = container.getChild(i) ?: continue
            if (AdClassifier.isCloseOrSkipButton(child)) {
                return child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            if (child.childCount > 0) {
                if (findAndClickCloseInContainer(child)) return true
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
