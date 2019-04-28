package com.maestro.parking.parking.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.maestro.parking.R
import com.maestro.parking.parking.presentation.model.UiParking
import kotlinx.android.synthetic.main.fragment_parkings.*
import org.koin.android.scope.currentScope
import org.koin.core.parameter.parametersOf

class ParkingsFragment : Fragment(), ParkingsView {
  private val presenter: ParkingsPresenter by currentScope.inject { parametersOf(this) }
  private lateinit var adapter: ParkingsAdapter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.fragment_parkings, container, false)

  override fun onViewCreated(view: View,
                             savedInstanceState: Bundle?) {
    adapter = ParkingsAdapter(ArrayList())
    parkings.layoutManager = LinearLayoutManager(context)
    parkings.adapter = adapter
  }

  override fun onResume() {
    super.onResume()
    presenter.onResume()
  }

  override fun onPause() {
    presenter.onPause()
    super.onPause()
  }

  override fun onDestroy() {
    currentScope.close()
    super.onDestroy()
  }

  override fun showParkings(parkings: List<UiParking>) {
    adapter.setParkings(parkings)
  }
}