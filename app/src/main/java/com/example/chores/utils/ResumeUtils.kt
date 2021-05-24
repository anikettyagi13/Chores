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

class ResumeUtils{
    public fun uploadResume(path:String, uri: Uri,cr:ContentResolver,pb:ProgressDialog,profile_pic:String,uploader:(url:String,pb:ProgressDialog,profile_pic:String)->Unit){
//        val inputStream = cr.openInputStream(uri)
//        val bitmap = BitmapFactory.decodeStream(inputStream)
//        val baos = ByteArrayOutputStream()
//        val data = baos.toByteArray()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        val mStorageRef: StorageReference? = com.google.firebase.storage.FirebaseStorage.getInstance().getReference();
        val riversRef: StorageReference = mStorageRef!!.child("$path"+".pdf")
        riversRef.putFile(uri).addOnProgressListener { (bytesTransferred, totalByteCount) ->
            val progress = (100.0 * bytesTransferred) / totalByteCount
            pb.setMessage("Percentage: "+ progress)
            pb.setTitle("Uploading Resume")
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
                    task.toString()
                    uploader(task.toString(),pb,profile_pic)
                }
            }
        }.addOnFailureListener {
            pb.dismiss()
        }
    }
}