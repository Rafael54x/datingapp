package com.example.datingapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.databinding.ActivityEditProfileBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var imageView: ImageView
    private lateinit var tvResult: TextView
    private lateinit var btnPickImage: Button

    // TFLite Variables
    private lateinit var tflite: Interpreter
    private val MODEL_FILE = "model.tflite"
    private val IMG_SIZE = 300
    private val CLASS_NAMES = listOf("Deepfakes", "Face2Face", "FaceShifter", "FaceSwap", "NeuralTedxtures", "Ori")
    private val NUM_CLASSES = CLASS_NAMES.size
    private val TAG = "EditProfileActivity"

    // Modern ActivityResultLauncher for picking images
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult

            try {
                // Get the bitmap and ensure it's converted to ARGB_8888
                val originalBitmap = uriToBitmap(imageUri)
                val argb8888Bitmap = convertToARGB8888(originalBitmap)

                // Display the image
                imageView.setImageBitmap(argb8888Bitmap)

                // Run TFLite inference
                runInference(argb8888Bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image from URI: ${e.message}")
                tvResult.text = "Error: Failed to load image."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Views.
        // Using findViewById as a workaround for a view binding generation issue.
        imageView = findViewById(R.id.profile_image)
        tvResult = findViewById(R.id.tv_result)
        btnPickImage = findViewById(R.id.btn_pick_image)

        initializeTfLite()

        btnPickImage.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun runInference(bitmap: Bitmap) {
        // 1. Image Preprocessing
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        var tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)

        // 2. Define Output Buffer
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, NUM_CLASSES), DataType.FLOAT32)

        // 3. Run Inference
        try {
            tflite.run(tImage.buffer, outputBuffer.buffer.rewind())
        } catch (e: Exception) {
            Log.e("TFLite", "Inference failed: ${e.message}")
            tvResult.text = "Error during prediction!"
            return
        }

        // 4. Post-processing
        val outputArray = outputBuffer.floatArray
        val predictedClassIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: 0
        val confidence = outputArray[predictedClassIndex]
        val predictedClass = CLASS_NAMES[predictedClassIndex]

        // 5. Display Result
        val resultText = """Predicted: $predictedClass
Confidence: ${"%.3f".format(confidence)}"""
        tvResult.text = resultText
        Log.i("Prediction", resultText)
    }

    private fun convertToARGB8888(bitmap: Bitmap): Bitmap {
        if (bitmap.config == Bitmap.Config.ARGB_8888) {
            return bitmap
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun initializeTfLite() {
        try {
            val tfliteModel = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)
            tflite = Interpreter(tfliteModel, options)
            Log.d("TFLite", "TFLite Model loaded successfully.")
        } catch (e: Exception) {
            Log.e("TFLite", "Failed to load model: ${e.message}")
            tvResult.text = "Error: Failed to load ML model!"
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
    }
}
