package com.shieldai.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    // Termos textuais universais (Português, Inglês e Espanhol)
    private val adKeywords = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", "promovido", "promoted", 
        "ad ", "ads ", "saiba mais", "instalar agora", "comprar agora", "shop now", 
        "install now", "baixar agora", "download now", "publicidad"
    )

    // Ações de fechamento/pulo
    private val actionKeywords = listOf(
        "pular", "skip", "fechar", "close", "dismiss", "x", "cancelar", "cancel"
    )

    // IDs de contêineres e SDKs de anúncios globais (AdMob, Unity Ads, AppLovin, Kwai, ByteDance, IronSource, Vungle)
    private val adViewIds = listOf(
        "ad_view", "banner_ad", "native_ad", "sponsor", "ads_container",
        "tt_ad", "anythink", "applovin", "mbridge", "close_btn", "btn_close",
        "closebutton", "dismiss_button", "ksad_kwai", "ironsource", "vungle",
        "ad_container", "ad_header", "ad_frame"
    )

    // Gatilhos de engenharia social / pop-ups falsos
    private val scamKeywords = listOf(
        "não é compatível", "dispositivo android", "tente novamente",
        "para “atualizar” agora", "atualizar agora", "instalar o apk",
        "seu celular está", "vírus detectado", "limpar memória"
    )

    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""

        // Identificação por ID de infraestrutura de anúncios
        for (id in adViewIds) {
            if (viewId.contains(id)) return true
        }

        // Identificação por texto direto de publicidade
        if (text.isNotBlank()) {
            for (keyword in adKeywords) {
                if (text == keyword || text.contains(keyword)) return true
            }
            for (scam in scamKeywords) {
                if (text.contains(scam)) return true
            }
        }
        return false
    }

    fun isCloseOrSkipButton(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (!node.isClickable) return false

        // Detecção por ID de botão de fechar/pular
        for (id in adViewIds) {
            if (viewId.contains("close") || viewId.contains("skip") || viewId.contains("dismiss")) {
                return true
            }
        }

        // Detecção por texto de botão
        for (keyword in actionKeywords) {
            if (text == keyword || text.contains(keyword) || viewId.contains(keyword)) {
                return true
            }
        }
        return false
    }
}
