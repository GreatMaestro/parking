package com.maestro.parking.parking.presentation

import com.maestro.parking.parking.presentation.model.UiParking

interface ParkingsView {
  fun showParkings(parkings: List<UiParking>)
}