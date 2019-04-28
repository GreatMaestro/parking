package com.maestro.parking.parking.utils

import android.location.Location
import com.maestro.parking.parking.data.polygon.Point
import com.maestro.parking.parking.data.polygon.Line
import com.maestro.parking.parking.data.polygon.Polygon

private fun distance(start: Point,
                     end: Point): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.x,
        start.y, end.x,
        end.y, results
    )
    return results[0].toDouble()
}

private fun distance(point: Point,
                     line: Line): Double {
  val lineLength = distance(line.start,
                                                              line.end)
  if (lineLength == 0.0) {
    return distance(point,
                                                      line.start)
  }
  val t = ((point.x - line.start.x) * (line.end.x - line.start.x) + (point.y - line.start.y) * (line.end.y - line.start.y)) / lineLength
  if (t < 0) {
    return distance(point,
                                                      line.start)
  }
  if (t > 1) {
    return distance(point,
                                                      line.end)
  }
  val q = Point(line.start.x + Math.round(t * (line.end.x - line.start.x)),
                line.start.y + Math.round(t * (line.end.y - line.start.y)))
  return distance(point,
                                                    q)
}

fun distance(point: Point,
             poly: Polygon): Double {
  if (poly.contains(point)) {
    return 0.0
  }
  return poly.sides
             .map { edge ->
               distance(point,
                                                          edge)
             }
             .reduce(::minOf)
}