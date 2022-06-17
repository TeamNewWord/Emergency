package com.example.emergency

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity() {
    private  var latitude:Double = 33.5014049
    private var longitude:Double = 126.5315568
    private lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private val cancellationTokenSource=CancellationTokenSource ()
    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            val deniedList: List<String> = result.filter {
                !it.value
            }.map {
                it.key
            }
            when {
                deniedList.isNotEmpty() -> {
                    val map = deniedList.groupBy { permissions ->
                        if (shouldShowRequestPermissionRationale(permissions)) "DENIED" else "EXPLAINED"
                    }
                    map["DENIED"]?.let {
                        //거부 한 번 했을 경우
                        ActivityResultContracts.RequestMultiplePermissions()
                    }
                    map["EXPLAINED"]?.let {
                        //거부 두 번 했을 경우
                        ActivityResultContracts.RequestMultiplePermissions()
                    }
                }
                else -> {}
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val mapview= MapView(this)
        val mapViewContainer = findViewById<ConstraintLayout>(R.id.mapview)
        mapViewContainer.addView(mapview)



        val currentMarker = MapPOIItem()
        currentMarker.apply {
            itemName ="현재 위치"
            markerType =MapPOIItem.MarkerType.RedPin
        }


        val myLocationBtn = findViewById<Button>(R.id.my_location_btn)

        myLocationBtn.setOnClickListener {

            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)
            //1. permission check && getCurrentLocation
            if (ActivityCompat.checkSelfPermission((this),
                    permissions[0]) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    permissions[1]) != PackageManager.PERMISSION_GRANTED
            ) {
                //permission이 없을 때
                requestMultiplePermissionsLauncher.launch(permissions)

            } else {
                fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token).addOnSuccessListener {
                     latitude = it.latitude
                     longitude = it.longitude

                    val mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude)
                    currentMarker.mapPoint = mapPoint
                    mapview.addPOIItem(currentMarker)
                    mapview.setMapCenterPointAndZoomLevel(mapPoint,4,true)

                }
            }



        }



        }
    }




