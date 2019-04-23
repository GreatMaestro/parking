package com.maestro.parking.location.redux

import su.rusfearuth.arch.kdroidredux.core.interfaces.State

data class LocationState(val lat: Double = 0.0,
                         val lon: Double = 0.0,
                         val time: Long = 0L) : State {
  override fun makeCopy(): LocationState = copy()
}