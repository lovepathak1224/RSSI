package com.example.rssi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssi.databinding.ActivityRssiCsvDisplayActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class RssiCsvDisplayActivity : ComponentActivity() {

    private lateinit var binding: ActivityRssiCsvDisplayActivityBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var rssiAdapter: RssiAdapter
    private val rssiDataList: MutableList<String> = mutableListOf()

    private val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRssiCsvDisplayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        rssiAdapter = RssiAdapter(rssiDataList)
        recyclerView.adapter = rssiAdapter

        loadCsvData()
    }

    private fun loadCsvData() {
        coroutineScope.launch {
            val data = withContext(Dispatchers.IO) {
                readCsvFile()
            }
            rssiDataList.addAll(data)
            Log.d("RssiCsvDisplayActivity", "Data loaded: $rssiDataList")
            rssiAdapter.notifyDataSetChanged()
        }
    }

    private fun readCsvFile(): List<String> {
        val csvFile = File(getExternalFilesDir(null), "rssi_data.csv")
        val lines = mutableListOf<String>()
        if (csvFile.exists()) {
            BufferedReader(FileReader(csvFile)).use { reader ->
                reader.forEachLine { line ->
                    lines.add(line)
                }
            }
        }
        return lines
    }
}
