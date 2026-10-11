package com.jagoalt.praisebreak.domain.model

data class Scripture(
    val id: String,
    val reference: String,
    val content: String
)

data class BibleVersion(
    val id: Int,
    val abbreviation: String,
    val title: String,
    val language: String,
    val copyright: String
)

data class PassageRef(val id: String, val reference: String)
