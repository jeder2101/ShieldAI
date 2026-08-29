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

    // Lista de pacotes que o ShieldAI NUNCA deve monitorar (Bancos e Sistema)
    private val ignoredPackages = setOf(
        "br.com.gft.cesta",              // Caixa
        "br.gov.caixa.tem",              // Caixa Tem
        "com.itau",                      // Itaú
        "br.com.bradesco.next",          // Bradesco / Next
        "com.nu.production",             // Nubank
        "com.santander.app",             // Santander
        "com.android.launcher",          // Launcher padrão Android
        "com.sec.android.app.launcher",  // Launcher Samsung
        "com.google.android.apps.nexuslauncher" // Launcher Google
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // 1. Ignora imediatamente se for um app bancário ou a tela inicial (Launcher)
        if (isIgnoredPackage(packageName)) {
            return
        }

        val rootNode = rootInActiveWindow ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime < 300) return

        scanAndNeutralize(rootNode)
    }

    private fun isIgnoredPackage(packageName: String): Boolean {
        for (ignored in ignoredPackages) {
            if (packageName.contains(ignored, ignoreCase = true) || packageName.startsWith("br.gov.caixa")) {
                return true
            }
        }
        return false
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

            performSmartSwipe()
            return
        }

        // Varredura recursiva
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanAndNeutralize(child)
        }
    }

    private fun performSmartSwipe() {
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val swipePath = Path()

        if (isLandscape) {
            val startX = width * 0.8f
            val endX = width * 0.2f
            val middleY = height / 2f
            swipePath.moveTo(startX, middleY)
            swipePath.lineTo(endX, middleY)
        } else {
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
