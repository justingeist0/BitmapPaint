package com.fantasma.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var mImageButtonCurrentPaint: ImageButton
    private lateinit var viewModel: MainActivityViewModel
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = MainActivityViewModel(application)

        drawing_canvas.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint = ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_selected))

        ib_brush.setOnClickListener {
            showBrushSizeChooseDialog()
        }

        ib_gallery.setOnClickListener {
            if(isReadStorageAllowed()) {
                val pickPhotoIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }

        ib_undo.setOnClickListener {
            drawing_canvas.undo()
        }

        ib_save.setOnClickListener {
            if(isReadStorageAllowed()) {
                viewModel.saveDrawing(getBitmapFromView(drawing_view_container), externalCacheDir!!)
            } else {
                requestStoragePermission()
            }
        }

        viewModel.showSaveLoad.observe(this, Observer {showSaveLoad ->
            println("meow at $showSaveLoad")
            if(showSaveLoad) {
                showProgressDialog()
            } else {
                cancelProgressDialog()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == GALLERY) {
                try {
                    if(data?.data != null) {
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    } else {
                        showImageError()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showImageError()
                }
            }
        }
    }

    private fun showImageError() {
        Toast.makeText(this, "Image not compatible", Toast.LENGTH_SHORT).show()
    }

    private fun showBrushSizeChooseDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        val currentBrushSize = drawing_canvas.getBrushSize()

        val brushSizeImage = brushDialog.iv_brush_size
        val setBrushDimens = {size: Float ->
            val wh = drawing_canvas.convertToDP(size).toInt()
            val newDimens = LinearLayout.LayoutParams(wh, wh)
            brushSizeImage.layoutParams = newDimens
        }
        setBrushDimens(currentBrushSize)

        val slider = brushDialog.slider
        slider.value = currentBrushSize
        slider.addOnChangeListener { _, value, _ ->
            setBrushDimens(value)
        }

        brushDialog.setOnCancelListener {
            drawing_canvas.setSizeForBrush(slider.value)
        }

        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if(view !== mImageButtonCurrentPaint) {
            mImageButtonCurrentPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_normal))

            mImageButtonCurrentPaint = view as ImageButton
            mImageButtonCurrentPaint.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_selected))

            val newColor = mImageButtonCurrentPaint.tag.toString()
            drawing_canvas.setColor(newColor)
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
            STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                permissionDeniedToast()
            }
        }
    }

    private fun permissionDeniedToast() {
        Toast.makeText(this, "We need permission to open your gallery", Toast.LENGTH_LONG).show()
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

    private fun showProgressDialog() {
        mProgressDialog = Dialog(this@MainActivity)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun cancelProgressDialog() {
        mProgressDialog?.dismiss()
        MediaScannerConnection.scanFile(this@MainActivity, arrayOf(), null) {
                path, uri -> val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"

            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}