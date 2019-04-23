package com.maestro.parking.core.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maestro.parking.parking.data.Parking

fun loadParkings(context: Context): List<Parking> =
  context.assets
      .open("parkings.json")
      .bufferedReader()
      .use { it.readText() }
      .let { json -> Gson().fromJson<List<Parking>>(json) }

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object: TypeToken<T>() {}.type)
