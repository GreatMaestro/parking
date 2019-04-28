package com.maestro.parking

import com.maestro.parking.parking.data.BoundaryPoint
import com.maestro.parking.parking.utils.isInPolygon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class ParkingTest {

  @Test
  fun `Should be in the polygon`() {
    val boundaryPoints = Arrays.asList(BoundaryPoint(42.449015,
                                                     76.164375),
                                       BoundaryPoint(42.459148,
                                                     76.22068),
                                       BoundaryPoint(42.489029,
                                                     76.201454),
                                       BoundaryPoint(42.477382,
                                                     76.138282))

    val isInPolygon = isInPolygon(42.471304,
                                  76.169868,
                                  boundaryPoints)

    assertEquals(isInPolygon, true)
  }

  @Test
  fun `Should not be in the polygon`() {
    val boundaryPoints = Arrays.asList(BoundaryPoint(42.449015,
                                                     76.164375),
                                       BoundaryPoint(42.459148,
                                                     76.22068),
                                       BoundaryPoint(42.489029,
                                                     76.201454),
                                       BoundaryPoint(42.477382,
                                                     76.138282))

    val isInPolygon = isInPolygon(58.5975,
                                  24.9873,
                                  boundaryPoints)

    assertNotEquals(isInPolygon, true)
  }
}