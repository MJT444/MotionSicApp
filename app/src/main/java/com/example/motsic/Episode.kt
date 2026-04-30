package com.example.motsic

data class Episode(
    val timestampMs: Long,
    val trigger: String,
    val severity: Int,
    val helped: String
)
