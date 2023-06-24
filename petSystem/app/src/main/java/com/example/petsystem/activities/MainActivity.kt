package com.example.petsystem.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.petsystem.R
import com.example.petsystem.firebase.FirestoreClass
import com.example.petsystem.models.User
import com.example.petsystem.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    companion object {
        const val EXTRA_ADDRESS: String = "Device_address"
        const val EXTRA_NAME: String = "Device_name"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this@MainActivity)

        FirestoreClass().loadUserData(this@MainActivity)

        bluetoothManager = getSystemService(android.bluetooth.BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this@MainActivity, "Device doesn't support Bluetooth!", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            // Bluetooth is disabled, request enabling
            requestBluetoothEnable()

        }

        val feederConnectBtn = findViewById<Button>(R.id.feeder_connect_btn)
        val waterConnectBtn = findViewById<Button>(R.id.water_connect_btn)
        val collarConnectBtn = findViewById<Button>(R.id.collar_connect_btn)

        feederConnectBtn.setOnClickListener {
            if(!bluetoothAdapter!!.isEnabled){
                Toast.makeText(this@MainActivity, "Please enable Bluetooth!", Toast.LENGTH_LONG).show()
            }else{
                connectToDevice(FeederActivity::class.java)
            }

        }

        waterConnectBtn.setOnClickListener {
            if(!bluetoothAdapter!!.isEnabled){
                Toast.makeText(this@MainActivity, "Please enable Bluetooth!", Toast.LENGTH_LONG).show()
            }else {
                connectToDevice(WaterBowlActivity::class.java)
            }
        }

        collarConnectBtn.setOnClickListener {

        }
    }

    private fun connectToDevice(targetActivity: Class<*>) {
        // Add your device connection logic here
        // For example, you can show a progress dialog or perform Bluetooth device discovery
        pairedDevices = bluetoothAdapter!!.bondedDevices
        val deviceList: ArrayList<BluetoothDevice> = ArrayList()
        val deviceListNames: ArrayList<String> = ArrayList()

        if(pairedDevices.isNotEmpty()){
            for(device: BluetoothDevice in pairedDevices){
                deviceList.add(device)
                val subitem = "Address: ${device.address}"
                deviceListNames.add("${device.name}\n$subitem")
            }
        }else{
            Toast.makeText(this@MainActivity, "No paired bluetooth devices found", Toast.LENGTH_LONG).show()
        }

        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1,deviceListNames)
        val selectDeviceList = findViewById<ListView>(R.id.select_device_list)
        selectDeviceList.adapter = adapter
        selectDeviceList.onItemClickListener = AdapterView.OnItemClickListener{
            _, _, position,_ ->
            val device: BluetoothDevice = deviceList[position]
            val name: String = device.name
            val address: String = device.address

            // Once the device is connected, start the target activity
            if(targetActivity.toString().contains("FeederActivity")){
                if(name == "HC-05-feeder"){
                    val intent = Intent(this@MainActivity, targetActivity)
                    intent.putExtra(EXTRA_ADDRESS, address)
                    intent.putExtra(EXTRA_NAME, name)
                    startActivity(intent)
                    recreate()
                }
                else{
                    Toast.makeText(this@MainActivity, "Please select the correct device", Toast.LENGTH_LONG).show()
                    recreate()
                }
            }
            else if(targetActivity.toString().contains("WaterBowlActivity")){
                if(name == "HC-05"){
                    val intent = Intent(this@MainActivity, targetActivity)
                    intent.putExtra(EXTRA_ADDRESS, address)
                    intent.putExtra(EXTRA_NAME, name)
                    startActivity(intent)
                    recreate()
                }
                else{
                    Toast.makeText(this@MainActivity, "Please select the correct device", Toast.LENGTH_LONG).show()
                    recreate()
                }
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothEnable() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the missing permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            ) {
                // Display rationale dialog
                showBluetoothPermissionRationaleDialog()
            } else {
                // Permission has not been granted yet, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    Constants.BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // Permission already granted, enable Bluetooth
            enableBluetooth()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showBluetoothPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth Permission")
            .setMessage("The Bluetooth permission is required to enable Bluetooth functionality.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Request the permission again
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    Constants.BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Handle cancellation or show an error message
                Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable Bluetooth
                enableBluetooth()
            } else {
                // Permission denied by the user
                Toast.makeText(this@MainActivity, "Bluetooth permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT)
    }




    private fun setupActionBar(){
        val toolbarMainActivity = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity.setNavigationIcon(R.drawable.ic_navigation_menu)
        toolbarMainActivity.setNavigationOnClickListener{
            // toggle drawer
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java), Constants.MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User) {
        Glide
            .with(this@MainActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(findViewById(R.id.nav_user_image))

        findViewById<TextView>(R.id.tv_username_text).text = user.username
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this@MainActivity)
        }else if(resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_ENABLE_BT){
            if (bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this@MainActivity, "Bluetooth has been enabled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Bluetooth has been disabled", Toast.LENGTH_LONG).show()
            }
        }else{
            Log.e("Cancelled", "Cancelled")
        }
    }

}