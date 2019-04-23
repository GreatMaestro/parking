package com.maestro.parking.parking.redux

import com.maestro.parking.parking.data.CurrentParkingInfo
import com.maestro.parking.parking.data.Parking
import su.rusfearuth.arch.kdroidredux.core.interfaces.State

data class ParkingsState(
  val parkings: List<Parking> = emptyList(),
  val currentParkingInfo: CurrentParkingInfo? = null
) : State {
  override fun makeCopy(): ParkingsState = copy()
}