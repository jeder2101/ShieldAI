package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
    private val mainHandler = Handler(Looper.getMainLooper())

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
                
                val bounds = Rect()
                node.getBoundsInScreen(bounds)

                // Garante que o elemento tem tamanho visível na tela
                if (bounds.width() > 10 && bounds.height() > 10) {
                    mainHandler.post {
                        drawShieldOverlay(bounds)
                    }
                }
                break
            }
        }

        // Percorre a árvore de elementos
        for (i in 0 until node.childCount) {
            scanNode(node.getChild(i))
        }
    }

    private fun drawShieldOverlay(bounds: Rect) {
        // Correção de Segurança 1: Checa se a permissão de Overlay está ativa no sistema
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Permissão de sobreposição não concedida ainda.")
            return
        }

        // Remove sobreposição anterior se existir
        removeShieldOverlay()

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

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

        // View de teste: caixa vermelha translúcida para verificar visualmente se cobriu o anúncio
        overlayView = View(this).apply {
            setBackgroundColor(Color.parseColor("#44FF0000")) 
            setOnClickListener {
                Log.d(TAG, "Clique no anúncio bloqueado com sucesso!")
            }
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao adicionar View ao WindowManager: ${e.message}")
        }
    }

    private fun removeShieldOverlay() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover View: ${e.message}")
            }
            overlayView = null
        }
    }

    override fun onInterrupt() {
        mainHandler.post {
            removeShieldOverlay()
        }
    }
}
