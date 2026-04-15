package com.vayu.android

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class VayuService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isTaskRunning = false
    private var currentGoal = ""
    private val actionHistory = JSONArray()
    private var stepCount = 0

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isTaskRunning) {
                pollForTask()
            }
            handler.postDelayed(this, 2000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        handler.post(pollRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    private fun pollForTask() {
        thread {
            try {
                val url = URL("http://localhost:8082/task/pending")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                if (conn.responseCode == 200) {
                    val response = InputStreamReader(conn.inputStream).readText()
                    val json = JSONObject(response)
                    if (json.has("task")) {
                        val task = json.getString("task")
                        startTask(task)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startTask(goal: String) {
        isTaskRunning = true
        currentGoal = goal
        stepCount = 0
        // Clear history
        while (actionHistory.length() > 0) {
            actionHistory.remove(0)
        }
        executeNextStep()
    }

    private fun executeNextStep() {
        if (stepCount >= 30) {
            finishTask("FAIL", "Max steps reached")
            return
        }

        takeScreenshot(0, mainExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(screenshot: ScreenshotResult) {
                val bitmap = Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                val b64 = bitmapToBase64(bitmap)
                bitmap?.recycle()
                screenshot.hardwareBuffer.close()

                val rootNode = rootInActiveWindow
                val uiTree = JSONArray()
                if (rootNode != null) {
                    traverseNode(rootNode, uiTree)
                }

                sendToBrain(b64, uiTree)
            }

            override fun onFailure(errorCode: Int) {
                finishTask("FAIL", "Screenshot failed: $errorCode")
            }
        })
    }

    private fun traverseNode(node: AccessibilityNodeInfo, array: JSONArray) {
        if (node.isVisibleToUser) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            val obj = JSONObject()
            obj.put("class", node.className)
            obj.put("text", node.text ?: "")
            obj.put("desc", node.contentDescription ?: "")
            obj.put("bounds", "[${rect.left},${rect.top}][${rect.right},${rect.bottom}]")
            obj.put("clickable", node.isClickable)
            obj.put("editable", node.isEditable)
            array.put(obj)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, array)
                child.recycle()
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap?): String {
        if (bitmap == null) return ""
        val baos = ByteArrayOutputStream()
        // Compress to save bandwidth
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun sendToBrain(screenshotB64: String, uiTree: JSONArray) {
        thread {
            try {
                val url = URL("http://localhost:8082/act")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val payload = JSONObject()
                payload.put("goal", currentGoal)
                payload.put("screenshot", screenshotB64)
                payload.put("ui_tree", uiTree)
                payload.put("history", actionHistory)

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

                if (conn.responseCode == 200) {
                    val response = InputStreamReader(conn.inputStream).readText()
                    val action = JSONObject(response)
                    actionHistory.put(action)
                    stepCount++
                    performAction(action)
                } else {
                    finishTask("FAIL", "Brain returned ${conn.responseCode}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finishTask("FAIL", "Brain connection error")
            }
        }
    }

    private fun performAction(action: JSONObject) {
        val type = action.optString("action")
        when (type) {
            "TAP" -> {
                val x = action.optInt("x").toFloat()
                val y = action.optInt("y").toFloat()
                dispatchGesture(createClick(x, y), null, null)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "LONG_PRESS" -> {
                val x = action.optInt("x").toFloat()
                val y = action.optInt("y").toFloat()
                dispatchGesture(createLongClick(x, y), null, null)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "SWIPE" -> {
                val x1 = action.optInt("x1").toFloat()
                val y1 = action.optInt("y1").toFloat()
                val x2 = action.optInt("x2").toFloat()
                val y2 = action.optInt("y2").toFloat()
                val duration = action.optInt("duration_ms", 500).toLong()
                dispatchGesture(createSwipe(x1, y1, x2, y2, duration), null, null)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "TYPE" -> {
                // Simplified typing using accessibility node if focused, or global action
                // In a real app, we might need to find the focused node and set text
                val text = action.optString("text")
                val root = rootInActiveWindow
                val focused = root?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                if (focused != null) {
                    val args = android.os.Bundle()
                    args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                    focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    focused.recycle()
                }
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "PRESS_BACK" -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "PRESS_HOME" -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "PRESS_RECENTS" -> {
                performGlobalAction(GLOBAL_ACTION_RECENTS)
                handler.postDelayed({ executeNextStep() }, 2000)
            }
            "OPEN_APP" -> {
                val pkg = action.optString("package")
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                handler.postDelayed({ executeNextStep() }, 4000)
            }
            "WAIT" -> {
                val ms = action.optInt("ms", 2000).toLong()
                handler.postDelayed({ executeNextStep() }, ms)
            }
            "DONE" -> {
                finishTask("DONE", action.optString("reason"))
            }
            "FAIL" -> {
                finishTask("FAIL", action.optString("reason"))
            }
            else -> {
                finishTask("FAIL", "Unknown action: $type")
            }
        }
    }

    private fun createClick(x: Float, y: Float): GestureDescription {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    private fun createLongClick(x: Float, y: Float): GestureDescription {
        val path = Path()
        path.moveTo(x, y)
        val stroke = GestureDescription.StrokeDescription(path, 0, 1000)
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    private fun createSwipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long): GestureDescription {
        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        val stroke = GestureDescription.StrokeDescription(path, 0, duration)
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    private fun finishTask(status: String, reason: String) {
        isTaskRunning = false
        thread {
            try {
                val url = URL("http://localhost:8082/task/result")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val payload = JSONObject()
                payload.put("goal", currentGoal)
                payload.put("status", status)
                payload.put("reason", reason)

                OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
