package com.nikhil.try_out_furnitureapp

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Toast
import androidx.annotation.ContentView
import androidx.annotation.RequiresApi
import com.google.ar.sceneform.ArSceneView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PhotoSaver(private val activity: Activity) {

    private fun generateFilename(): String? {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.absolutePath +
                "/TryOutFurniture/${date}_screenshot.jpg"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBitmapToGallery(bmp: Bitmap) {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${date}_screenshot.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/TryOutFurniture")
        }
        val uri = activity.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        activity.contentResolver.openOutputStream(uri ?: return).use { outputStream ->
            outputStream?.let {

                try {
                    saveDataToGallery(bmp, outputStream)
                } catch (e: IOException) {
                    Toast.makeText(activity, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmapToGallery(bmp: Bitmap, filename: String) {
        val out = File(filename)
        if (!out.parentFile.exists()) {
            out.parentFile.mkdirs()
        }
        try {
            val outputStream = FileOutputStream(filename)
            saveDataToGallery(bmp, outputStream)

        } catch (e: IOException) {
            Toast.makeText(activity, "Failed to save", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun takePhoto(arSceneView: ArSceneView) {
        val bmp =
            Bitmap.createBitmap(arSceneView.width, arSceneView.height, Bitmap.Config.ARGB_8888)
        val handlerThread = HandlerThread("PixelCopyThread")
        handlerThread.start()

        PixelCopy.request(arSceneView, bmp, { result ->

            if (result == PixelCopy.SUCCESS) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    val filename = generateFilename()
                    saveBitmapToGallery(bmp, filename ?: return@request)
                }
                else {
                    saveBitmapToGallery(bmp)
                }

                activity.runOnUiThread {
                    Toast.makeText(activity, "SuccessFully Took Photo!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activity, "Failed to Took Photo.", Toast.LENGTH_SHORT).show()

            }
            handlerThread.quitSafely()
        }, Handler(handlerThread.looper))
    }

    private fun saveDataToGallery(bmp: Bitmap, outputStream: OutputStream) {
        val outputData = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputData)
        outputData.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
    }
}