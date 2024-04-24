package com.example.piracycheckapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.example.piracycheckapp.databinding.ActivityBarcodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        iniBc()
    }

    private fun iniBc(){
        try {
            barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_128)
                .build()
            cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build()
        } catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error initializing barcode scanner", Toast.LENGTH_SHORT).show()
            return
        }

        binding.surfaceView!!.holder.addCallback(object: SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.surfaceView!!.holder)
                }catch (e:IOException){
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error starting camera", Toast.LENGTH_SHORT).show()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                    cameraSource.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle camera stop error
                    Toast.makeText(applicationContext, "Error stopping camera", Toast.LENGTH_SHORT).show()
                }
            }

        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                Toast.makeText(applicationContext, "barcode scanner has been stopped",
                    Toast.LENGTH_LONG).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val scannedBarcode = barcodes.valueAt(0).displayValue
                    checkBarcodeInDatabase(scannedBarcode)
                }
            }
        })
    }

    private fun checkBarcodeInDatabase(barcode: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://nodei.ssccglpinnacle.com/getKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to server for API 1", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Failed to get response from server for API 1", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val responseData = response.body?.string()
                        responseData?.let { parseApiResponse(it, barcode) }
                    }
                }
            }
        })
    }

    // Inside the parseApiResponse function
    private fun parseApiResponse(responseData: String, scannedBarcode: String) {
        val jsonArray = JSONArray(responseData)
        var found = false
        var message = ""
        var key = ""

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val batchId = jsonObject.getString("batchId")
            val keyValuePairs = jsonObject.getJSONArray("keyValuePairs")

            for (j in 0 until keyValuePairs.length()) {
                val pairObject = keyValuePairs.getJSONObject(j)
                val k = pairObject.getString("key")
                val value = pairObject.getString("value")

                if (value == scannedBarcode) {
                    found = true
                    message = "Scanned barcode belongs to Pinnacle Database so the book is original with:\nBatch ID --> $batchId\nKey --> $k"
                    key = k // Assign the key to the 'key' variable
                    break
                }
            }
            if (found) {
                break
            }
        }

        if (!found) {
            message = "Scanned barcode does not belong to the database."
            runOnUiThread {
                binding.txtMessage?.text = message
                binding.txtMessage?.visibility = View.VISIBLE
            }
        } else {
            checkSecondApi(key, message) // Pass the 'key' to the checkSecondApi function
        }
    }


    private fun checkSecondApi(key: String, message: String) {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("searchValue", key)

        val requestBody = json.toString()

        val request = Request.Builder()
            .url("https://nodei.ssccglpinnacle.com/searchBarr1")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to connect to server for API 2", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Failed to get response from server for API 2", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val responseData = response.body?.string()
                        responseData?.let { parseSecondApiResponse(it, message) }
                    }
                }
            }
        })
    }

    private fun parseSecondApiResponse(responseData: String, originalMessage: String) {
        val jsonObject = JSONObject(responseData)
        val resultValue = jsonObject.optString("result")

        val message = if (resultValue == "Not Verified") {
            "$originalMessage\n Barcode is not from our Database!!!!!!!!!!"
        } else {
            "$originalMessage\n Belongs To \n $resultValue \n Verified "
        }

        Log.d("Response", message) // Log the response data along with a message
        runOnUiThread {
            binding.txtMessage?.text = message
            binding.txtMessage?.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        iniBc()
    }
}

