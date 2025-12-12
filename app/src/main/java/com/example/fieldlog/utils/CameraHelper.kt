package com.example.fieldlog.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(private val context: Context) {
    
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
    
    private var currentPhotoUri: Uri? = null
    private var currentPhotoFile: File? = null
    
    fun getTakePictureIntent(context: Context): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        
        // Crear archivo para la foto
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
        
        photoFile?.also {
            currentPhotoFile = it
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
            currentPhotoUri = photoURI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return intent
        }
        
        return null
    }
    
    @Deprecated("Use getTakePictureIntent instead", ReplaceWith("getTakePictureIntent"))
    fun dispatchTakePictureIntent(activity: Activity): Uri? {
        val intent = getTakePictureIntent(activity)
        if (intent != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            return currentPhotoUri
        }
        return null
    }
    
    @Throws(Exception::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
    
    fun getCurrentPhotoPath(): String? {
        return currentPhotoFile?.absolutePath
    }
    
    fun getCurrentPhotoUri(): Uri? {
        return currentPhotoUri
    }
}

