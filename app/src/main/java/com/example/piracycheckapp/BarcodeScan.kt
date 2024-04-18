package com.example.piracycheckapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import org.json.JSONObject
import java.io.IOException


open class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }



    private fun iniBc(){
        try {barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.CODE_128)
            .build()
            cameraSource = CameraSource.Builder(this,barcodeDetector)
                .setRequestedPreviewSize(1920,1080)
                .setAutoFocusEnabled(true)
//            .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build() } catch (e:Exception){
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


//            private fun checkBarcodeInDatabase(barcode: String) {
//                val client = OkHttpClient()
//
//                val request = Request.Builder()
//                    .url("https://nodei.ssccglpinnacle.com/getScannedInbound")
//                    .build()
//
//                client.newCall(request).enqueue(object : Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        e.printStackTrace()
//                        // Handle failure
//                        runOnUiThread {
//                            Toast.makeText(applicationContext, "Failed to connect to server", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        response.use {
//                            if (!response.isSuccessful) {
//                                // Handle unsuccessful response
//                                runOnUiThread {
//                                    Toast.makeText(applicationContext, "Failed to get response from server", Toast.LENGTH_SHORT).show()
//                                }
//                            } else {
//                                val responseData = response.body?.string()
//                                responseData?.let { parseApiResponse(it, barcode) }
//                            }
//                        }
//                    }
//                })
//            }

//try
            private fun checkBarcodeInDatabase(barcode: String) {
                val client = OkHttpClient()

                val json = JSONObject()
                json.put("barcode", barcode)

                val requestBody = json.toString()

                val request = Request.Builder()
                    .url("https://nodei.ssccglpinnacle.com/searchBarr1")

                   // .url("https://nodei.ssccglpinnacle.com/count")
                    .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        // Handle failure
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) {
                                // Handle unsuccessful response
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "Failed to get response from server", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val responseData = response.body?.string()
                                responseData?.let { parseApiResponse(it, barcode) }
                            }
                        }
                    }
                })
            }




           // try
//            private fun parseApiResponse(responseData: String, barcode: String) {
//                try {
//                    val jsonObject = JSONObject(responseData)
//                    val found = jsonObject.getBoolean("found")
//                    var message = ""
//
//                    if (found) {
//
////                        val responseData = jsonObject.getString("responseData")
////                        val barcode = jsonObject.getString("barcode")
//                        val result = jsonObject.getString("result")
//                        message = "Scanned barcode belongs to Pinnacle Data base with value: $result "
//                    } else {
//                        message = "Scanned barcode does not belong to the database."
//                    }
//
//                    runOnUiThread {
//                        binding.txtMessage.text = message
//                        binding.txtMessage.visibility = View.VISIBLE
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    runOnUiThread {
//                        Toast.makeText(applicationContext, "Failed to parse response", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//


            private fun parseApiResponse(responseData: String, barcode: String) {
                runOnUiThread {
                    binding.txtMessage.text = responseData
                    binding.txtMessage.visibility = View.VISIBLE
                }
            }




//            private fun parseApiResponse(responseData: String, scannedBarcode: String) {
//                val jsonArray = JSONArray(responseData)
//                var found = false
//                var message = ""
//
//                for (i in 0 until jsonArray.length()) {
//                    val jsonObject = jsonArray.getJSONObject(i)
//                    val barcodeData = jsonObject.getJSONArray("barcodeData")
//
//                    for (j in 0 until barcodeData.length()) {
//                        if (barcodeData.getString(j) == scannedBarcode) {
//                            found = true
//                            val title = jsonObject.getString("Title")
//                            val orderno = jsonObject.getString("orderno")
//                            val BatchID = jsonObject.getString("BatchID")
//                            val date = jsonObject.getString("date")
//
//                            //message = "Scanned barcode belongs to Pinnacle Data base so book is original with: Tittle-->$title, Order no --> $orderno,Batch ID --> $BatchID and Date-->$date"
//                            message = "Scanned barcode belongs to Pinnacle Data base so book is original with:\nTitle-->$title\nOrder no --> $orderno\nBatch ID --> $BatchID\nDate-->$date"
//
//
//                            break
//                        }
//                    }
//                    if (found) {
//                        break
//                    }
//                }
//
//                if (!found) {
//                    message = "Scanned barcode does not belong to the database."
//                }
//
//                runOnUiThread {
//                    binding.txtMessage.text = message
//                    binding.txtMessage.visibility = View.VISIBLE
//                }
//            }

            // Modify your receiveDetections function to call checkBarcodeInDatabase
            override fun receiveDetections(p0: Detector.Detections<Barcode>) {
                val barcodes = p0.detectedItems
                if (barcodes.size() != 0) {
                    binding.txtBarcodeValue!!.post {
                        binding.btnAction!!.text = "SEARCH ITEM"
                        val scannedBarcode = barcodes.valueAt(0).displayValue
                        binding.txtBarcodeValue.setText(scannedBarcode)
                        checkBarcodeInDatabase(scannedBarcode)
                    }
                }
            }

        })
    }


    override fun onPause(){
        super.onPause()
        cameraSource!!.release()
    }
    override fun onResume(){
        super.onResume()
        iniBc()
    }


}



