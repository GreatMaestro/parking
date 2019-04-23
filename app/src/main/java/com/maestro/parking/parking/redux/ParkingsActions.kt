package com.maestro.parking.parking.redux

import com.maestro.parking.parking.data.CurrentParkingInfo
import com.maestro.parking.parking.data.Parking
import su.rusfearuth.arch.kdroidredux.core.interfaces.Action

data class UpdateParkingsAction(
  val parkings: List<Parking> = emptyList()
) : Action

data class UpdateCurrentParkingInfoAction(
  val currentParkingInfo: CurrentParkingInfo?
) : Action

data class UpdateStartParkingTimeAction(
  val startParkingTime: Long
) : Action

data class UpdateEndParkingTimeAction(
  val endParkingTime: Long
) : Action