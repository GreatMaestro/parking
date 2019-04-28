package com.maestro.parking.location.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

import android.util.Log
import androidx.core.app.NotificationManagerCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.maestro.parking.base.presentation.MainActivity
import com.maestro.parking.R
import com.maestro.parking.location.utils.LocationUtils
import com.maestro.parking.parking.worker.CoordinatesHandleWorker

class LocationUpdatesService : Service() {

  inner class LocalBinder : Binder() {
    val service: LocationUpdatesService
      get() = this@LocationUpdatesService
  }

  private val binder = LocalBinder()

  private var changingConfiguration = false

  private lateinit var notificationManager: NotificationManager
  private lateinit var locationRequest: LocationRequest
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback
  private lateinit var serviceHandler: Handler

  private var location: Location? = null
  private val notification: Notification
    get() {
      val intent = Intent(this, LocationUpdatesService::class.java)

      val text = LocationUtils.getLocationText(location)
      intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
      val servicePendingIntent = PendingIntent.getService(
        this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
      )
      val activityPendingIntent = PendingIntent.getActivity(
          this, 0, Intent(this, MainActivity::class.java), 0
      )

      val builder = NotificationCompat.Builder(this).addAction(
        R.drawable.ic_launch, getString(R.string.launch_activity), activityPendingIntent
      ).addAction(
          R.drawable.ic_cancel, getString(R.string.remove_location_updates), servicePendingIntent
        ).setContentText(text).setContentTitle(LocationUtils.getLocationTitle(this)).setOngoing(true).setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
        .setSmallIcon(R.drawable.ic_notification_parking).setTicker(text).setWhen(System.currentTimeMillis())
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        builder.setChannelId(CHANNEL_ID)
      }

      return builder.build()
    }

  override fun onCreate() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    locationCallback = object : LocationCallback() {
      override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        onNewLocation(locationResult.lastLocation)
      }
    }

    createLocationRequest()
    getLastLocation()

    val handlerThread = HandlerThread(TAG)
    handlerThread.start()
    serviceHandler = Handler(handlerThread.looper)
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.app_name)
      val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(mChannel)
    }
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Log.i(TAG, "Service started")
    val startedFromNotification = intent.getBooleanExtra(
      EXTRA_STARTED_FROM_NOTIFICATION, false
    )

    // We got here because the user decided to remove location updates from the notification.
    if (startedFromNotification) {
      removeLocationUpdates()
      stopSelf()
    }
    // Tells the system to not try to recreate the service after it has been killed.
    return START_NOT_STICKY
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    changingConfiguration = true
  }

  override fun onBind(intent: Intent): IBinder? {
    stopForeground(true)
    changingConfiguration = false
    return binder
  }

  override fun onRebind(intent: Intent) {
    stopForeground(true)
    changingConfiguration = false
    super.onRebind(intent)
  }

  override fun onUnbind(intent: Intent): Boolean {
    if (!changingConfiguration) {
      startForeground(NOTIFICATION_ID, notification)
    }
    return true
  }

  override fun onDestroy() {
    serviceHandler.removeCallbacksAndMessages(null)
  }

  fun requestLocationUpdates() {
    startService(Intent(applicationContext, LocationUpdatesService::class.java))
    try {
      fusedLocationClient.requestLocationUpdates(
        locationRequest, locationCallback, Looper.myLooper()
      )
    } catch (unlikely: SecurityException) {
      Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
    }

  }

  fun removeLocationUpdates() {
    try {
      fusedLocationClient.removeLocationUpdates(locationCallback)
      stopSelf()
    } catch (unlikely: SecurityException) {
      Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
    }

  }

  private fun getLastLocation() {
    try {
      fusedLocationClient.lastLocation.addOnCompleteListener {task ->
        if (task.isSuccessful && task.result != null) {
          location = task.result
        } else {
          Log.w(TAG, "Failed to get location.")
        }
      }
    } catch (unlikely: SecurityException) {
      Log.e(TAG, "Lost location permission.$unlikely")
    }

  }

  private fun onNewLocation(location: Location) {
    this.location = location

    val intent = Intent(ACTION_BROADCAST)
    intent.putExtra(EXTRA_LOCATION, location)
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    broadcastLocation(location)

    if (serviceIsRunningInForeground(this)) {
      notificationManager.notify(NOTIFICATION_ID, notification)
    }
  }

  private fun broadcastLocation(location: Location) {
    val inputData = Data.Builder()
                        .putDouble(CoordinatesHandleWorker.LOCATION_LAT, location.latitude)
                        .putDouble(CoordinatesHandleWorker.LOCATION_LONG, location.longitude)
                        .putLong(CoordinatesHandleWorker.LOCATION_TIME, location.time).build()
    val coordinatesHandleWork = OneTimeWorkRequest
      .Builder(CoordinatesHandleWorker::class.java)
      .setInputData(inputData)
      .build()
    WorkManager
      .getInstance()
      .enqueue(coordinatesHandleWork)
  }

  private fun createLocationRequest() {
    locationRequest = LocationRequest()
    locationRequest.interval = UPDATE_INTERVAL_IN_MILLISECONDS
    locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
  }

  private fun serviceIsRunningInForeground(context: Context): Boolean {
    val manager = context.getSystemService(
      Context.ACTIVITY_SERVICE
    ) as ActivityManager
    for (service in manager.getRunningServices(
      Integer.MAX_VALUE
    )) {
      if (javaClass.name == service.service.className) {
        if (service.foreground) {
          return true
        }
      }
    }
    return false
  }

  companion object {
    private const val PACKAGE_NAME = "com.maestro.parking"
    private val TAG = LocationUpdatesService::class.java.simpleName
    private const val CHANNEL_ID = "com.maestro.parking.channel"
    const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
    const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
    private const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private const val NOTIFICATION_ID = 210560562
  }
}

