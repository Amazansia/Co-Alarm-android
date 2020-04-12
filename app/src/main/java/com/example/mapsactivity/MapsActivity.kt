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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    }

    // Json Parser 기능 ( 현재 위도, 경도 값을 받아서 공공데이터 받아오기 )
    fun fetchJson(location: Location){
        println("데이터를 가져 오는 중...")
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

                //썸네일을 위한 추가 작업
                println("--------store[0]---------")
                println(storesByGeo.get(0).name)

                // runOnUiThread: 백그라운드에서 돌기 때문에 메인UI로 접근할 수 있도록 주는 메소드
                // store리스트 루프 (핀 출력)
                for (store in storesByGeo)
                {
                    var pinLocation : LatLng
                    pinLocation = LatLng(store.lat, store.lng)
                    //핀 찍는 메소드 (ui는 메인쓰레드) , 현재 매개변수로 남은개수랑 위치만 보내는데 가게 이름도 보내야 할듯
                    runOnUiThread{ placeMarkerOnMap(pinLocation, store.remain_stat) }

                }

                //어뎁터 설정 (사용 안함)
                /*
                runOnUiThread {

                    my_recycler_view.adapter = RecyclerViewAdapter(books)
                }
                */
            }

            override fun onFailure(call: Call, e: IOException) {
                println("리퀘스트 실패")
            }
        })
    }

    // 새로운 핀 생성 (아직 수정 더 해야함)
    private fun placeMarkerOnMap(location: LatLng, remain : String) {
        val position = location
        var color = null
        if(remain == "plenty")
            //color =  이미지를 green으로 바꿔야함
        else if (remain == "some")
            //color =  이미지를 yellow으로 바꿔야함
        else if (remain == "few")
            //color =  이미지를 red으로 바꿔야함
        else if (remain == "empty")
            //color =  이미지를 gray로 바꿔야함

        map.addMarker(MarkerOptions()   //MarkerOptions의 매개변수에 color를 넣어야함
            .position(position)
            .title("Museum")    //이것도 store.name 를 매개변수로 받아야할 듯 ?
            .snippet("National Air and Space Museum"))
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
