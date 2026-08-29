package com.shieldai.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    // Identificadores universais de publicidade
    private val adKeywords = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", 
        "promovido", "promoted", "ad ", "ads ", "saiba mais", 
        "instalar agora", "comprar agora", "shop now", "install now"
    )

    private val actionKeywords = listOf(
        "pular", "skip", "fechar", "close", "dismiss", "x"
    )

    // IDs de layouts conhecidos de redes de anúncios (AdMob, Unity Ads, etc)
    private val adViewIds = listOf(
        "ad_view", "banner_ad", "native_ad", "sponsor", "ads_container"
    )

    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        // Verifica por ID de recurso de redes de ad
        for (id in adViewIds) {
            if (viewId.contains(id)) return true
        }

        // Verifica por texto de patrocínio ou chamada de ad
        if (text.isNotBlank()) {
            for (keyword in adKeywords) {
                if (text == keyword || text.startsWith("$keyword ") || text.endsWith(" $keyword")) {
                    return true
                }
            }
        }
        return false
    }

    fun isCloseOrSkipButton(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (!node.isClickable) return false

        for (keyword in actionKeywords) {
            if (text.contains(keyword) || viewId.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
