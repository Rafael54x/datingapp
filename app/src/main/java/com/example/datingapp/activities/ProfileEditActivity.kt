
package com.example.datingapp.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.databinding.ActivityEditProfileBinding
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.User
import com.example.datingapp.models.YearPreferences
import com.example.datingapp.utils.SharedPrefManager
import com.google.android.material.chip.Chip
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var binding: ActivityEditProfileBinding

    // TFLite Variables from example
    private lateinit var tflite: Interpreter
    private val MODEL_FILE = "model.tflite"
    private val IMG_SIZE = 300
    private val CLASS_NAMES = listOf("Deepfakes", "Face2Face", "FaceShifter", "FaceSwap", "NeuralTextures", "Ori")
    private val NUM_CLASSES = CLASS_NAMES.size
    private val TAG = "ProfileEditActivity"
    private var lastPredictedClass: String? = null

    // Launcher for picking an image from the gallery
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPrefManager = SharedPrefManager(this)

        initializeTfLite()
        setupImagePicker()
        setupClickListeners()
        setupDropdowns()
        loadUserData()
    }

    private fun initializeTfLite() {
        try {
            val tfliteModel = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)
            tflite = Interpreter(tfliteModel, options)
            Log.d(TAG, "TFLite Model loaded successfully.")
            Toast.makeText(this, "ML Model Initialized.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: ${e.message}")
            binding.tvResult.text = "Error: Failed to load ML model!"
            Toast.makeText(this, "Error initializing ML model.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data ?: return@registerForActivityResult
                try {
                    val originalBitmap = uriToBitmap(imageUri)
                    val argb8888Bitmap = convertToARGB8888(originalBitmap)

                    binding.profileImage.setImageBitmap(argb8888Bitmap)
                    runInference(argb8888Bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image from URI: ${e.message}")
                    Toast.makeText(this, "Error: Failed to load image.", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun convertToARGB8888(bitmap: Bitmap): Bitmap {
        if (bitmap.config == Bitmap.Config.ARGB_8888) {
            return bitmap
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun runInference(bitmap: Bitmap) {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        var tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)

        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, NUM_CLASSES), DataType.FLOAT32)

        try {
            tflite.run(tImage.buffer, outputBuffer.buffer.rewind())
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed: ${e.message}")
            binding.tvResult.text = "Error during prediction!"
            return
        }

        val outputArray = outputBuffer.floatArray
        val predictedClassIndex = outputArray.indices.maxByOrNull { outputArray[it] } ?: 0
        val confidence = outputArray[predictedClassIndex]
        val predictedClass = CLASS_NAMES[predictedClassIndex]
        this.lastPredictedClass = predictedClass

        val resultText = """Predicted: $predictedClass
Confidence: ${"%.3f".format(confidence)}"""
        binding.tvResult.text = resultText
        Log.i("Prediction", resultText)
    }

    private fun setupClickListeners() {
        binding.save.setOnClickListener { saveUserData() }
        binding.addMajorPreferenceButton.setOnClickListener { addMajorPreference() }
        binding.btnPickImage.setOnClickListener { pickImageFromGallery() }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun setupDropdowns() {
        val genders = Gender.values().map { it.displayName }
        binding.editGender.setAdapter(createArrayAdapter(genders))

        val majors = Jurusan.values().map { it.displayName }
        binding.editMajor.setAdapter(createArrayAdapter(majors))
        binding.addMajorPreference.setAdapter(createArrayAdapter(majors))

        val yearPrefs = YearPreferences.values().map { it.displayName }
        binding.editRange.setAdapter(createArrayAdapter(yearPrefs))

        binding.editGenderPreference.isEnabled = false
    }

    private fun loadUserData() {
        val user = sharedPrefManager.getUser()
        user?.let {
            binding.editUsername.setText(it.username)
            binding.editBio.setText(it.bio)
            binding.editFullname.setText(it.name)
            binding.editAge.setText(it.age)
            binding.editSchoolyear.setText(it.schoolyear)
            binding.editEmail.setText(it.email)
            binding.editPassword.setText(it.password)
            binding.editGender.setText(it.gender?.displayName, false)
            binding.editMajor.setText(it.major?.displayName, false)
            binding.editGenderPreference.setText(if (it.gender == Gender.M) Gender.F.displayName else Gender.M.displayName, false)
            binding.editRange.setText(it.preference.yearPreferences?.displayName, false)
            binding.majorPreferencesChipGroup.removeAllViews()
            it.preference.majorPreferences?.forEach { major -> addMajorChip(major.displayName) }
        }
    }

    private fun addMajorPreference() {
        val majorName = binding.addMajorPreference.text.toString()
        if (majorName.isNotBlank() && Jurusan.values().any { it.displayName == majorName }) {
            val isAlreadyAdded = (0 until binding.majorPreferencesChipGroup.childCount).any {
                (binding.majorPreferencesChipGroup.getChildAt(it) as Chip).text.toString() == majorName
            }
            if (!isAlreadyAdded) {
                addMajorChip(majorName)
                binding.addMajorPreference.text.clear()
            } else {
                Toast.makeText(this, "Major already added.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select a valid major.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        if (lastPredictedClass != null && lastPredictedClass != "Ori") {
            Toast.makeText(this, "Cannot save profile. The uploaded photo is not original.", Toast.LENGTH_LONG).show()
            return
        }

        val currentUser = sharedPrefManager.getUser()
        if (currentUser != null) {
            val selectedMajors = mutableListOf<Jurusan>()
            for (i in 0 until binding.majorPreferencesChipGroup.childCount) {
                val chip = binding.majorPreferencesChipGroup.getChildAt(i) as Chip
                Jurusan.values().find { it.displayName == chip.text.toString() }?.let {
                    selectedMajors.add(it)
                }
            }

            val updatedPreferences = currentUser.preference.copy(
                gender = if (binding.editGenderPreference.text.toString() == "Female") Gender.F else Gender.M,
                yearPreferences = YearPreferences.values().find { it.displayName == binding.editRange.text.toString() },
                majorPreferences = selectedMajors
            )

            val updatedUser = currentUser.copy(
                username = binding.editUsername.text.toString(),
                bio = binding.editBio.text.toString(),
                name = binding.editFullname.text.toString(),
                age = binding.editAge.text.toString(),
                schoolyear = binding.editSchoolyear.text.toString(),
                email = binding.editEmail.text.toString(),
                password = binding.editPassword.text.toString(),
                gender = Gender.values().find { it.displayName == binding.editGender.text.toString() },
                major = Jurusan.values().find { it.displayName == binding.editMajor.text.toString() },
                preference = updatedPreferences
            )

            sharedPrefManager.saveUser(updatedUser)
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addMajorChip(majorName: String) {
        val chip = Chip(this).apply {
            text = majorName
            isCloseIconVisible = true
            setOnCloseIconClickListener { binding.majorPreferencesChipGroup.removeView(this) }
        }
        binding.majorPreferencesChipGroup.addView(chip)
    }

    private fun createArrayAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
    }
}
