package com.fantasma.drawingapp
import android.app.Application
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivityViewModel(application: Application) : AndroidViewModel(application){

    private val _showSaveLoad = MutableLiveData<Boolean>()
    val showSaveLoad: LiveData<Boolean>
        get() = _showSaveLoad


    init {
        _showSaveLoad.value = false
    }

    fun saveDrawing(mBitmap: Bitmap, externalCacheDir: File?) {
        val scope = CoroutineScope(Job())

        scope.launch(Dispatchers.IO) {
            try {
                _showSaveLoad.postValue(true)
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val f = File(externalCacheDir!!.absoluteFile.toString()
                        + File.separator
                        + "DrawingApp_"
                        + System.currentTimeMillis()/1000
                        + ".png")

                yield()

                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _showSaveLoad.postValue(false)
            }
        }
    }



//
//    override fun onPostExecute(result: String?) {
//        super.onPostExecute(result)
//        cancelProgressDialog()
//        if(result?.isEmpty()!!) {
//            Toast.makeText(this@MainActivity, "Error storing image", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this@MainActivity, "Image saved on device", Toast.LENGTH_SHORT).show()
//        }
//
//        }
//    }



}