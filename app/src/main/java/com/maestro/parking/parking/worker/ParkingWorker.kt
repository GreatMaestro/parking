package com.maestro.parking.parking.worker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.maestro.parking.core.redux.MainState
import com.maestro.parking.parking.data.BoundaryPoint
import com.maestro.parking.parking.data.CurrentParkingInfo
import com.maestro.parking.parking.data.polygon.Point
import com.maestro.parking.parking.data.polygon.Polygon
import com.maestro.parking.parking.redux.UpdateCurrentParkingInfoAction
import com.maestro.parking.parking.redux.UpdateEndParkingTimeAction
import com.maestro.parking.parking.redux.UpdateStartParkingTimeAction
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import su.rusfearuth.arch.kdroidredux.rx.store.Store
import java.util.concurrent.TimeUnit

const val MINUTES_BEFORE_START_PARKING = 1L

class ParkingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
  override fun doWork(): Result {
    val store = GlobalContext.get().koin.get<Store<MainState>> { parametersOf() }
    val currentLocation = store.state.location

    // Was in the parking lot last time?
    store.state.parkings.currentParkingInfo?.let { currentParkingInfo ->
      // In the parking lot now?
      if (isInPolygon(currentLocation.lat, currentLocation.lon, currentParkingInfo.currentParking.boundaryPoints)) {
        // The countdown is active?
        if (currentParkingInfo.startParkingTime != 0L) {
          return Result.success()
        }
        val timeFromEnter = Duration.between(Instant.ofEpochMilli(currentParkingInfo.enterTime), Instant.now())
        // Already 3 minutes have passed from the arrival to the parking?
        if (timeFromEnter.toMinutes() >= MINUTES_BEFORE_START_PARKING) {
          store.dispatch(UpdateStartParkingTimeAction(Instant.now().toEpochMilli()))
        }
      } else {
        // The countdown is active?
        if (currentParkingInfo.startParkingTime != 0L) {
          val endParkingTime = Instant.now().toEpochMilli()
          store.dispatch(UpdateEndParkingTimeAction(endParkingTime))
          val parkingTime = TimeUnit.MILLISECONDS.toSeconds(endParkingTime - currentParkingInfo.startParkingTime)
          //TODO: Save to database here
          Log.e("Total parking time", "$parkingTime s")
        }
        store.dispatch(UpdateCurrentParkingInfoAction(null))
      }

      return Result.success()
    }

    for (parking in store.state.parkings.parkings) {
      // In the parking lot now?
      if (isInPolygon(currentLocation.lat, currentLocation.lon, parking.boundaryPoints)) {
        store.dispatch(UpdateCurrentParkingInfoAction(CurrentParkingInfo(parking, currentLocation.time)))
        planParking()
        break
      }
    }

    return Result.success()
  }

  private fun planParking() {
    val parkingWork = OneTimeWorkRequest
      .Builder(ParkingWorker::class.java)
      .setInitialDelay(MINUTES_BEFORE_START_PARKING, TimeUnit.MINUTES)
      .build()
    WorkManager
      .getInstance()
      .enqueue(parkingWork)
  }

  private fun isInPolygon(lat: Double, lon: Double, boundaryPoints: List<BoundaryPoint>): Boolean {
    val polygonBuilder = Polygon.Builder()
    for (boundaryPoint in boundaryPoints) {
      polygonBuilder.addVertex(Point(boundaryPoint.lat, boundaryPoint.lon))
    }
    val polygon = polygonBuilder.build()
    val point = Point(lat, lon)
    return polygon.contains(point)
  }
}