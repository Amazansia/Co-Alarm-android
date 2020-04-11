package com.example.mapsactivity

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mapsactivity.data.Store
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private lateinit var storesByGeo : List<Store> //전역변수

    //현재 위치 객체
    private lateinit var lastLocation: Location
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
            fetchJson(lastLocation);
        }

        /*var pinLocation : LatLng
        // location = LatLng(37.566554, 126.978546) //서울시청 핀 코드
        pinLocation = LatLng(storesByGeo.get(0).lat, storesByGeo.get(0).lng)
        placeMarkerOnMap(pinLocation)*/

        // Add a marker in Seoul and move the camera (처음 서울 기반 zoom)
        /*
        val myPlace = LatLng(37.56, 126.97) // 서울 코드
        map.addMarker(MarkerOptions().position(myPlace).title("Marker in Seoul"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12.0f)) // 초반 zoom 크기

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
        */
    }

    // Json Parser 기능 ( 현재 위도, 경도 값을 받아서 공공데이터 받아오기 )
    fun fetchJson(location: Location){
        println("데어터를 가져 오는 중...")
//        val key = "Your-key"

        // maskApi 링크로 변경함
        val url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1"

        // 임의로 현재 위치로 설정
        val query = "/storesByGeo/json?"+ "lat=37.500148776257376&lng=127.02741476091066&m=1000";
        println("--------query---------\n" + url + query)
        val request = Request.Builder().url(url + query).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response?.body()?.string()

                if(body != null) {
                    println("--------body---------")
                    println(body)
                } else {
                    println("error!")
                }

                //파싱 - 이렇게 가져온 데이터를 모델오브젝트로 변환해 줘야 한다.
                val gson = GsonBuilder().create()
                val parser = JsonParser()
                //루트 object와 경로를 찾아서 설정해 주는데 이부분에서 개념이 안 잡혀서 헤메다. 원하는 데이터가 속에 속에 들어 있어서...
                val rootObj = parser.parse(body.toString())
                    .getAsJsonObject().get("stores")
                //여기까진 제대로 되고
                val type = object : TypeToken<List<Store>>() {}.type
                var storesByGeo = gson.fromJson<List<Store>>(rootObj, type)

//                val storesByGeo =  gson.fromJson<List<Store>>(rootObj, StoresByGeo::class.java)
                //썸네일을 위한 추가 작업
                println("--------store[0]---------")
                println(storesByGeo.get(0).name)

                for (store in storesByGeo)
                {
                    var pinLocation : LatLng
                    pinLocation = LatLng(store.lat, store.lng)
                    runOnUiThread{ placeMarkerOnMap(pinLocation) } //마크 찍는 메소드 (ui는 메인쓰레드에서)
                }



                //백그라운드에서 돌기 때문에 메인UI로 접근할 수 있도록 해줘야 한다.
                runOnUiThread {
                    //어답터 설정
//                    my_recycler_view.adapter = RecyclerViewAdapter(books)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("리퀘스트 실패")
            }
        })
    }

    // 새로운 핀 생성 (아직 수정 더 해야함)
    private fun placeMarkerOnMap(location: LatLng) {
        val position = location
        map.addMarker(MarkerOptions()
            .position(position)
            .title("Museum")
            .snippet("National Air and Space Museum"))
    }

    //    위치 권한이 off인 경우, request하는 메소드
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
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    override fun onMarkerClick(p0: Marker?) = false


}
