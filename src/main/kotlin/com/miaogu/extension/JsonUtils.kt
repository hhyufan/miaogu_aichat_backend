package com.miaogu.extension

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.miaogu.entity.ChatMessage

private val gson: Gson by lazy {
    GsonBuilder()
        .setPrettyPrinting()
        .create()
}

fun Any.toJson(): String {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    return gson.toJson(this)
}
inline fun <reified T : ChatMessage> String.fromMessageJson(): MutableList<ChatMessage> {
    val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    val type = object : TypeToken<List<T>>() {}.type
    return gson.fromJson(this, type)
}

internal inline fun <reified T> String.fromJsonToObject(): T {
    return try {
        gson.fromJson(this, T::class.java)
    } catch (ex: JsonSyntaxException) {
        throw IllegalArgumentException("Failed to parse JSON: ${ex.message}", ex)
    }
}
