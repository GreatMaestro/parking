package com.maestro.parking.parking.data

data class CurrentParkingInfo(
  val currentParking: Parking,
  val enterTime: Long,
  var startParkingTime: Long = 0L,
  var endParkingTime: Long = 0L
)