package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class ShieldService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "App"
        val textList = event.text

        if (textList != null && textList.isNotEmpty()) {
            val textContent = textList.toString()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    applicationContext,
                    "[$packageName]: $textContent",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onInterrupt() {
    }
}
