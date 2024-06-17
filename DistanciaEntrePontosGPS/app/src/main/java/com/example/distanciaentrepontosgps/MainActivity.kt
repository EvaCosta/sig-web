package com.example.distanciaentrepontosgps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService

data class Ponto(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0
) {
    constructor(latitude: Double, longitude: Double) : this(latitude, longitude, 0.0)

    fun imprimir(): String {
        return "Lat: $latitude, Long: $longitude, Alt: $altitude"
    }

    fun imprimir2(): String {
        return """
            ****************************
            Latitude: $latitude
            Longitude: $longitude
            Altitude: $altitude
            ****************************
        """.trimIndent()
    }
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

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private var p1: Ponto = Ponto()
    private var p2: Ponto = Ponto()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun getPonto(updatePonto1: Boolean): Ponto? {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return null
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, MinhaLocalizacaoListener())

        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (lastKnownLocation != null) {
            // Decide qual ponto deve ser atualizado
            if (updatePonto1) {
                p1 = Ponto(lastKnownLocation.latitude, lastKnownLocation.longitude)
                return p1
            } else {
                p2 = Ponto(lastKnownLocation.latitude, lastKnownLocation.longitude)
                return p2
            }
        }

        return null // Retorno caso não haja localização conhecida
    }

    fun reset(v: View) {
        p1 = Ponto()
        p2 = Ponto()
        val edtPtoA = findViewById<EditText>(R.id.edtPtoA)
        val edtPtoB = findViewById<EditText>(R.id.edtPtoB)
        edtPtoA.setText("")
        edtPtoB.setText("")
    }

    fun calcularDistancia(v: View) {
        val resultado = FloatArray(1)
        Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, resultado)

        val texto = "**** Distância: ${resultado[0]}m"
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }

    fun verPontoA(v: View) {
        mostrarGoogleMaps(p1.latitude, p1.longitude)
    }

    fun verPontoB(v: View) {
        mostrarGoogleMaps(p2.latitude, p2.longitude)
    }

    fun lerPontoA(v: View) {
        val ponto = getPonto(true)
        val edtPtoA = findViewById<EditText>(R.id.edtPtoA)
        if (ponto != null) {
            edtPtoA.setText(ponto.imprimir2())
        } else {
            edtPtoA.setText("Ponto não encontrado")
        }
    }

    fun lerPontoB(v: View) {
        val ponto = getPonto(false)
        val edtPtoB = findViewById<EditText>(R.id.edtPtoB)
        if (ponto != null) {
            edtPtoB.setText(ponto.imprimir2())
        } else {
            edtPtoB.setText("Ponto não encontrado")
        }
    }

    fun mostrarGoogleMaps(latitude: Double, longitude: Double) {
        val wv: WebView = findViewById(R.id.webv)
        wv.settings.javaScriptEnabled = true
        wv.loadUrl("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
    }
}
