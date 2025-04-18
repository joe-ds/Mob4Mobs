package com.example.mob4mobs

import android.content.Context
import java.io.File
import java.io.FileOutputStream

class Utils {
    fun setupChallengeFiles(context: Context) {
        val notesDir = File(context.filesDir, "preload")
        val markerFile = File(notesDir, "Alice")

        if (markerFile.exists()) {
            // Already set up
            return
        }
        copyAssetsToFilesDir(context, "preload", context.filesDir)
    }

    private fun copyAssetsToFilesDir(context: Context, assetPath: String = "preload", destPath: File = context.filesDir) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return

        for (fileName in files) {
            val fullAssetPath = "$assetPath/$fileName"
            val outFile = File(destPath, fileName)

            if ((assetManager.list(fullAssetPath)?.isNotEmpty()) == true) {
                outFile.mkdirs()
                copyAssetsToFilesDir(context, fullAssetPath, outFile)
            } else {
                assetManager.open(fullAssetPath).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}