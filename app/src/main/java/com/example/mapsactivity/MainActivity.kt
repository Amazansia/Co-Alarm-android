package com.example.mapsactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //레이아웃매니저 설정
//        my_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
//        my_recycler_view.setHasFixedSize(true)

        //어답터 설정
        //my_recycler_view.adapter = RecyclerViewAdapter()

        fetchJson()
    }

    fun fetchJson(){
        println("데어터를 가져 오는 중...")
//        val key = "Your-key"
        val url = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response?.body()?.string()
                println(body)

                //파싱 - 이렇게 가져온 데이터를 모델오브젝트로 변환해 줘야 한다.
                val gson = GsonBuilder().create()
                val parser = JsonParser()
                //루트 object와 경로를 찾아서 설정해 주는데 이부분에서 개념이 안 잡혀서 헤메다. 원하는 데이터가 속에 속에 들어 있어서...
                val rootObj = parser.parse(body.toString())
                    .getAsJsonObject().get("SeoulLibraryBookRentNumInfo")
                val books =  gson.fromJson(rootObj, RentInfo::class.java)
                //썸네일을 위한 추가 작업
                println(books.row[0].ISBN)

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

//    "CONTROLNO": "000000269509",
//    "TITLE": "7년 후 :기욤 뮈소 장편소설",
//    "AUTHOR": "기욤 뮈소 [지음]",
//    "PUBLISHER": "밝은세상",
//    "PUBLISHER_YEAR": "2012",
//    "ISBN": "9788984371200",
//    "CLASS_NO": "8",
//    "CNT": "15"

    // Model class(서울도서관 인기대출도서)
    data class RentInfo(val list_total_count: Int, val row: List<Book> )
    data class Book(val TITLE: String, val AUTHOR : String, val PUBLISHER : String, val PUBLISHER_YEAR : Int, val ISBN : Long)

}