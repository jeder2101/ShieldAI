package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.res.Configuration
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class ShieldService : AccessibilityService() {

    private var lastActionTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 300) return

        scanAndNeutralize(rootNode)
    }

    private fun scanAndNeutralize(node: AccessibilityNodeInfo) {
        // 1. Prioridade: Botões explícitos de fechar/pular
        if (AdClassifier.isCloseOrSkipButton(node) && node.isClickable) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio fechado!")
                return
            }
        }

        // 2. Detecção de Anúncio Contextual sem botão de fechar imediato
        if (AdClassifier.isAdElement(node)) {
            lastActionTime = System.currentTimeMillis()
            notifyUser("ShieldAI: Anúncio detectado. Neutralizando...")

            // Executa o gesto de pular adaptado para a orientação da tela
            performSmartSwipe()
            return
        }

        // Varredura recursiva nos elementos da tela
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanAndNeutralize(child)
        }
    }

    /**
     * Calcula as dimensões e executa o movimento de Swipe correto 
     * dependendo se a tela está Em Pé (Vertical) ou Deitada (Horizontal).
     */
    private fun performSmartSwipe() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val swipePath = Path()

        if (isLandscape) {
            // TELA DEITADA (Jogos / Vídeos): Desliza da direita para a esquerda
            val startX = width * 0.8f
            val endX = width * 0.2f
            val middleY = height / 2f

            swipePath.moveTo(startX, middleY)
            swipePath.lineTo(endX, middleY)
        } else {
            // TELA EM PÉ (TikTok / Kwai / Shorts): Desliza de baixo para cima
            val middleX = width / 2f
            val startY = height * 0.8f
            val endY = height * 0.2f

            swipePath.moveTo(middleX, startY)
            swipePath.lineTo(middleX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 150))

        dispatchGesture(gestureBuilder.build(), object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
            }
        }, null)
    }

    private fun notifyUser(msg: String) {
        handler.post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {}
}
