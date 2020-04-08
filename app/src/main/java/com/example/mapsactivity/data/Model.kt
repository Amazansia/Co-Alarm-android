package com.example.mapsactivity.data

class Store (
    val code : String,
    val name : String,
    val addr : String,
    val type : String,
    val lat : Double,
    val lng : Double,
    val remain_stat: String?

)

class Stores (
    val count : Int,
    val stores : List<Store>,
    val stock_at : String?
)