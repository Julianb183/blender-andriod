package com.julianb183.questterminal

import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import java.net.URI

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        urlInput = findViewById(R.id.urlInput)
        status = findViewById(R.id.status)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !isAllowed(request.url.toString())
            }
        }

        findViewById<Button>(R.id.loadButton).setOnClickListener { loadTrustedSite() }
        findViewById<Button>(R.id.windowsButton).setOnClickListener {
            startActivity(Intent(this, WindowsEmulatorActivity::class.java))
        }
        loadTrustedSite()
    }

    private fun loadTrustedSite() {
        val url = urlInput.text.toString().trim()
        if (!isAllowed(url)) {
            status.text = "Blocked: HTTPS URL required"
            Toast.makeText(this, "Only HTTPS websites are allowed", Toast.LENGTH_SHORT).show()
            return
        }
        status.text = "Loaded in trusted-site mode • native ADB bridge is not connected"
        webView.loadUrl(url)
    }

    private fun isAllowed(value: String): Boolean = try {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    } catch (_: Exception) { false }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
