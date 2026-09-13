package com.julianb183.questterminal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.File

class WindowsEmulatorActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var memory: EditText
    private var imagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_windows_emulator)
        status = findViewById(R.id.emulatorStatus)
        memory = findViewById(R.id.memoryInput)

        findViewById<Button>(R.id.selectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, IMAGE_REQUEST)
        }
        findViewById<Button>(R.id.openBlender).setOnClickListener { openBlenderFile() }
        findViewById<Button>(R.id.startWindows).setOnClickListener { startEmulator() }
        findViewById<Button>(R.id.stopWindows).setOnClickListener {
            status.text = EmulatorNative.nativeStop()
        }
    }

    @Deprecated("Use Activity Result APIs when this prototype is modernized")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        if (requestCode == BLENDER_REQUEST) {
            val path = copyBlenderToPrivateStorage(uri)
            status.text = "Blender file opened:\n${path ?: "Could not copy file"}\n\nA Blender-compatible viewer or renderer is required to display it."
            return
        }
        if (requestCode != IMAGE_REQUEST) return
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) { }
        imagePath = copyImageToPrivateStorage(uri)
        status.text = "Guest image selected:\n${imagePath ?: "Could not copy image"}\n\nPress START to launch the ARM64 QEMU backend."
    }

    private fun openBlenderFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/octet-stream"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, BLENDER_REQUEST)
    }

    private fun copyBlenderToPrivateStorage(uri: Uri): String? {
        val name = "scene_" + System.currentTimeMillis() + ".blend"
        val target = File(filesDir, "blender/$name")
        target.parentFile?.mkdirs()
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.absolutePath
        } catch (error: Exception) {
            Toast.makeText(this, "Could not open Blender file: ${error.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun copyImageToPrivateStorage(uri: Uri): String? {
        val target = File(filesDir, "guest/Windows11-arm64.img")
        target.parentFile?.mkdirs()
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.absolutePath
        } catch (error: Exception) {
            Toast.makeText(this, "Could not copy guest image: ${error.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun startEmulator() {
        val ramMb = memory.text.toString().trim().ifEmpty { "4096" }.toIntOrNull()
        if (ramMb == null || ramMb !in 1024..4096) {
            Toast.makeText(this, "RAM must be between 1024 and 4096 MB", Toast.LENGTH_SHORT).show()
            return
        }
        val image = imagePath
        if (image == null) {
            status.text = "Select a legally obtained Windows 11 ARM64 image first."
            return
        }
        status.text = EmulatorNative.nativeStart(image, ramMb, applicationInfo.nativeLibraryDir)
    }

    companion object {
        private const val IMAGE_REQUEST = 41
        private const val BLENDER_REQUEST = 42
    }
}
