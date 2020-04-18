package com.example.mapsactivity

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

public class NetworkController
{

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
}