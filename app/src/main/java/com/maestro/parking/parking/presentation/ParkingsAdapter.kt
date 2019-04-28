package com.maestro.parking.parking.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maestro.parking.R
import com.maestro.parking.parking.presentation.model.UiParking
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_parking.view.*

class ParkingsAdapter(private var data: ArrayList<UiParking>) : RecyclerView.Adapter<ParkingVH>() {

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): ParkingVH {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_parking,
                                                           parent,
                                                           false)
    return ParkingVH(view)
  }

  override fun getItemCount(): Int =
      data.size

  override fun onBindViewHolder(holder: ParkingVH,
                                position: Int) =
      holder.bind(data[position])

  fun setParkings(parkings: List<UiParking>) {
    data = ArrayList(parkings)
    notifyDataSetChanged()
  }
}

class ParkingVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(parking: UiParking) =
      with(itemView) {
        title.text = parking.name
        Picasso.get().load(parking.imageUrl).into(image)
        distance.text = context.getString(R.string.km,
                                          parking.distance)
        description.text = parking.description
      }

}