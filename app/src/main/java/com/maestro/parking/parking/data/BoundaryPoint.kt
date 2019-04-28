package com.maestro.parking.parking.data

import com.maestro.parking.parking.data.polygon.Point
import com.maestro.parking.parking.data.polygon.Polygon

data class BoundaryPoint(val lat: Double, val lon: Double)

fun List<BoundaryPoint>.toPolygon(): Polygon {
  val polygonBuilder = Polygon.Builder()
  for (boundaryPoint in this) {
    polygonBuilder.addVertex(Point(boundaryPoint.lat, boundaryPoint.lon))
  }
  return polygonBuilder.build()
}