
package com.example.glaucoscan

import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var cardView: CardView
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var textView: TextView
    private val options = arrayOf<CharSequence>("Camera", "Gallery", "Cancel")
    private var selectedImage: Uri? = null // Add this variable to store the image URI
    private var base64String= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // This return is unnecessary
        }

        cardView = findViewById(R.id.cardView)
        imageView = findViewById(R.id.imageView)
        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

        // Handle potential NullPointerException
        if (cardView != null) {
            cardView.setOnClickListener {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Select Image")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                    if (options[which] == "Camera") {
                        val takePic = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePic, 0)
                    } else if (options[which] == "Gallery") {
                        val gallery = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(gallery, 1)
                    } else {
                        dialog.dismiss()
                    }
                })
                builder.show()
            }
        }

        button.setOnClickListener {
            sendData()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                0 -> {
                    // Handle camera result
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        imageView.setImageBitmap(imageBitmap)
                        // Convert to Base64
                        base64String = convertBitmapToBase64(imageBitmap)
                        selectedImage = data?.data // Set the selectedImage URI
                        //uploadFileToServer()
                    }
                }

                1 -> {
                    // Handle gallery result
                    selectedImage = data?.data // Set the selectedImage URI
                    if (selectedImage != null) {
                        imageView.setImageURI(selectedImage)
                        // Convert to Base64
                        base64String = convertUriToBase64(selectedImage!!)
                        //uploadFileToServer()
                    }
                }
            }
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    private fun convertUriToBase64(uri: Uri): String {
        val bitmap = getBitmapFromUri(uri)
        return convertBitmapToBase64(bitmap)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return bitmap
    }

    private fun sendData() {
        // Create the Retrofit service
        val httpService: HttpService = RetrofitBuilder.getClient().create(HttpService::class.java)

        // Create the FileModel object with the base64 encoded image
        val fileModel = FileModel(base64String)

        // Make the API call
        val call: Call<FileModel> = httpService.callUploadAPI(fileModel)
        call.enqueue(object : Callback<FileModel> {
            override fun onResponse(call: Call<FileModel>, response: Response<FileModel>) {
                if (response.isSuccessful) {
                    // Access the FileModel object directly
                    val responseModel = response.body()
                    if (responseModel != null) {
                        // Parse the body as a JSON array
                        val bodyJson = responseModel.getBody()
                        try {
                            val jsonArray = JSONArray(bodyJson)
                            if (jsonArray.length() > 0) {
                                val jsonObject = jsonArray.getJSONObject(0)
                                val name = jsonObject.optString("Name", "N/A")
                                val confidence = jsonObject.optDouble("Confidence", 0.0)

                                // Display in the desired format
                                textView.text = """
                                Name: $name
                                Confidence: $confidence
                            """.trimIndent()
                            } else {
                                textView.text = "No data found in response"
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            textView.text = "Failed to parse response: ${e.message}"
                        }
                    } else {
                        textView.text = "Response was successful but body is null"
                    }
                } else {
                    textView.text = "Request failed with status: ${response.code()} - ${response.message()}"
                }
            }

            override fun onFailure(call: Call<FileModel>, t: Throwable) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Error")
                    .setMessage("Failed to upload: ${t.message}")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        })
    }




}