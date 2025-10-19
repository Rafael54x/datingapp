package com.example.datingapp.models


enum class Jurusan(val displayName: String) {
    TI("Informatika"),
    TE("Teknik Elektro"),
    TF("Teknik Fisika"),
    TK("Teknik Komputer"),
    SI("Sistem Informasi"),
    STRACOM("Strategic Communication"),
    JURNAL("Jurnalistik"),
    MANAGEMENT("Management"),
    AK("Akuntansi"),
    HOTEL("Perhotelan"),
    DKV("Desain Komunikasi Visual"),
    FILM("Film & Animasi"),
    ARSI("Arsitektur"),
}

enum class Range(val displayName: String, val value: String) {
    LESS_THAN("Junior", "<"),
    MORE_THAN("Senior", ">"),
    EQUAL_TO("Seangkatan", "=="),
    NO("No Preference", "-")
}

enum class Gender(val displayName: String) {
    M("Male"),
    F("Female"),
}