package com.julianb183.questterminal

object EmulatorNative {
    init {
        System.loadLibrary("pcvr_qemu_runner")
    }

    external fun nativeStart(imagePath: String, ramMb: Int, runtimeDir: String): String
    external fun nativeStop(): String
}
