package com.maestro.parking.parking.presentation

import com.maestro.parking.base.redux.MainState
import com.maestro.parking.parking.data.polygon.Point
import com.maestro.parking.parking.data.toPolygon
import com.maestro.parking.parking.utils.distance
import com.maestro.parking.parking.presentation.model.UiParking
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import su.rusfearuth.arch.kdroidredux.rx.store.Store
import java.math.BigDecimal
import java.math.RoundingMode

class ParkingsPresenter(private val view: ParkingsView,
                        private val store: Store<MainState>) {
  private var locationState = store.state.location.copy()
  private var subs = CompositeDisposable()

  fun onResume() {
    loadParkings()
    store.subscribe {
      if (locationState == it.location) {
        return@subscribe
      }
      locationState = it.location.copy()
      loadParkings()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribeBy(onNext = view::showParkings, onError = {})
    }.apply { subs.add(this) }
  }

  fun onPause() {
    subs.dispose()
  }

  private fun loadParkings(): Observable<List<UiParking>> {
    val currentLocationPoint = locationState
        .let {
          if (it.lat == 0.0 || it.lon == 0.0) {
            return Observable.empty()
          }
          Point(it.lat, it.lon)
        }
    val uiParkings = store.state.parkings.parkings
        .map {
          val distance = (distance(currentLocationPoint,
                                   it.boundaryPoints.toPolygon()) / 1000)
              .let {
                BigDecimal(it)
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .toString()
              }
          UiParking(it.name, distance, it.imageUrl, it.description)
        }
        .sortedBy { it.distance }
    return Observable.just(uiParkings)
  }
}