package com.maestro.parking.base.redux

import com.maestro.parking.location.redux.LocationState
import com.maestro.parking.parking.redux.ParkingsState
import su.rusfearuth.arch.kdroidredux.core.interfaces.State

data class MainState(
  val location: LocationState = LocationState(),
  val parkings: ParkingsState = ParkingsState()
) : State {
  override fun makeCopy(): MainState = copy()
}