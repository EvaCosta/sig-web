package com.example.positionglobalgps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener = MinhaLocalizacaoListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    fun buscarInformacoesGPS(v: View) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissionsLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
            ))
            return
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)

        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnownLocation != null) {
            MinhaLocalizacaoListener.latitude = lastKnownLocation.latitude
            MinhaLocalizacaoListener.longitude = lastKnownLocation.longitude
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            val texto = "Latitude: ${MinhaLocalizacaoListener.latitude}\n" +
                    "Longitude: ${MinhaLocalizacaoListener.longitude}\n"

            Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "GPS DESABILITADO.", Toast.LENGTH_LONG).show()
        }

        mostrarGoogleMaps(MinhaLocalizacaoListener.latitude, MinhaLocalizacaoListener.longitude)
    }

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (!fineLocationGranted && !coarseLocationGranted) {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
        } else {
            buscarInformacoesGPS(View(this)) // Retry to get location after permissions are granted
        }
    }

    fun mostrarGoogleMaps(latitude: Double, longitude: Double) {
        val wv: WebView = findViewById(R.id.webv)
        wv.settings.javaScriptEnabled = true
        wv.loadUrl("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
    }

    class MinhaLocalizacaoListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}

        companion object {
            var latitude: Double = 0.0
            var longitude: Double = 0.0
        }
    }
}