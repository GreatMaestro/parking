package com.maestro.parking.parking.data.polygon

import java.util.ArrayList

/**
 * The 2D polygon.
 */
class Polygon private constructor(val sides: List<Line>,
                                  private val boundingBox: BoundingBox) {

  /**
   * Builder of the polygon
   */
  class Builder {
    private var vertexes: MutableList<Point> = ArrayList()
    private val sides = ArrayList<Line>()
    private var boundingBox: BoundingBox? = null

    private var isFirstPoint = true
    private var isClosed = false

    /**
     * Add vertex points of the polygon.<br></br>
     * It is very important to add the vertexes by order, like you were drawing them one by one.
     *
     * @param point The vertex point
     * @return The builder
     */
    fun addVertex(point: Point): Builder {
      if (isClosed) {
        // each hole we start with the new array of vertex points
        vertexes = ArrayList()
        isClosed = false
      }

      updateBoundingBox(point)
      vertexes.add(point)

      // add line (edge) to the polygon
      if (vertexes.size > 1) {
        val line = Line(vertexes[vertexes.size - 2], point)
        sides.add(line)
      }

      return this
    }

    /**
     * Close the polygon shape. This will create a new side (edge) from the **last** vertex point to the **first** vertex point.
     *
     * @return The builder
     */
    fun close(): Builder {
      validate()

      // add last Line
      sides.add(Line(vertexes[vertexes.size - 1], vertexes[0]))
      isClosed = true

      return this
    }

    /**
     * Build the instance of the polygon shape.
     *
     * @return The polygon
     */
    fun build(): Polygon {
      validate()

      // in case you forgot to close
      if (!isClosed) {
        // add last Line
        sides.add(Line(vertexes[vertexes.size - 1], vertexes[0]))
      }

      return Polygon(sides, boundingBox!!)
    }

    /**
     * Update bounding box with a new point.
     *
     * @param point New point
     */
    private fun updateBoundingBox(point: Point) {
      if (isFirstPoint) {
        boundingBox = BoundingBox().apply {
          xMax = point.x
          xMin = point.x
          yMax = point.y
          yMin = point.y
        }

        isFirstPoint = false
      } else {
        boundingBox!!.apply {
          if (point.x > xMax) {
            xMax = point.x
          } else if (point.x < xMin) {
            xMin = point.x
          }
          if (point.y > yMax) {
            yMax = point.y
          } else if (point.y < yMin) {
            yMin = point.y
          }
        }
      }
    }

    private fun validate() {
      if (vertexes.size < 3) {
        throw RuntimeException("Polygon must have at least 3 points")
      }
    }
  }

  /**
   * Check if the the given point is inside of the polygon.<br></br>
   *
   * @param point The point to check
   * @return `True` if the point is inside the polygon, otherwise return `False`
   */
  operator fun contains(point: Point): Boolean {
    if (inBoundingBox(point)) {
      val ray = createRay(point)
      var intersection = 0
      for (side in sides) {
        if (intersect(ray, side)) {
          intersection++
        }
      }

      /*
       * If the number of intersections is odd, then the point is inside the polygon
       */
      if (intersection % 2 != 0) {
        return true
      }
    }
    return false
  }

  /**
   * By given ray and one side of the polygon, check if both lines intersect.
   *
   * @param ray
   * @param side
   * @return `True` if both lines intersect, otherwise return `False`
   */
  private fun intersect(ray: Line, side: Line): Boolean {
    val intersectPoint: Point

    // if both vectors aren't from the kind of x=1 lines then go into
    if (!ray.isVertical && !side.isVertical) {
      // check if both vectors are parallel. If they are parallel then no intersection point will exist
      if (ray.a - side.a == 0.0) {
        return false
      }

      val x = (side.b - ray.b) / (ray.a - side.a) // x = (b2-b1)/(a1-a2)
      val y = side.a * x + side.b // y = a2*x+b2
      intersectPoint = Point(x, y)
    } else if (ray.isVertical && !side.isVertical) {
      val x = ray.start.x
      val y = side.a * x + side.b
      intersectPoint = Point(x, y)
    } else if (!ray.isVertical && side.isVertical) {
      val x = side.start.x
      val y = ray.a * x + ray.b
      intersectPoint = Point(x, y)
    } else {
      return false
    }

    return side.isInside(intersectPoint) && ray.isInside(intersectPoint)
  }

  /**
   * Create a ray. The ray will be created by given point and on point outside of the polygon.<br></br>
   * The outside point is calculated automatically.
   *
   * @param point
   * @return
   */
  private fun createRay(point: Point): Line {
    // create outside point
    val epsilon = (boundingBox.xMax - boundingBox.xMin) / 10e6
    val outsidePoint = Point(boundingBox.xMin - epsilon, boundingBox.yMin)

    return Line(outsidePoint, point)
  }

  /**
   * Check if the given point is in bounding box
   *
   * @param point
   * @return `True` if the point in bounding box, otherwise return `False`
   */
  private fun inBoundingBox(point: Point): Boolean {
    with(boundingBox) {
      return point.x in xMin..xMax && point.y in yMin..yMax
    }
  }

  private class BoundingBox {
    var xMax = Double.NEGATIVE_INFINITY
    var xMin = Double.NEGATIVE_INFINITY
    var yMax = Double.NEGATIVE_INFINITY
    var yMin = Double.NEGATIVE_INFINITY
  }
}
