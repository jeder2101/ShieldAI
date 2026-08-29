package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ShieldService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventObject = event ?: return
        val packageName = eventObject.packageName?.toString() ?: "App"
        val textList = eventObject.text

        if (!textList.isNullOrEmpty()) {
            val eventTexts = textList.filterNotNull().joinToString(" ")
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
