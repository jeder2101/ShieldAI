package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class ShieldService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Lógica de monitoramento do serviço
    }

    override fun onInterrupt() {
        // Chamado quando o serviço é interrompido
    }
}
