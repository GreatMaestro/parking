package com.maestro.parking.location.utils

import android.content.Context
import android.location.Location
import com.maestro.parking.R
import java.text.DateFormat
import java.util.*

object LocationUtils {

  /**
   * Returns the `location` object as a human readable string.
   * @param location  The [Location].
   */
  fun getLocationText(location: Location?): String {
    return if (location == null) "Unknown location"
    else "(" + location.latitude + ", " + location.longitude + ")"
  }

  fun getLocationTitle(context: Context): String {
    return context.getString(
      R.string.location_updated, DateFormat.getDateTimeInstance().format(Date())
    )
  }
}
