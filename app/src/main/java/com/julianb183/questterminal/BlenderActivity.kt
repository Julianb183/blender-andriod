package com.julianb183.questterminal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.File

class BlenderActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blender)
        status = findViewById(R.id.blendStatus)
        findViewById<Button>(R.id.openBlend).setOnClickListener { chooseBlendFile() }
        findViewById<Button>(R.id.questTerminal).setOnClickListener {
            startActivity(Intent(this, WindowsEmulatorActivity::class.java))
        }
    }

    private fun chooseBlendFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/octet-stream"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, BLEND_REQUEST)
    }

    @Deprecated("Use Activity Result APIs when this prototype is modernized")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != BLEND_REQUEST || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val path = copyToPrivateStorage(uri)
        status.text = if (path == null) "Could not copy Blender file" else "Opened Blender file:\n$path\n\nStored locally and ready for a compatible viewer."
    }

    private fun copyToPrivateStorage(uri: Uri): String? {
        val target = File(filesDir, "blender/scene_${System.currentTimeMillis()}.blend")
        target.parentFile?.mkdirs()
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            target.absolutePath
        } catch (error: Exception) {
            Toast.makeText(this, "Could not open .blend: ${error.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    companion object { private const val BLEND_REQUEST = 77 }
}
