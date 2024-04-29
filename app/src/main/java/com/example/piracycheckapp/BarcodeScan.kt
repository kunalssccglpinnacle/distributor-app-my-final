package com.example.piracycheckapp

//import AddBarcodeRequest
//import ApiService
//import CountRequest
//import GetKeyResponse
//import SearchBarr1Request
//import SearchBarr1Response
//import android.annotation.SuppressLint
//import android.graphics.Color
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.text.Spannable
//import android.text.SpannableString
//import android.text.style.ForegroundColorSpan
//import android.util.Log
//import android.view.SurfaceHolder
//import android.view.View
//import android.widget.Toast
//import com.example.piracycheckapp.databinding.ActivityBarcodeScanBinding
//import com.google.android.gms.vision.CameraSource
//import com.google.android.gms.vision.Detector
//import com.google.android.gms.vision.barcode.Barcode
//import com.google.android.gms.vision.barcode.BarcodeDetector
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class BarcodeScan : AppCompatActivity() {
//    private lateinit var binding: ActivityBarcodeScanBinding
//    private lateinit var barcodeDetector: BarcodeDetector
//    private lateinit var cameraSource: CameraSource
//    private var isCameraStarted = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityBarcodeScanBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        initBarcodeScanner()
//    }
//
//    private fun initBarcodeScanner() {
//        try {
//            barcodeDetector = BarcodeDetector.Builder(this)
//                .setBarcodeFormats(Barcode.CODE_128)
//                .build()
//            cameraSource = CameraSource.Builder(this, barcodeDetector)
//                .setRequestedPreviewSize(1920, 1080)
//                .setAutoFocusEnabled(true)
//                .build()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            showToast("Error initializing barcode scanner")
//            return
//        }
//
//        binding.surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
//            @SuppressLint("MissingPermission")
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                if (!isCameraStarted) {
//                    try {
//                        cameraSource.start(binding.surfaceView!!.holder)
//                        isCameraStarted = true
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        showToast("Error starting camera")
//                    }
//                }
//            }
//
//            override fun surfaceChanged(
//                holder: SurfaceHolder,
//                format: Int,
//                width: Int,
//                height: Int
//            ) {
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                try {
//                    cameraSource.stop()
//                    isCameraStarted = false
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    showToast("Error stopping camera")
//                }
//            }
//
//        })
//
//        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
//            override fun release() {
//                showToast("Barcode scanner has been stopped")
//            }
//
//            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
//                val barcodes = detections.detectedItems
//                if (barcodes.size() > 0) {
//                    val scannedBarcode = barcodes.valueAt(0).displayValue
//
//
//
//
//                    getBarcodeInfo(scannedBarcode)
//                }
//            }
//        })
//    }
//
//    private fun getBarcodeInfo(barcode: String){
//        if (barcode.contains("-")) {
//            searchBarr1(barcode)
//        }else{
//            val retrofit = Retrofit.Builder()
//                .baseUrl("https://nodei.ssccglpinnacle.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(OkHttpClient.Builder().build())
//                .build()
//
//            val api = retrofit.create(ApiService::class.java)
//            val call = api.getKey(barcode)
//            call.enqueue(object : Callback<GetKeyResponse> {
//                override fun onResponse(call: Call<GetKeyResponse>, response: Response<GetKeyResponse>) {
//                    if (response.isSuccessful) {
//                        val result = response.body()
//                        result?.let {
//                            showBarcodeInfo(it)
//                        }
//                    } else {
//                        showToast("Failed to get response from server")
//                    }
//                }
//
//                override fun onFailure(call: Call<GetKeyResponse>, t: Throwable) {
//                    t.printStackTrace()
//                    showToast("Failed to connect to server")
//                }
//            })
//        }
//    }
//    private fun showBarcodeInfo(apiResponse: GetKeyResponse?) {
//        if (apiResponse != null) {
//            val key = apiResponse.key
//            if (key != null) {
//                runOnUiThread {
//                    binding.txtMessage?.text = key
//                    binding.txtMessage?.visibility = View.VISIBLE
//                }
//                // Call the second API with the key
//                searchBarr1(key)
//            } else {
//                runOnUiThread {
//                    binding.txtMessage?.text = "Not verified"
//                    binding.txtMessage?.visibility = View.VISIBLE
//                }
//            }
//        } else {
//            runOnUiThread {
//                binding.txtMessage?.text = "Response is null"
//                binding.txtMessage?.visibility = View.VISIBLE
//            }
//        }
//    }
//    private fun searchBarr1(key: String) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://nodei.ssccglpinnacle.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(OkHttpClient.Builder().build())
//            .build()
//
//        val api = retrofit.create(ApiService::class.java)
//        val call = api.searchBarr1(SearchBarr1Request(key))
//        call.enqueue(object : Callback<SearchBarr1Response> {
//            override fun onResponse(call: Call<SearchBarr1Response>, response: Response<SearchBarr1Response>) {
//                if (response.isSuccessful) {
//                    val result = response.body()
//                    result?.let {
//                        showSearchBarr1Response(it)
//                    }
//                } else {
//                    showToast("Failed to get response from api 2")
//                }
//            }
//
//            override fun onFailure(call: Call<SearchBarr1Response>, t: Throwable) {
//                t.printStackTrace()
//                showToast("Failed to connect to api 2")
//            }
//        })
//    }
//    private fun showSearchBarr1Response(response: SearchBarr1Response) {
//        val result = response.result
//        val message = "Belongs to : $result\n verified "
//
//        val coloredMessage = if (result == "Not Verified") {
//            val spannable = SpannableString(message)
//            spannable.setSpan(ForegroundColorSpan(Color.RED), 11, message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//            spannable
//        } else {
//            message
//        }
//
//        runOnUiThread {
//            binding.txtMessage1?.text = coloredMessage
//            binding.txtMessage1?.visibility = View.VISIBLE
//
//            binding.txtMessage?.text = ""
//            binding.txtMessage?.visibility = View.VISIBLE
//
//
//        }
//    }
//    private fun showToast(message: String) {
//        runOnUiThread {
//            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//
//
//    override fun onPause() {
//        super.onPause()
//        cameraSource.release()
//        isCameraStarted = false
//    }
//
//    override fun onResume() {
//        super.onResume()
//        initBarcodeScanner()
//    }
//}
//
//
//


 // working 3rdapi



import AddBarcodeRequest
import ApiService
import CountRequest
import CountResponse
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
import okhttp3.OkHttpClient
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
    private var barcodeAdded = false // Flag to track if the barcode has already been added

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
                if (barcodes.size() > 0 && !barcodeAdded) { // Check if barcode is not already added
                    val scannedBarcode = barcodes.valueAt(0).displayValue

                    addBarcode(scannedBarcode)
                    barcodeAdded = true // Set the flag to true to indicate barcode is added
                }
            }
        })
    }

    private fun addBarcode(barcode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.addBarcode(AddBarcodeRequest(barcode))
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showToast("Barcode added successfully!")
                    getCount(barcode)
                } else {
                    showToast("Failed to add barcode to the database")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server barcode api")
            }
        })
    }

    private fun getCount(barcode: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://nodei.ssccglpinnacle.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val api = retrofit.create(ApiService::class.java)
        val call = api.getCount(CountRequest(barcode))
        call.enqueue(object : Callback<CountResponse> {
            override fun onResponse(call: Call<CountResponse>, response: Response<CountResponse>) {
                if (response.isSuccessful) {
                    val count = response.body()
                    count?.let {
                        runOnUiThread {
                            binding.txtCount.text = "Barcode scanned ${count} times"
                            binding.txtCount.visibility = View.VISIBLE
                        }
                    }
                } else {
                    showToast("Failed to get count from the server: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<CountResponse>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to connect to server count : ${t.message}")

//                binding.txtCount.text = "Barcode scanned ${t.message}times "
//                binding.txtCount.visibility = View.VISIBLE
            }
        })
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


