package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.os.Handler
import android.os.Looper

class ShieldService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Configura o serviço diretamente no código sem precisar do arquivo XML
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_DEFAULT or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val packageName = it.packageName?.toString() ?: "App"
            val eventTexts = it.text.filterNotNull().joinToString(" ")

            if (eventTexts.isNotBlank()) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        applicationContext,
                        "[$packageName]: $eventTexts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onInterrupt() {}
}
