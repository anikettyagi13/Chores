package com.example.chores.utils

import android.app.ProgressDialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import java.io.ByteArrayOutputStream
import java.io.File


class imageProcessor() {
    fun imageProcessor(uri: Uri, cr : ContentResolver, quality:Int): ByteArray {
        val f = File(uri.path)
        val size = f.length()
        val inputStream = cr.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val baos = ByteArrayOutputStream()
        if(size<300000) bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        else if(size<500000) bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos)
        else if(size>500000) bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos)
//        return Uri.parse(path)
        return baos.toByteArray()
    }
    fun imageUploader(UUID:String,id:String,pathname:String, data:ByteArray, pb:ProgressDialog,uploader:(postUUID:String,id:String, uri:String, pb: ProgressDialog)->Any):String {
        val mStorageRef: StorageReference? = com.google.firebase.storage.FirebaseStorage.getInstance().getReference();
        val riversRef: StorageReference = mStorageRef!!.child("$pathname")
        var myUri :String = "useless"
        riversRef.putBytes(data).addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            pb.setMessage("Percentage: "+ progress)
            Log.i("message image", "Upload is $progress% done")
        }.continueWith {
            if (!it.isSuccessful) {
                pb.dismiss()
                it.exception?.let { t ->
                    throw t
                }
            }
            riversRef.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful) {
                it.result!!.addOnSuccessListener { task ->
                    myUri = task.toString()
                    print("$myUri")
                    uploader(UUID,id,myUri,pb)
                }
            }
        }.addOnFailureListener {
            pb.dismiss()
        }
        return myUri
    }
}