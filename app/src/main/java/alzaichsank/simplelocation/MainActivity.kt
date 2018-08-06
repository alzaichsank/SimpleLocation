package alzaichsank.simplelocation

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.multidex.MultiDex
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    // inside a basic activity

    private val TAG = "MainActivity"
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mLocationManager: LocationManager? = null
    lateinit var mLocation: Location
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    lateinit var locationManager: LocationManager

    private var GpsStatus : Boolean? = false
    private var NetworkStatus : Boolean? = false
    private var OnClickButton : Boolean? = false

    private var latitude: Double = 0.toDouble()
    private var longitude:Double = 0.toDouble()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Create persistent LocationManager reference
        lateinit var locationManager: LocationManager
        MultiDex.install(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //* dot his when button hit *//
        btn_check.setOnClickListener{
            OnClickButton = true
            checkPermissionActivity()
        }

    }
//    declare override

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected) {
            mGoogleApiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {

        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.errorCode)
    }

    override fun onLocationChanged(location: Location) {
        if(location.latitude!=latitude || location.longitude != longitude){

            latitude = location.latitude
            longitude = location.longitude
            var msg = "Updated Location: Latitude " + latitude.toString() + longitude.toString()
            txt_latitude.text = latitude.toString()
            txt_longitude.text = longitude.toString()
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }




    }

    override fun onConnected(p0: Bundle?) {

        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionActivity()
        }else{
            startLocationUpdates()

            var fusedLocationProviderClient :
                    FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient .lastLocation
                    .addOnSuccessListener(this) { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object

                            mLocation = location
                            latitude = mLocation.latitude
                            longitude = mLocation.longitude

                            txt_latitude.text = latitude.toString()
                            txt_longitude.text = longitude.toString()
                        }
                    }

        }


    }


    fun go_location(){
        CheckGpsStatus()
        if (GpsStatus!! && NetworkStatus!!) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect()
            }
            Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show()
        }else{
            showAlert()
        }
    }


    fun CheckGpsStatus() {

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        NetworkStatus=  locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }



    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Check Location") { _, _ ->
                    startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Please enable your location", Toast.LENGTH_SHORT).show()
                }
        dialog.show()
    }

    private fun startLocationUpdates() {

        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
    }

    //permission check and sdk check
    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    fun checkPermissionActivity(){
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("LOG TAG HASIL", "IN IF Build.VERSION.SDK_INT >= 23")

            if (!checkLocationPermission()) {
                Log.d("LOG TAG HASIL", "IN IF hasPermissions")
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            } else {
                Log.d("LOG TAG HASIL", "IN ELSE hasPermissions")
                go_location()
            }
        } else {
            Log.d("LOG TAG HASIL", "IN ELSE  Build.VERSION.SDK_INT >= 23")
            go_location()
        }
    }

    fun checkLocationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this@MainActivity != null && android.Manifest.permission.ACCESS_FINE_LOCATION != null) {
            for (permission in android.Manifest.permission.ACCESS_FINE_LOCATION) {
                if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
    //permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mGoogleApiClient = GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build()
                    go_location()

                } else {

                    if(OnClickButton!!){
                        val alert = android.support.v7.app.AlertDialog.Builder(this@MainActivity)
                        alert.setTitle("Warning!")
                        alert.setMessage("Please give permission!")
                        alert.setCancelable(false)
                        alert.setPositiveButton("Yes",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface, which: Int) {
                                        // TODO Auto-generated method stub
                                        this@MainActivity.finish()
                                        this@MainActivity.startActivity(this@MainActivity.intent)
                                    }
                                })
                        alert.show()
                    }else{
                        Log.d("LOG TAG HASIL", "PERMISSIONS Denied")
                        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }


}