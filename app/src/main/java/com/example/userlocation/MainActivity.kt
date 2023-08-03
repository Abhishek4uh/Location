package com.example.userlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.userlocation.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var service: Intent?=null
    private lateinit var sharedPreference: SharedPreferences
    private var booleanForRational:Boolean=false

    private val requestPermissionLauncherBG= registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){

        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private val requestPermissionLauncher= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        when{
            it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false)->{
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                     if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                         requestPermissionLauncherBG.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                     }
                }
            }
            it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,false)->{

            }
        }
    }



    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    enum class Screen {
        MAINACTIVITY,
        MAINACTIVITY2
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        service = Intent(this, LocationService::class.java)


        sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        val lastVisitedScreen = sharedPreference.getString("LastActivity", null)
        if (lastVisitedScreen == null) {
            //openMainScreen()
            Log.d("APPDATA_1", "lastVisitedScreen->null")
        } else {
            if (lastVisitedScreen == Screen.MAINACTIVITY.toString()) {
                Log.d("APPDATA_2", "MAINACTIVITY")
            } else {
                Log.d("APPDATA_3", "MAINACTIVITY2")
            }
        }

        mainBinding.tvNewActivity.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }


        mainBinding.btnStartLocationTracking.setOnClickListener {
            if (checkLocationPermissions()) {
                startLocationTracking()
            } else {
                askLocationPermissions()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
    }


    private fun ultimateDialogForGallary() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Permission Required ")
        builder.setMessage("This app require Location permission that you have denied previously please grant the permission")

        //positive action
        builder.setPositiveButton("Settings"){dialogInterface,_ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            startActivity(intent)
            dialogInterface.dismiss()
        }
        //negative action
        builder.setNegativeButton("Cancel"){dialogInterface,_ ->
            dialogInterface.dismiss()
        }

        val alertDialog= builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }


    private fun showSnackbar(view: View, msg: String, length: Int, actionMessage: CharSequence?, action: (View) -> Unit) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(view)
            }.show()
        } else {
            snackbar.show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun askLocationPermissions() {
        val shouldShowRationaleFine = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val shouldShowRationaleCoarse = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldShowRationaleFine || shouldShowRationaleCoarse) {
            showRationaleMessage()
        }
        else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showRationaleMessage() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Location permission is required to track your location.")
            .setPositiveButton("OK") { _, _ ->
                // Request the permission after the user acknowledges the rationale message
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Handle the user's cancellation if needed
            }
            .show()
    }


    private fun startLocationTracking() {
        mainBinding.tvLatitude.visibility= View.INVISIBLE
        mainBinding.tvLongitude.visibility= View.INVISIBLE
        mainBinding.tvAccuracy.visibility= View.INVISIBLE
        mainBinding.progress.visibility = View.VISIBLE
        startService(service)
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent){
        mainBinding.tvLatitude.visibility =View.VISIBLE
        mainBinding.tvLongitude.visibility =View.VISIBLE
        mainBinding.tvAccuracy.visibility =View.VISIBLE
        mainBinding.progress.visibility =View.GONE
        mainBinding.tvLatitude.text = "Latitude -> ${locationEvent.latitude}"
        mainBinding.tvLongitude.text = "Longitude -> ${locationEvent.longitude}"
        mainBinding.tvAccuracy.text = "Accuracy -> ${locationEvent.accuracy}"
        Log.d("UPDATE",
                       "${locationEvent.latitude}" +
                            "${locationEvent.longitude}"+
                            "${locationEvent.accuracy}")

    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(service)
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
        val editor = sharedPreference.edit()
        editor.putString("LastActivity", "MAINACTIVITY")
        editor.apply()
    }

}