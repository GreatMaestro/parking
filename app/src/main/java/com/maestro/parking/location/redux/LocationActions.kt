package com.maestro.parking.location.redux

import su.rusfearuth.arch.kdroidredux.core.interfaces.Action

data class UpdateLocationAction(
  val lat: Double = 0.0,
  val lon: Double = 0.0,
  val time: Long = 0L
) : Action