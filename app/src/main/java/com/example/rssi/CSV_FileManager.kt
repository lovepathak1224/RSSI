
package com.example.rssi

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class CSV_FileManager(private val context: Context) {

    suspend fun saveWiFiDataToCSV(dataList: ArrayList<Map<String, String>>) = withContext(Dispatchers.IO) {
        val appName = context.getString(R.string.app_name)
        val timestamp = SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val fileName = "WiFi_Data_$timestamp.csv"

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "External storage is not available", Toast.LENGTH_LONG).show()
            }
            return@withContext
        }

        val appDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), appName)

        if (!appDir.exists()) {
            val dirCreated = appDir.mkdirs()
            if (!dirCreated) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to create directory", Toast.LENGTH_LONG).show()
                }
                return@withContext
            }
        }

        val file = File(appDir, fileName)

        try {
            FileOutputStream(file).use { fileOutputStream ->
                OutputStreamWriter(fileOutputStream).use { writer ->
                    writer.append("Sample Number, RSSI, Link Speed (Mbps), SSID, Network ID\n")
                    for (i in dataList.indices) {
                        val data = dataList[i]
                        writer.append("${i + 1}, ${data["rssi"]}, ${data["linkSpeed"]}, ${data["ssid"]}, ${data["networkId"]}\n")
                    }
                    writer.flush()
                    Log.d("Data", "Data saved as $fileName in RSSI directory")
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.toString()),
                null
            ) { path, uri ->
                Log.d("File Scan", "File $path was scanned successfully: $uri")
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data saved as $fileName in RSSI directory", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }
}
