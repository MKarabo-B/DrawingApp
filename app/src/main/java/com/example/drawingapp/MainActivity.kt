package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.util.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var brushButton: ImageButton
    private lateinit var drawingView: DrawingView

    private lateinit var purpleButton: ImageButton
    private lateinit var blueButton: ImageButton
    private lateinit var redButton: ImageButton
    private lateinit var orangeButton: ImageButton
    private lateinit var greenButton: ImageButton

    private lateinit var undoButton: ImageButton

    private lateinit var colorPickerButton: ImageButton

    private lateinit var galleryButton: ImageButton

    private lateinit var saveButton: ImageButton
    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        findViewById<ImageView>(R.id.gallery_image).setImageURI(result.data?.data)
    }

    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE ||
                        permissionName == Manifest.permission.READ_MEDIA_IMAGES
                    ) {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                        val pickIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)
                    } else if (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                        CoroutineScope(IO).launch {
                            saveImage(getBitMapFromView(findViewById(R.id.constraint_l3)))
                        }
                    }
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE ||
                        permissionName == Manifest.permission.READ_MEDIA_IMAGES
                    ) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        brushButton = findViewById(R.id.brush_button)

        purpleButton = findViewById(R.id.purple_button)
        blueButton = findViewById(R.id.blue_button)
        redButton = findViewById(R.id.red_button)
        greenButton = findViewById(R.id.green_button)
        orangeButton = findViewById(R.id.orange_button)
        undoButton = findViewById(R.id.undo_button)
        colorPickerButton = findViewById(R.id.color_picker_button)
        galleryButton = findViewById(R.id.gallery_button)
        saveButton = findViewById(R.id.save_button)


        drawingView = findViewById(R.id.drawing_view)
        drawingView.changeBrushSize(23.toFloat())

        brushButton.setOnClickListener {
            showBrushChooserDialog()
        }

        purpleButton.setOnClickListener(this)
        orangeButton.setOnClickListener(this)
        blueButton.setOnClickListener(this)
        greenButton.setOnClickListener(this)
        redButton.setOnClickListener(this)
        undoButton.setOnClickListener(this)
        colorPickerButton.setOnClickListener(this)
        galleryButton.setOnClickListener(this)
        saveButton.setOnClickListener(this)
    }

    private fun showBrushChooserDialog() {
        val brushDialog = Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialogue_brush)
        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_seekbar)
        val showProgressTv = brushDialog.findViewById<TextView>(R.id.dialog_text_view_progress)



        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                p1: Int,
                p2: Boolean
            ) {

                drawingView.changeBrushSize(seekBar.progress.toFloat())

                showProgressTv.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        brushDialog.show()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.purple_button -> {
                drawingView.setColor("#AA66CC")
            }

            R.id.red_button -> {
                drawingView.setColor(("#FF4444"))
            }

            R.id.green_button -> {
                drawingView.setColor(("#99CC00"))
            }

            R.id.blue_button -> {
                drawingView.setColor(("#33B5E5"))
            }

            R.id.orange_button -> {
                drawingView.setColor(("#FFBB33"))
            }

            R.id.undo_button -> {
                drawingView.undoPath()
            }

            R.id.color_picker_button -> {
                showColorPickerDialog()
            }

            R.id.gallery_button -> {
                if (isReadPermissionGranted()) {
                    val pickIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    requestStoragePermission()
                }

            }

            R.id.save_button -> {
                if (isWritePermissionGranted()) {
                    val layout = findViewById<ConstraintLayout>(R.id.constraint_l3)
                    val bitmap = getBitMapFromView(layout)
                    CoroutineScope(IO).launch {
                        saveImage(bitmap)
                    }
                } else {
                    requestStoragePermission()
                }
            }
        }
    }

    private fun isReadPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isWritePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // On Android 13+, WRITE_EXTERNAL_STORAGE is not needed for saving media
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showColorPickerDialog() {
        val dialog =
            AmbilWarnaDialog(this, Color.GREEN, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {

                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    drawingView.setColor(color)
                }

            })
        dialog.show()
    }

    private fun requestStoragePermission() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val shouldShowRationale = permissionsToRequest.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }

        if (shouldShowRationale) {
            showRationaleDialog(permissionsToRequest.toTypedArray())
        } else {
            requestPermission.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun showRationaleDialog(permissions: Array<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage permission")
            .setMessage("We need this permission in order to access the internal storage")
            .setPositiveButton(R.string.dialog_yes) { dialog, _ ->
                requestPermission.launch(permissions)
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitMapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    private suspend fun saveImage(bitmap: Bitmap) {
        val root =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File("$root/saved_images")
        myDir.mkdir()
        val generator = Random()

        var n = 10000
        n = generator.nextInt(n)
        val outPutFile = File(myDir, "Images-$n.jpg")
        if (outPutFile.exists()) {
            outPutFile.delete()
        } else {
            try {
                val out = FileOutputStream(outPutFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.stackTrace
            }
            withContext(Main) {
                Toast.makeText(
                    this@MainActivity,
                    "${outPutFile.absoluteFile} saved!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
