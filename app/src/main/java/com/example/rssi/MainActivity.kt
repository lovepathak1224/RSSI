package com.example.rssi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rssi.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    companion object {
        const val WRITE_PERMISSION = 101
    }

    private lateinit var binding: ActivityMainBinding
    private val rssiList: ArrayList<Map<String, String>> = ArrayList()
    private val measurePeriod: Int = 500
    private val previewPeriod: Int = 250
    private val handler: Handler = Handler()
    private var numSamples: Int = 0
    private var hasPerms: Boolean = false
    private var measureIsOn: Boolean = false
    private lateinit var wifiManager: WifiManager
    private lateinit var csvFileManager: CSV_FileManager

    private val rssiRunnable: Runnable = object : Runnable {
        override fun run() {
            val wifiInfo = getWiFiInfo()
            addWiFiMeasurement(wifiInfo)
            handler.postDelayed(this, measurePeriod.toLong())
            Log.d("Runnable", "Wi-Fi info measured: $wifiInfo")
        }
    }

    private val rssiPreviewRunnable: Runnable = object : Runnable {
        override fun run() {
            val receivedRSSI = getWiFiInfo()["rssi"]?.toInt() ?: 0
            updateRSSIPreview(receivedRSSI)
            handler.postDelayed(this, previewPeriod.toLong())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        csvFileManager = CSV_FileManager(this)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        binding.buttonStart.setOnClickListener { handleStartButton() }
        binding.buttonClear.setOnClickListener { handleClearButton() }
        binding.buttonSave.setOnClickListener { handleSaveButton() }
        binding.buttonViewCsv.setOnClickListener {
            val intent = Intent(this, RssiCsvDisplayActivity::class.java)
            startActivity(intent)
        }

        hasPerms = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!hasPerms) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSION)
        }

        handler.postDelayed(rssiPreviewRunnable, previewPeriod.toLong())
    }

    private fun getWiFiInfo(): Map<String, String> {
        val wifiInfo = wifiManager.connectionInfo
        return mapOf(
            "rssi" to wifiInfo.rssi.toString(),
            "linkSpeed" to wifiInfo.linkSpeed.toString(), // in Mbps
            "ssid" to wifiInfo.ssid,
            "networkId" to wifiInfo.networkId.toString()
        )
    }

    private fun addWiFiMeasurement(infoMap: Map<String, String>) {
        rssiList.add(infoMap)
        numSamples++
        binding.textViewSampleCount.text = "Amount of samples: $numSamples"
        binding.textViewRssi.text = infoMap["rssi"]
    }

    private fun updateRSSIPreview(rssi: Int) {
        binding.textViewRssiPreview.text = rssi.toString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Permission has been granted")
            } else {
                finish()
                System.exit(0)
            }
        }
    }

    private fun handleStartButton() {
        if (!measureIsOn) {
            Log.d("Runnable", "Runnable started")
            binding.buttonStart.text = "Stop Measuring"
            handler.postDelayed(rssiRunnable, measurePeriod.toLong())
            measureIsOn = true
        } else {
            Log.d("Runnable", "Runnable stopped")
            binding.buttonStart.text = "Start Measuring"
            handler.removeCallbacks(rssiRunnable)
            measureIsOn = false
        }
    }

    private fun handleClearButton() {
        Log.d("Button", "Data cleared")
        rssiList.clear()
        numSamples = 0
        binding.textViewSampleCount.text = "Amount of samples: $numSamples"
        binding.textViewRssi.text = "0"
    }

    private fun handleSaveButton() {
        if (rssiList.isNotEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                csvFileManager.saveWiFiDataToCSV(rssiList)
            }
        } else {
            Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show()
        }
    }
}
