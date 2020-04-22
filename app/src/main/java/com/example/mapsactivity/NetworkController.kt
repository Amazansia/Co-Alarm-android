package com.example.mapsactivity

import android.location.Location
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.net.URLEncoder


public class NetworkController
{
    val clientId = "uyuvw49pig"
    val clientSecret = "BOJCfcK5klMMoKOgtb1LUhwlpCOsxPXZepAAE0Kb"

    public constructor() {

    }

    // Json Parser 기능 ( 현재 위도, 경도 값을 받아서 공공데이터 약국정보 받아오기 )
    fun fetchStore(location: Location, completion: (List<Store>?)-> Unit) {
        //marker함수를 매개변수로 받아온다
        println("데이터를 가져 오는 중...")
        // maskApi 링크로 변경함
        val url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1"

        // 임의로 현재 위치로 설정
        val query = "/storesByGeo/json?"+ "lat=" + location.latitude + "&lng=" + location.longitude + "&m=1000";
        println("--------query---------\n" + url + query)
        val request = Request.Builder().url(url + query).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response?.body()?.string()

                if(body != null) {
//                    println("--------body---------")
//                    println(body)
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
                val type = object : TypeToken<List<Store>?>() {}.type
                var storesByGeo = gson.fromJson<List<Store>?>(rootObj, type)

                completion(storesByGeo)

                //썸네일을 위한 추가 작업
                println("--------store[0]---------")
                println(storesByGeo?.get(0)?.name)

                // runOnUiThread: 백그라운드에서 돌기 때문에 메인UI로 접근할 수 있도록 주는 메소드
                // store리스트 루프 (핀 출력)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("리퀘스트 실패")
            }

        })
    }

    // Json Parser 기능 ( 검색창에 입력한 주소를 위도, 경도값으로 바꾸는 함수 )
    fun fetchGeocoding(address: String) {
        // OkHttp로 요청하기
        val text = URLEncoder.encode("${address[0]}", "UTF-8")
        println(text)
        val url = URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${text}")

        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
            .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
            .method("GET", null)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                val body = response?.body()?.string()
                println("*********************")
                println("*********************")
                println("Success to execute request : $body")
                println("*********************")
                println("*********************")

                //파싱 - 이렇게 가져온 데이터를 모델오브젝트로 변환해 줘야 한다.
                val gson = GsonBuilder().create()
                val parser = JsonParser()
                //루트 object와 경로를 찾아서 설정해 주는데 이부분에서 개념이 안 잡혀서 헤메다. 원하는 데이터가 속에 속에 들어 있어서...
                val rootObj = parser.parse(body.toString())
                    .getAsJsonObject().get("addresses")

                val type = object : TypeToken<List<Address>?>() {}.type
                val addresses = gson.fromJson<List<Address>?>(rootObj, type)

                println("*********************")
                println("*********************")
                println(addresses?.get(0)?.x)
                println("*********************")
                println("*********************")
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Failed to execute request")
            }
        })
    }
}