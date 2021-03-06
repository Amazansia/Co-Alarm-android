package com.example.mapsactivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_maps.*
import okhttp3.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var networkController : NetworkController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var inputtext : String

    //현재 위치 객체
    private lateinit var lastLocation: Location
    private lateinit var searchLocation: Location
    //위치 권환 요청 객체
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    //앱 처음 실행 할때 적용되는 함수 (main 함수)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val searchbtn = findViewById<Button>(R.id.btn_search)
        searchbtn.setOnClickListener{
            inputtext = entertext.text.toString()
            networkController = NetworkController()
            networkController.fetchStore(lastLocation){storesByGeo : List<Store>? ->
                placeMarkerOnMap(storesByGeo)
            }
            networkController.fetchGeocoding(inputtext,searchLocation)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap() //위치 권한 request

        // 1
        map.isMyLocationEnabled = true


        // 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // 마지막으로 알려준 위치를 갖는다. 가끔 null값이 있을 수도 있음
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
            networkController = NetworkController()
            networkController.fetchStore(lastLocation){storesByGeo : List<Store>? ->
                placeMarkerOnMap(storesByGeo)}
//            if (storesByGeo != null) {
//                println(storesByGeo.get(0).name)
//            }
//            placeMarkerOnMap(storesByGeo)
//            networkController.fetchGeocoding("서교동")
        }
    }



    private fun bitmapDescriptorFromVector(context: Context, vectorResId:Int):BitmapDescriptor {
        var vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        var bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        var canvas =  Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // 새로운 핀 생성 (아직 수정 더 해야함)
    private fun placeMarkerOnMap(storesByGeo: List<Store>?) {
        if (storesByGeo != null) {
            for (store in storesByGeo) {
                var pinLocation : LatLng
                pinLocation = LatLng(store.lat, store.lng)
                //핀 찍는 메소드 (ui는 메인쓰레드) , 현재 매개변수로 남은개수랑 위치만 보내는데 가게 이름도 보내야 할듯

                var remain : String = store.remain_stat

                runOnUiThread {
                         if (remain == "plenty"){
                            map.addMarker(MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                .position(pinLocation)
                                .title(store.name)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_green)))
                        }
                        else if (remain == "some"){
                            map.addMarker(MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                .position(pinLocation)
                                .title(store.name)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_yellow)))
                        }
                        else if (remain == "few"){
                            map.addMarker(MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                .position(pinLocation)
                                .title(store.name)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_red)))
                        }
                        else {
                            map.addMarker(MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
                                .position(pinLocation)
                                .title(store.name)
                                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_gray)))
                        }
                }
            }
        }

    }

    // 위치 권한이 꺼진 경우, 요청하는 메소드
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng, remain) 이거 사용할거면 매개변수 확인 할 것
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    override fun onMarkerClick(p0: Marker?) = false


}