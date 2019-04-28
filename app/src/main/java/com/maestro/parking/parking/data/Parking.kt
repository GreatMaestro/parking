package com.maestro.parking.parking.data

data class Parking(val name: String,
                   val boundaryPoints: List<BoundaryPoint>,
                   val imageUrl: String,
                   val description: String)