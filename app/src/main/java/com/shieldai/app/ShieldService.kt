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

    // LISTA COMPLETA DE APPS SENSÍVEIS (BANCOS, CARTEIRAS, GOVERNO E TELA DE INÍCIO)
    private val ignoredKeywords = listOf(
        // Bancos e Fintechs
        "caixa", "gft.cesta", "itau", "bradesco", "next", "nubank", "santander",
        "inter", "c6bank", "bancopan", "pagbank", "pagseguro", "picpay", "mercadopago",
        "stone", "ton", "neon", "bs2", "original", "safra", "sicoob", "sicredi",
        
        // Apps Governamentais e Documentos
        "gov.br", "carteiradigital", "fgts", "e-titulo", "cnh",
        
        // System UI e Launchers (Gaveta de Apps / Tela Inicial)
        "launcher", "systemui", "trebuchet", "miui.home", "oneui"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // Interrompe imediatamente se for banco, governo ou tela inicial
        if (isIgnoredPackage(packageName)) return

        val rootNode = rootInActiveWindow ?: return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 150) return

        scanAndNeutralizeAllTypes(rootNode)
    }

    private fun isIgnoredPackage(packageName: String): Boolean {
        val lower = packageName.lowercase()
        for (keyword in ignoredKeywords) {
            if (lower.contains(keyword)) return true
        }
        return false
    }

    private fun scanAndNeutralizeAllTypes(node: AccessibilityNodeInfo) {
        // 1. Clique em Botão de Fechar/Pular/Cancelar (Tipos 2, 3 e 5)
        if (AdClassifier.isCloseOrSkipButton(node)) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio fechado!")
                return
            }
        }

        // 2. Trata Feeds Verticais ou Banners sem Botão (Tipos 1 e 4)
        if (AdClassifier.isAdElement(node)) {
            if (findAndClickCloseInAncestors(node)) return

            lastActionTime = System.currentTimeMillis()
            notifyUser("ShieldAI: Anúncio neutralizado!")
            performSmartSwipe()
            return
        }

        // Varredura recursiva nos nós filhos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanAndNeutralizeAllTypes(child)
        }
    }

    private fun findAndClickCloseInAncestors(node: AccessibilityNodeInfo): Boolean {
        var parent = node.parent
        var depth = 0
        while (parent != null && depth < 4) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChild(i) ?: continue
                if (AdClassifier.isCloseOrSkipButton(child)) {
                    if (child.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        lastActionTime = System.currentTimeMillis()
                        notifyUser("ShieldAI: Anúncio neutralizado!")
                        return true
                    }
                }
            }
            parent = parent.parent
            depth++
        }
        return false
    }

    private fun performSmartSwipe() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val swipePath = Path()
        if (isLandscape) {
            swipePath.moveTo(width * 0.8f, height / 2f)
            swipePath.lineTo(width * 0.2f, height / 2f)
        } else {
            swipePath.moveTo(width / 2f, height * 0.8f)
            swipePath.lineTo(width / 2f, height * 0.2f)
        }

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 150))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun notifyUser(msg: String) {
        handler.post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {}
}
