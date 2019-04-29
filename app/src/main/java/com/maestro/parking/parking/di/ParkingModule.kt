package com.maestro.parking.parking.di

import com.maestro.parking.parking.presentation.ParkingsFragment
import com.maestro.parking.parking.presentation.ParkingsPresenter
import com.maestro.parking.parking.presentation.ParkingsView
import org.koin.core.qualifier.named
import org.koin.dsl.module

val parkingModule = module {
  scope(named<ParkingsFragment>()) {
    scoped {
      (view: ParkingsView) -> ParkingsPresenter(view, get())
    }
  }
}