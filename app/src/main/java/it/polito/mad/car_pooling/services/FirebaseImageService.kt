package it.polito.mad.car_pooling.services

import android.graphics.Bitmap
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

enum class folderType {
    USER,
    TRIP
}
/*
object FirebaseImageService {

    fun saveImage(imageBitmap: Bitmap, objectId: String, folder: folderType) {
        val folderName = when(folder) {
            folderType.USER -> "users/"
            folderType.TRIP -> "trips/"
        }
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val storage = Firebase.storage.reference
                .child("${folderName}${objectId}.jpg")
                .putBytes(data)
    }
}
 */