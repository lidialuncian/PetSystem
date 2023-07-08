package com.example.petsystem.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.petsystem.R
import com.example.petsystem.utils.Constants
import java.io.IOException
import java.util.UUID

class WaterBowlActivity : BaseActivity() {
    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var bluetoothSocket: BluetoothSocket? = null
    private lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    var isConnected: Boolean = false
    lateinit var address:String
    lateinit var name:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_bowl)
        setUpActionBar()

        address = intent.getStringExtra(Constants.EXTRA_ADDRESS).toString()
        name = intent.getStringExtra(Constants.EXTRA_NAME).toString()

        Toast.makeText(this@WaterBowlActivity, "$name: $address", Toast.LENGTH_LONG).show()
        ConnectToDevice(this@WaterBowlActivity).execute()

        val start = findViewById<Button>(R.id.start_btn)
        val stop = findViewById<Button>(R.id.stop_btn)
        val disconnect = findViewById<Button>(R.id.disconnect_water_btn)
        start.setOnClickListener { sendCommand("O") }
        stop.setOnClickListener { sendCommand("C") }
        disconnect.setOnClickListener { disconnect() }
    }

    private fun setUpActionBar() {

        setSupportActionBar(findViewById(R.id.toolbar_water_activity))

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.title = resources.getString(R.string.water_dispenser)
        }

        findViewById<Toolbar>(R.id.toolbar_water_activity).setNavigationOnClickListener {
            startActivity(Intent(this@WaterBowlActivity, MainActivity::class.java))
            finish() }
    }

    private fun sendCommand(input: String) {
        if (bluetoothSocket != null) {
            try {
                if (bluetoothSocket!!.isConnected) {
                    val outputStream = bluetoothSocket!!.outputStream
                    val command = input.toByteArray()
                    outputStream.write(command)
                } else {
                    Toast.makeText(
                        this@WaterBowlActivity,
                        "Bluetooth socket is not connected",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@WaterBowlActivity, "Error sending command", Toast.LENGTH_LONG)
                    .show()
            }
        }else{
            Toast.makeText(this@WaterBowlActivity, "Bluetooth connection error", Toast.LENGTH_LONG).show()
        }
    }

    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        onBackPressed()
    }

    private inner class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Connecting...please wait")
        }

        @Deprecated("Deprecated in Java")
        @RequiresApi(Build.VERSION_CODES.S)
        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (bluetoothSocket == null || !isConnected) {

                    bluetoothManager = getSystemService(android.bluetooth.BluetoothManager::class.java)
                    bluetoothAdapter = bluetoothManager.adapter
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)

                    // Check for Bluetooth permissions
                    val hasPermission = ContextCompat.checkSelfPermission(this@WaterBowlActivity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    val hasPermissionCoarse = ContextCompat.checkSelfPermission(this@WaterBowlActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission || !hasPermissionCoarse) {
                        // Request Bluetooth permissions
                        ActivityCompat.requestPermissions(this@WaterBowlActivity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN), Constants.REQUEST_BLUETOOTH_PERMISSION)
                    }
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    bluetoothAdapter.cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Toast.makeText(this@WaterBowlActivity, "Connection failed", Toast.LENGTH_LONG).show()
                Log.i("data", "couldn't connect")
            } else {
                isConnected = true
                Toast.makeText(this@WaterBowlActivity, "Connected successfully", Toast.LENGTH_LONG).show()
            }
            hideProgressDialog()
        }
    }

}