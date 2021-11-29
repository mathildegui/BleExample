package com.example.lesdentsbleues.model

class CustomGatt(val uuid: String, val type: Type) {
    enum class Type {
        SERVICE,
        CHAR
    }
}