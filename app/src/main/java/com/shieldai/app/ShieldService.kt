package com.shieldai.app

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShieldService : AccessibilityService() {

    private val TAG = "ShieldAI"
    private val adKeywords = listOf("patrocinado", "anúncio", "promoted", "sponsored", "ad")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        scanNode(rootNode)
    }

    private fun scanNode(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        for (keyword in adKeywords) {
            if (text.contains(keyword) || desc.contains(keyword)) {
                Log.d(TAG, "Anúncio detectado: $keyword no app ${node.packageName}")
                break
            }
        }

        for (i in 0 until node.childCount) {
            scanNode(node.getChild(i))
        }
    }

    override fun onInterrupt() {}
}
