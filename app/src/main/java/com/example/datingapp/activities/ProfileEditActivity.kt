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
import com.bumptech.glide.Glide
import com.example.datingapp.databinding.ActivityEditProfileBinding
import com.example.datingapp.models.Gender
import com.example.datingapp.models.Jurusan
import com.example.datingapp.models.YearPreferences
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String

    // TFLite
    private lateinit var tflite: Interpreter
    private val MODEL_FILE = "model.tflite"
    private val IMG_SIZE = 300
    private val CLASS_NAMES = listOf("Deepfakes", "Face2Face", "FaceShifter", "FaceSwap", "NeuralTextures", "Ori")
    private val NUM_CLASSES = CLASS_NAMES.size
    private val TAG = "ProfileEditActivity"
    private var lastPredictedClass: String? = null
    private var selectedImageUri: Uri? = null
    private var currentPhotoUrl: String? = null

    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(this, "User not logged in. Redirecting...", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        userId = auth.currentUser!!.uid

        initializeTfLite()
        setupImagePicker()
        setupClickListeners()
        setupDropdowns()
        loadUserDataFromFirestore()
    }

    // --- Data Handling -- -

    private fun loadUserDataFromFirestore() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val data = doc.data ?: return@addOnSuccessListener

                binding.editUsername.setText(data["username"] as? String)
                binding.editBio.setText(data["bio"] as? String)
                binding.editFullname.setText(data["name"] as? String)
                binding.editAge.setText(data["age"] as? String)
                binding.editSchoolyear.setText(data["schoolyear"] as? String)
                binding.editEmail.setText(data["email"] as? String)

                (data["gender"] as? String)?.let { 
                    binding.editGender.setText(it, false)
                    binding.editGender.isEnabled = false // Gender cannot be changed
                }
                (data["major"] as? String)?.let { binding.editMajor.setText(it, false) }

                currentPhotoUrl = data["photoUrl"] as? String
                if (!currentPhotoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(currentPhotoUrl).into(binding.profileImage)
                }

                val preferences = data["preference"] as? Map<*, *>
                preferences?.let {
                    binding.editRange.setText(it["yearPreferences"] as? String, false)
                    binding.editGenderPreference.setText(it["gender"] as? String, false)
                    val majorPrefs = it["majorPreferences"] as? List<String>
                    binding.majorPreferencesChipGroup.removeAllViews()
                    majorPrefs?.forEach { majorName -> addMajorChip(majorName) }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        if (selectedImageUri != null && lastPredictedClass != "Ori") {
            Toast.makeText(this, "Cannot save profile. The uploaded photo is not original.", Toast.LENGTH_LONG).show()
            return
        }

        // Show loading
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.save.isEnabled = false

        if (selectedImageUri != null) {
            val file = uriToFile(selectedImageUri!!)
            if (file == null) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.save.isEnabled = true
                Toast.makeText(this, "Failed to process image file.", Toast.LENGTH_SHORT).show()
                return
            }
            uploadToCloudinary(
                file,
                cloudName = "drfuydppl",
                uploadPreset = "ml_default"
            ) { imageUrl ->
                runOnUiThread {
                    if (imageUrl != null) {
                        saveDataToFirestore(imageUrl)
                    } else {
                        binding.progressBar.visibility = android.view.View.GONE
                        binding.save.isEnabled = true
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            saveDataToFirestore(currentPhotoUrl)
        }
    }

    private fun saveDataToFirestore(imageUrl: String?) {
        val majorPrefs = (0 until binding.majorPreferencesChipGroup.childCount).map {
            (binding.majorPreferencesChipGroup.getChildAt(it) as Chip).text.toString()
        }

        val userProfileMap = mutableMapOf<String, Any>(
            "username" to binding.editUsername.text.toString(),
            "name" to binding.editFullname.text.toString(),
            "bio" to binding.editBio.text.toString(),
            "age" to binding.editAge.text.toString(),
            "schoolyear" to binding.editSchoolyear.text.toString(),
            "major" to binding.editMajor.text.toString(),
            "preference" to mapOf(
                "gender" to binding.editGenderPreference.text.toString(),
                "yearPreferences" to binding.editRange.text.toString(),
                "majorPreferences" to majorPrefs
            )
        )

        // Only update photo fields if a new image was selected
        if (selectedImageUri != null && imageUrl != null) {
            userProfileMap["photoUrl"] = imageUrl
            userProfileMap["photoVerified"] = (lastPredictedClass == "Ori")
        }

        firestore.collection("users").document(userId).update(userProfileMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = android.view.View.GONE
                binding.save.isEnabled = true
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.save.isEnabled = true
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Cloudinary Upload ---

    private fun uploadToCloudinary(
        file: File,
        cloudName: String,
        uploadPreset: String,
        callback: (String?) -> Unit
    ) {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .addFormDataPart("upload_preset", uploadPreset)
            .addFormDataPart("folder", "dating_app")
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(multipartBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Cloudinary upload failed: ", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "Cloudinary upload failed with code: ${response.code}")
                    callback(null)
                    return
                }
                try {
                    val json = JSONObject(response.body!!.string())
                    val imageUrl = json.optString("secure_url")
                    if (imageUrl.isNullOrEmpty()) {
                        callback(null)
                    } else {
                        callback(imageUrl)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Cloudinary response: ", e)
                    callback(null)
                }
            }
        })
    }

    // --- TFLite & Image Handling -- -

    private fun setupImagePicker() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data ?: return@registerForActivityResult
                selectedImageUri = imageUri
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

        val resultText = "Predicted: $predictedClass\nConfidence: ${"%.3f".format(confidence)}"
        binding.tvResult.text = resultText
        Log.i("Prediction", resultText)
    }

    // --- Boilerplate & Helpers -- -

    private fun initializeTfLite() {
        try {
            val tfliteModel = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)
            tflite = Interpreter(tfliteModel, options)
            Log.d(TAG, "TFLite Model loaded successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: ${e.message}")
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
    
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_image_for_upload.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: IOException) {
            Log.e(TAG, "Failed to convert Uri to File", e)
            null
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
        binding.genderInfoText.text = "Gender cannot be changed"

        val majors = Jurusan.values().map { it.displayName }
        binding.editMajor.setAdapter(createArrayAdapter(majors))
        binding.addMajorPreference.setAdapter(createArrayAdapter(majors))

        val schoolYears = listOf("2021", "2022", "2023", "2024", "2025")
        binding.editSchoolyear.setAdapter(createArrayAdapter(schoolYears))

        val yearPrefs = YearPreferences.values().map { it.displayName }
        binding.editRange.setAdapter(createArrayAdapter(yearPrefs))
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
