package com.shieldai.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    private val adKeywords = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", 
        "promovido", "promoted", "ad ", "ads ", "saiba mais", 
        "instalar agora", "comprar agora", "shop now", "install now"
    )

    private val actionKeywords = listOf(
        "pular", "skip", "fechar", "close", "dismiss", "x"
    )

    // IDs de contêineres e botões de fechar usados por SDKs de anúncio (AdMob, Unity, AppLovin, IronSource)
    private val adViewIds = listOf(
        "ad_view", "banner_ad", "native_ad", "sponsor", "ads_container",
        "tt_ad", "anythink", "applovin", "mbridge", "close_btn", "btn_close",
        "closebutton", "dismiss_button", "ksad_kwai"
    )

    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""

        // Check por ID de infraestrutura de anúncios
        for (id in adViewIds) {
            if (viewId.contains(id)) return true
        }

        // Check por texto tradicional
        if (text.isNotBlank()) {
            for (keyword in adKeywords) {
                if (text == keyword || text.contains(keyword)) return true
            }
        }
        return false
    }

    fun isCloseOrSkipButton(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (!node.isClickable) return false

        // 1. Detecta botões com IDs típicos de fechar ("close", "btn_close", "x")
        for (id in adViewIds) {
            if (viewId.contains("close") || viewId.contains("skip") || viewId.contains("dismiss")) {
                return true
            }
        }

        // 2. Detecta por texto/descrição
        for (keyword in actionKeywords) {
            if (text == keyword || text.contains(keyword) || viewId.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
