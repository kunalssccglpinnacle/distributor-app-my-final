package com.example.piracycheckapp

import ApiService
import GetKeyResponse
import SearchBarr1Request
import SearchBarr1Response
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BarcodeScan : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private var isCameraStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBarcodeScanner()
    }

    private fun initBarcodeScanner() {
        try {
            barcodeDetector = BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.CODE_128)
                .build()
            cameraSource = CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error initializing barcode scanner")
            return
        }

        binding.surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (!isCameraStarted) {
                    try {
                        cameraSource.start(binding.surfaceView!!.holder)
                        isCameraStarted = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Error starting camera")
                    }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                try {
                    cameraSource.stop()
                    isCameraStarted = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    showToast("Error stopping camera")
                }
            }

        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                showToast("Barcode scanner has been stopped")
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    val scannedBarcode = barcodes.valueAt(0).displayValue
                    getBarcodeInfo(scannedBarcode)
                }
            }
        })
    }

    private fun getBarcodeInfo(barcode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.getKey(barcode)
        call.enqueue(object : Callback<GetKeyResponse> {
            override fun onResponse(call: Call<GetKeyResponse>, response: Response<GetKeyResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        showBarcodeInfo(it)
                    }
                } else {
                    showToast("Failed to get response from server")
                }
            }

            override fun onFailure(call: Call<GetKeyResponse>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server")
            }
        })
    }

    private fun showBarcodeInfo(apiResponse: GetKeyResponse?) {
        if (apiResponse != null) {
            val message = "Scanned barcode belongs to Pinnacle Database with:\nKey --> ${apiResponse.key}"
            runOnUiThread {
                binding.txtMessage?.text = message
                binding.txtMessage?.visibility = View.VISIBLE
            }

            // Call the second API with the key
            searchBarr1(apiResponse.key)
        } else {
            runOnUiThread {
                binding.txtMessage?.text = "not found in api 1"
                binding.txtMessage?.visibility = View.VISIBLE
            }
        }
    }

    private fun searchBarr1(key: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.searchBarr1(SearchBarr1Request(key))
        call.enqueue(object : Callback<SearchBarr1Response> {
            override fun onResponse(call: Call<SearchBarr1Response>, response: Response<SearchBarr1Response>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        showSearchBarr1Response(it)
                    }
                } else {
                    showToast("Failed to get response from server")
                }
            }

            override fun onFailure(call: Call<SearchBarr1Response>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server")
            }
        })
    }

    private fun showSearchBarr1Response(response: SearchBarr1Response) {
        val message = "SearchBarr1 response:\nResult --> ${response.result}"
        runOnUiThread {
            binding.txtMessage?.text = message
            binding.txtMessage?.visibility = View.VISIBLE
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
        isCameraStarted = false
    }

    override fun onResume() {
        super.onResume()
        initBarcodeScanner()
    }
}
