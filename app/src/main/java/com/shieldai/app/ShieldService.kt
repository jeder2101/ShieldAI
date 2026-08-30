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

    private val ignoredPackages = setOf(
        "br.com.gft.cesta", "br.gov.caixa.tem", "com.itau", 
        "br.com.bradesco.next", "com.nu.production", "com.santander.app", 
        "com.android.launcher", "com.sec.android.app.launcher", 
        "com.google.android.apps.nexuslauncher"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        if (isIgnoredPackage(packageName)) return

        val rootNode = rootInActiveWindow ?: return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 150) return

        scanAndNeutralizeAllTypes(rootNode)
    }

    private fun isIgnoredPackage(packageName: String): Boolean {
        val lower = packageName.lowercase()
        for (ignored in ignoredPackages) {
            if (lower.contains(ignored) || lower.contains("caixa") || lower.contains("launcher")) return true
        }
        return false
    }

    private fun scanAndNeutralizeAllTypes(node: AccessibilityNodeInfo) {
        // AÇÃO 1: Clique em Botão de Fechar/Pular/Cancelar (Tipos 2, 3 e 5)
        if (AdClassifier.isCloseOrSkipButton(node)) {
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                lastActionTime = System.currentTimeMillis()
                notifyUser("ShieldAI: Anúncio fechado!")
                return
            }
        }

        // AÇÃO 2: Trata Feeds Verticais ou Banners sem Botão (Tipos 1 e 4)
        if (AdClassifier.isAdElement(node)) {
            // Primeiro tenta encontrar um botão de fechar nos filhos ou no contêiner pai
            if (findAndClickCloseInAncestors(node)) return

            // Se não houver botão, faz o Swipe Adaptativo para rolar/tirar o anúncio da tela
            lastActionTime = System.currentTimeMillis()
            notifyUser("ShieldAI: Anúncio neutralizado!")
            performSmartSwipe()
            return
        }

        // Varredura nos nós filhos
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
