package com.vayu.android

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvCurrentTask: TextView
    private lateinit var btnOpenSettings: Button
    private lateinit var etTask: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnToggleService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvCurrentTask = findViewById(R.id.tv_current_task)
        btnOpenSettings = findViewById(R.id.btn_open_settings)
        etTask = findViewById(R.id.et_task)
        btnSubmit = findViewById(R.id.btn_submit)
        btnToggleService = findViewById(R.id.btn_toggle_service)

        btnOpenSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnToggleService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnSubmit.setOnClickListener {
            val task = etTask.text.toString()
            if (task.isNotBlank()) {
                submitTask(task)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkServiceStatus()
    }

    private fun checkServiceStatus() {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val isEnabled = enabledServices.any { it.resolveInfo.serviceInfo.packageName == packageName }

        if (isEnabled) {
            tvStatus.text = "● RUNNING"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            btnOpenSettings.visibility = View.GONE
            btnSubmit.isEnabled = true
        } else {
            tvStatus.text = "● OFFLINE"
            tvStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
            btnOpenSettings.visibility = View.VISIBLE
            btnSubmit.isEnabled = false
        }
    }

    private fun submitTask(task: String) {
        thread {
            try {
                val url = URL("http://localhost:8082/task/submit")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = JSONObject()
                json.put("task", task)

                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    runOnUiThread {
                        etTask.text.clear()
                        tvCurrentTask.text = "Task queued: $task"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
