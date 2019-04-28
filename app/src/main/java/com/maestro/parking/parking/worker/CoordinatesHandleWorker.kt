package com.maestro.parking.parking.worker

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.maestro.parking.base.redux.MainState
import com.maestro.parking.location.redux.UpdateLocationAction
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import su.rusfearuth.arch.kdroidredux.rx.store.Store

class CoordinatesHandleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  override fun doWork(): Result {
    //get Data out from input
    val latitude = inputData.getDouble(LOCATION_LAT, 0.0)
    val longitude = inputData.getDouble(LOCATION_LONG, 0.0)
    val time = inputData.getLong(LOCATION_TIME, 0L)

    Log.d("CoordinatesHandleWorker", "Job done: $latitude, $longitude")

    val store = GlobalContext.get().koin.get<Store<MainState>> { parametersOf() }
    store.dispatch(UpdateLocationAction(lat = latitude, lon = longitude, time = time))

    val parkingWork = OneTimeWorkRequest
      .Builder(ParkingWorker::class.java)
      .build()
    WorkManager
      .getInstance()
      .enqueue(parkingWork)

    return Result.success()
  }

  companion object {
    const val LOCATION_LONG = "LOCATION_LONG"
    const val LOCATION_LAT = "LOCATION_LAT"
    const val LOCATION_TIME = "LOCATION_TIME"
  }
}
