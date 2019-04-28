package com.maestro.parking.location.redux

import com.maestro.parking.base.redux.MainState
import su.rusfearuth.arch.kdroidredux.core.reducer.Reducer

val locationReducer: Reducer<MainState> = {state, action ->
  when(action) {
    is UpdateLocationAction -> updateLocation(state, action)
    else                                                       -> state
  }
}

internal val updateLocation = {
    state: MainState, action: UpdateLocationAction ->
  val location = state.location.copy(lat = action.lat, lon = action.lon, time = action.time)
  state.copy(location = location)
}