package com.maestro.parking.base

import android.app.Application
import com.maestro.parking.core.di.reduxModule
import com.maestro.parking.core.redux.MainState
import com.maestro.parking.parking.redux.UpdateParkingsAction
import com.maestro.parking.core.utils.loadParkings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import su.rusfearuth.arch.kdroidredux.rx.store.Store

class App : Application() {
  override fun onCreate() {
    super.onCreate()

    startKoin {
      androidLogger()
      androidContext(this@App)
      modules(reduxModule)
    }
    val parkings = loadParkings(this)
    val store = GlobalContext.get().koin.get<Store<MainState>> { parametersOf() }
    store.dispatch(UpdateParkingsAction(parkings = parkings))
  }
}