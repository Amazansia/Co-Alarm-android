package com.example.mapsactivity.data

data class Sales(
    val count: Int,
    val page: Int,
    val sales: List<Sale>,
    val totalCount: Int,
    val totalPages: Int
)