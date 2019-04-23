package com.maestro.parking.core.di

import com.maestro.parking.core.redux.MainState
import com.maestro.parking.location.redux.locationReducer
import com.maestro.parking.parking.redux.parkingsReducer
import org.koin.dsl.module
import su.rusfearuth.arch.kdroidredux.core.reducer.combineReducers
import su.rusfearuth.arch.kdroidredux.rx.middleware.applyMiddleware
import su.rusfearuth.arch.kdroidredux.rx.store.createStore

val reduxModule = module {
  single {
    createStore(combineReducers(locationReducer, parkingsReducer), MainState(),
                applyMiddleware())
  }
}