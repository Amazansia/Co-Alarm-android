package com.example.mapsactivity.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

const val BASE_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1"

interface  MaskService {
    @GET("storesByGeo/json")
            /**
             * REST 요청을 처리하기 위한 메서드
             * @param par QueryMap을 통해 질의한 쿼리문을 Map으로 부터 받는다.
             * @return Call<T> 콜백 인터페이스 반환, T는 주고 받을 데이터 구조
             * @QueryMap 어노테이션은 위치가 바뀌어도 동적으로 값을 받아올 수 있게 한다.
             */
    fun getTop(@QueryMap par: Map<String, Double>): Call<Stores>

}