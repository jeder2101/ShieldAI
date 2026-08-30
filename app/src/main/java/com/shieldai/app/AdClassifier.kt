package com.shieldai.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    // Termos textuais universais de anúncios
    private val adKeywords = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", "promovido", "promoted", 
        "ad ", "ads ", "saiba mais", "instalar agora", "comprar agora", "shop now", 
        "install now", "baixar agora", "download now", "publicidad"
    )

    // NOVO: PALAVRAS-CHAVE E MARCAS DE BETS / JOGOS DE AZAR
    private val betKeywords = listOf(
        "bet", "bets", "aposte", "apostas", "aposta", "cassino", "casino",
        "tiger", "tigrinho", "aviator", "mines", "fortune", "roleta", "spin",
        "bônus de depósito", "ganhe até", "rodadas grátis", "deposite", "jackpot",
        "bet365", "betano", "blaze", "vai de bet", "estrelabet", "kTO", "pixbet",
        "parimatch", "1xbet", "novibet", "superbet", "777", "slot", "slots"
    )

    // Ações de fechamento/pulo
    private val actionKeywords = listOf(
        "pular", "skip", "fechar", "close", "dismiss", "x", "cancelar", "cancel"
    )

    // IDs de contêineres e SDKs de anúncios globais
    private val adViewIds = listOf(
        "ad_view", "banner_ad", "native_ad", "sponsor", "ads_container",
        "tt_ad", "anythink", "applovin", "mbridge", "close_btn", "btn_close",
        "closebutton", "dismiss_button", "ksad_kwai", "ironsource", "vungle",
        "ad_container", "ad_header", "ad_frame"
    )

    // Pop-ups falsos / Engenharia social
    private val scamKeywords = listOf(
        "não é compatível", "dispositivo android", "tente novamente",
        "para “atualizar” agora", "atualizar agora", "instalar o apk",
        "seu celular está", "vírus detectado", "limpar memória"
    )

    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""

        // 1. Identificação por ID de infraestrutura de anúncios
        for (id in adViewIds) {
            if (viewId.contains(id)) return true
        }

        // 2. Identificação por palavras-chave de PUBLICIDADE GERAL
        if (text.isNotBlank()) {
            for (keyword in adKeywords) {
                if (text == keyword || text.contains(keyword)) return true
            }
            // Check por pop-ups falsos
            for (scam in scamKeywords) {
                if (text.contains(scam)) return true
            }
        }

        // 3. NOVO: Identificação prioritária de ANÚNCIOS DE BETS
        if (text.isNotBlank()) {
            for (bet in betKeywords) {
                if (text.contains(bet)) return true
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
