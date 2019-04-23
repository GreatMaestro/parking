package com.maestro.parking.parking.redux

import com.maestro.parking.core.redux.MainState
import su.rusfearuth.arch.kdroidredux.core.reducer.Reducer

val parkingsReducer: Reducer<MainState> = {state, action ->
  when (action) {
    is UpdateParkingsAction           -> updateParkings(state, action)
    is UpdateCurrentParkingInfoAction -> updateCurrentParkingInfo(state, action)
    is UpdateStartParkingTimeAction   -> updateStartParkingTime(state, action)
    is UpdateEndParkingTimeAction     -> updateEndParkingTime(state, action)
    else                              -> state
  }
}

internal val updateParkings = {
    state: MainState, action: UpdateParkingsAction ->
  val parkings = state.parkings.copy(parkings = action.parkings)
  state.copy(parkings = parkings)
}

internal val updateCurrentParkingInfo = {
    state: MainState, action: UpdateCurrentParkingInfoAction ->
  val parkings = state.parkings.copy(currentParkingInfo = action.currentParkingInfo)
  state.copy(parkings = parkings)
}

internal val updateStartParkingTime = {
    state: MainState, action: UpdateStartParkingTimeAction ->
  val currentParkingInfo = state.parkings.currentParkingInfo?.copy()?.apply { startParkingTime = action.startParkingTime }
  val parkings = state.parkings.copy(currentParkingInfo = currentParkingInfo)
  state.copy(parkings = parkings)
}

internal val updateEndParkingTime = {
    state: MainState, action: UpdateEndParkingTimeAction ->
  val currentParkingInfo = state.parkings.currentParkingInfo?.copy()?.apply { endParkingTime = action.endParkingTime }
  val parkings = state.parkings.copy(currentParkingInfo = currentParkingInfo)
  state.copy(parkings = parkings)
}