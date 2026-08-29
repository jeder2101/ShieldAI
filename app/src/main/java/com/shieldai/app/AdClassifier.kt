package com.shieldai.app

import android.view.accessibility.AccessibilityNodeInfo

object AdClassifier {

    // Termos e sinais que indicam anúncios contextuais ou feeds patrocinados
    private val adTerms = listOf(
        "patrocinado", "sponsored", "anúncio", "publicidade", 
        "promovido", "promoted", "ad ", "ads ", "saiba mais", 
        "instalar agora", "comprar agora", "shop now", "install now"
    )

    // Termos de botões de fechamento/pulo
    private val actionTerms = listOf(
        "pular", "skip", "fechar", "close", "dismiss"
    )

    /**
     * Analisa um nó da tela e retorna VERDADEIRO se for classificado como Publicidade.
     */
    fun isAdElement(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        
        if (text.isBlank()) return false

        // 1. Verificação de termos diretos de patrocínio ou anúncio
        for (term in adTerms) {
            if (text.contains(term)) {
                return true
            }
        }
        return false
    }

    /**
     * Verifica se o elemento é um botão de ação para fechar/pular anúncio.
     */
    fun isCloseOrSkipButton(node: AccessibilityNodeInfo): Boolean {
        val text = (node.text?.toString() ?: node.contentDescription?.toString())?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (!node.isClickable) return false

        for (term in actionTerms) {
            if (text.contains(term) || viewId.contains(term)) {
                return true
            }
        }
        return false
    }
}
