package com.example.mapsactivity.data

data class Stores(
    val count: Int,
    val page: Int,
    val storeInfos: List<Store>,
    val totalCount: Int,
    val totalPages: Int
)