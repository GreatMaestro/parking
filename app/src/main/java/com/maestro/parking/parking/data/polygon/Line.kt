package com.maestro.parking.parking.data.polygon

/**
 * Line is defined by starting point and ending point on 2D dimension.
 */
data class Line(val start: Point, val end: Point) {

  /**
   * y = **A**x + B
   *
   * @return The **A**
   */
  val a: Double

  /**
   * y = Ax + **B**
   *
   * @return The **B**
   */
  val b: Double

  /**
   * Indicate whereas the line is vertical.
   * For example, line like x=1 is vertical, in other words parallel to axis Y.
   * In this case the A is (+/-)infinite.
   *
   * @return `True` if the line is vertical, otherwise return `False`
   */
  var isVertical = false
    private set

  init {
    if (this.end.x - this.start.x != 0.0) {
      a = (this.end.y - this.start.y) / (this.end.x - this.start.x)
      b = this.start.y - a * this.start.x
    } else {
      a = Double.NaN
      b = Double.NaN
      isVertical = true
    }
  }

  /**
   * Indicate whereas the point lays on the line.
   *
   * @param point - The point to check
   * @return `True` if the point lays on the line, otherwise return `False`
   */
  fun isInside(point: Point): Boolean {
    val maxX = if (start.x > end.x) start.x else end.x
    val minX = if (start.x < end.x) start.x else end.x
    val maxY = if (start.y > end.y) start.y else end.y
    val minY = if (start.y < end.y) start.y else end.y

    return point.x in minX..maxX && point.y in minY..maxY
  }

  override fun toString(): String {
    return String.format("%s-%s", start.toString(), end.toString())
  }
}
