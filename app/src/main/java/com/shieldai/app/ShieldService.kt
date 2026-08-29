package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShieldService : AccessibilityService() {

    private val TAG = "ShieldAI"
    private val adKeywords = listOf("patrocinado", "anúncio", "promoted", "sponsored", "ad")
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        scanNode(rootNode)
    }

    private fun scanNode(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        // 1. Tentar auto-fechar se for um botão "X" de fechar anúncio
        if (text == "x" || desc == "fechar" || desc == "close") {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Tentativa de auto-fechamento do anúncio executada.")
            return
        }

        // 2. Identificar se o elemento é um anúncio/banner
        for (keyword in adKeywords) {
            if (text.contains(keyword) || desc.contains(keyword)) {
                Log.d(TAG, "Anúncio detectado: $keyword no app ${node.packageName}")
                
                // Pega as coordenadas exatas do anúncio na tela
                val bounds = Rect()
                node.getBoundsInScreen(bounds)

                // Desenha a camada de proteção sobre o anúncio
                if (bounds.width() > 0 && bounds.height() > 0) {
                    drawShieldOverlay(bounds)
                }
                break
            }
        }

        // Percorre a árvore de elementos da tela
        for (i in 0 until node.childCount) {
            scanNode(node.getChild(i))
        }
    }

    private fun drawShieldOverlay(bounds: Rect) {
        // Remove a sobreposição anterior, se existir
        removeShieldOverlay()

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Configura uma área que intercepta toques na região do anúncio
        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = bounds.left
            y = bounds.top
        }

        // View de proteção (pode ser transparente ou levemente sombreada para testes)
        overlayView = View(this).apply {
            setBackgroundColor(Color.parseColor("#33FF0000")) // Sombra vermelha transparente para testes
            setOnClickListener {
                Log.d(TAG, "Clique no anúncio foi bloqueado pelo ShieldAI!")
            }
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao desenhar sobreposição: ${e.message}")
        }
    }

    private fun removeShieldOverlay() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover sobreposição: ${e.message}")
            }
            overlayView = null
        }
    }

    override fun onInterrupt() {
        removeShieldOverlay()
    }
}
