package com.example.petsystem.activities

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.petsystem.R
import com.example.petsystem.firebase.FirestoreDatabase
import com.example.petsystem.models.Feeder
import com.example.petsystem.utils.Constants
import java.io.IOException
import java.util.UUID


class FeederActivity : BaseActivity() {
    var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var bluetoothSocket: BluetoothSocket? = null
    private lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    var isConnected: Boolean = false
    lateinit var address:String
    lateinit var name:String

    private lateinit var feederDetails: Feeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeder)

        setUpActionBar()

        FirestoreDatabase().loadMeals(this@FeederActivity)

        val quantityFirstMeal = findViewById<EditText>(R.id.edit_text_quantity_first_meal)
        quantityFirstMeal.inputType = InputType.TYPE_CLASS_NUMBER
        val quantitySecondMeal = findViewById<EditText>(R.id.edit_text_quantity_second_meal)
        quantitySecondMeal.inputType = InputType.TYPE_CLASS_NUMBER
        val quantityThirdMeal = findViewById<EditText>(R.id.edit_text_quantity_third_meal)
        quantityThirdMeal.inputType = InputType.TYPE_CLASS_NUMBER

        val timeFirstMeal = findViewById<EditText>(R.id.edit_text_hour_first_meal)
        val timeSecondMeal = findViewById<EditText>(R.id.edit_text_hour_second_meal)
        val timeThirdMeal = findViewById<EditText>(R.id.edit_text_hour_third_meal)

        timeFirstMeal.setOnClickListener { showTimePicker(timeFirstMeal) }
        timeSecondMeal.setOnClickListener { showTimePicker(timeSecondMeal) }
        timeThirdMeal.setOnClickListener { showTimePicker(timeThirdMeal) }

        address = intent.getStringExtra(Constants.EXTRA_ADDRESS).toString()
        name = intent.getStringExtra(Constants.EXTRA_NAME).toString()

        Toast.makeText(this@FeederActivity, "$name: $address", Toast.LENGTH_LONG).show()
        ConnectToDevice(this@FeederActivity).execute()

        val open = findViewById<Button>(R.id.open_btn)
        val close = findViewById<Button>(R.id.close_btn)
        val disconnect = findViewById<Button>(R.id.disconnect_feeder_btn)
        val sendFirstMeal = findViewById<Button>(R.id.send_first_meal_btn)
        val sendSecondMeal = findViewById<Button>(R.id.send_second_meal_btn)
        val sendThirdMeal = findViewById<Button>(R.id.send_third_meal_btn)
        open.setOnClickListener { sendCommand("O") } //OPEN
        close.setOnClickListener { sendCommand("C") } //CLOSE
        disconnect.setOnClickListener { disconnect() }
        sendFirstMeal.setOnClickListener {
            showProgressDialog(resources.getString(R.string.please_wait))
            updateMeal("FIRST")
            sendCommand("M1:Q1:${quantityFirstMeal.text} T1:${timeFirstMeal.text}")
        }
        sendSecondMeal.setOnClickListener {
            updateMeal("SECOND")
            sendCommand("M2:Q2:${quantitySecondMeal.text} T2:${timeSecondMeal.text}") }
        sendThirdMeal.setOnClickListener {
            updateMeal("THIRD")
            sendCommand("M3:Q3:${quantityThirdMeal.text} T3:${timeThirdMeal.text}") }

    }

    fun setMealsDataInFeederPage(feeder: Feeder) {
        feederDetails = feeder
        findViewById<EditText>(R.id.edit_text_quantity_first_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.first_meal_quantity)
        findViewById<EditText>(R.id.edit_text_quantity_second_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.second_meal_quantity)
        findViewById<EditText>(R.id.edit_text_quantity_third_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.third_meal_quantity)
        findViewById<EditText>(R.id.edit_text_hour_first_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.first_meal_hour)
        findViewById<EditText>(R.id.edit_text_hour_second_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.second_meal_hour)
        findViewById<EditText>(R.id.edit_text_hour_third_meal).text = Editable.Factory.getInstance().newEditable(feederDetails.third_meal_hour)
    }

    private fun updateMeal(meal: String) {
        val mealHashMAp = HashMap<String, Any>()

        if (meal == "FIRST") {
            if (findViewById<EditText>(R.id.edit_text_quantity_first_meal).text.toString() != feederDetails.first_meal_quantity) {
                mealHashMAp[Constants.FIRST_MEAL_QUANTITY] =
                    findViewById<EditText>(R.id.edit_text_quantity_first_meal).text.toString()
            }

            if (findViewById<EditText>(R.id.edit_text_hour_first_meal).text.toString() != feederDetails.first_meal_hour) {
                mealHashMAp[Constants.FIRST_MEAL_HOUR] =
                    findViewById<EditText>(R.id.edit_text_hour_first_meal).text.toString()
            }
        }
        else if (meal == "SECOND") {
            if (findViewById<EditText>(R.id.edit_text_quantity_second_meal).text.toString() != feederDetails.second_meal_quantity) {
                mealHashMAp[Constants.SECOND_MEAL_QUANTITY] =
                    findViewById<EditText>(R.id.edit_text_quantity_second_meal).text.toString()
            }

            if (findViewById<EditText>(R.id.edit_text_hour_second_meal).text.toString() != feederDetails.second_meal_hour) {
                mealHashMAp[Constants.SECOND_MEAL_HOUR] =
                    findViewById<EditText>(R.id.edit_text_hour_second_meal).text.toString()
            }
        }
        else if (meal == "THIRD") {
            if (findViewById<EditText>(R.id.edit_text_quantity_third_meal).text.toString() != feederDetails.third_meal_quantity) {
                mealHashMAp[Constants.THIRD_MEAL_QUANTITY] =
                    findViewById<EditText>(R.id.edit_text_quantity_third_meal).text.toString()
            }

            if (findViewById<EditText>(R.id.edit_text_hour_third_meal).text.toString() != feederDetails.third_meal_hour) {
                mealHashMAp[Constants.THIRD_MEAL_HOUR] =
                    findViewById<EditText>(R.id.edit_text_hour_third_meal).text.toString()
            }
        }

        FirestoreDatabase().updateMeals(this@FeederActivity, mealHashMAp)
    }

    fun mealUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
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

    private fun sendCommand(input: String) {
        if (bluetoothSocket != null) {
            try{
                if(bluetoothSocket!!.isConnected){
                    val outputStream = bluetoothSocket!!.outputStream
                    val command = input.toByteArray()
                    outputStream.write(command)
                }
                else{
                    Toast.makeText(this@FeederActivity, "Bluetooth socket is not connected", Toast.LENGTH_LONG).show()
                }
            } catch(e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@FeederActivity, "Error sending command", Toast.LENGTH_LONG).show()

            }
        }else{
            Toast.makeText(this@FeederActivity, "Bluetooth connection errors", Toast.LENGTH_LONG).show()
        }
    }
    private fun showTimePicker(timeMeal: EditText) {
        val currentTime: Calendar = Calendar.getInstance()
        val hour: Int = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute: Int = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this@FeederActivity, { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            timeMeal.setText(selectedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }
    private fun setUpActionBar() {

        setSupportActionBar(findViewById(R.id.toolbar_feeder_activity))

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.title = resources.getString(R.string.food_dispenser)
        }

        findViewById<Toolbar>(R.id.toolbar_feeder_activity).setNavigationOnClickListener {
            onBackPressed()
        }
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
                    val hasPermission = ContextCompat.checkSelfPermission(this@FeederActivity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                    val hasPermissionCoarse = ContextCompat.checkSelfPermission(this@FeederActivity, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission || !hasPermissionCoarse) {
                        // Request Bluetooth permissions
                        ActivityCompat.requestPermissions(this@FeederActivity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN), Constants.REQUEST_BLUETOOTH_PERMISSION)
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
                Toast.makeText(this@FeederActivity, "Connection failed", Toast.LENGTH_LONG).show()
                Log.i("data", "couldn't connect")
            } else {
                isConnected = true
                Toast.makeText(this@FeederActivity, "Connected successfully", Toast.LENGTH_LONG).show()
            }
            hideProgressDialog()
        }
    }
}