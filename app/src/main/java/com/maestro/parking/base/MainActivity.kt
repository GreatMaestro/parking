package com.maestro.parking.base

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.maestro.parking.BuildConfig
import com.maestro.parking.R
import com.maestro.parking.core.redux.MainState
import com.maestro.parking.location.service.LocationUpdatesService
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import su.rusfearuth.arch.kdroidredux.rx.store.Store

class MainActivity : AppCompatActivity() {

  lateinit var storeDisposable: Disposable
  private val store: Store<MainState> by inject { parametersOf(this) }
  private var state = store.state.location.makeCopy()

  private val onNavigateListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
    when (item.itemId) {
      R.id.navigation_parkings -> {
        return@OnNavigationItemSelectedListener true
      }
      R.id.navigation_map      -> {
        return@OnNavigationItemSelectedListener true
      }
    }
    false
  }

  private val TAG = MainActivity::class.java.simpleName
  private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
  private var service: LocationUpdatesService? = null
  private var bound = false
  private val serviceConnection = object : ServiceConnection {

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
      val binder = service as LocationUpdatesService.LocalBinder
      this@MainActivity.service = binder.service
      binder.service.requestLocationUpdates()
      bound = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
      service = null
      bound = false
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    navigation.setOnNavigationItemSelectedListener(onNavigateListener)
    navigation.selectedItemId = R.id.navigation_parkings

    if (!checkPermissions()) {
      requestPermissions()
    }
  }

  override fun onStart() {
    super.onStart()

    bindService(
      Intent(this, LocationUpdatesService::class.java), serviceConnection, Context.BIND_AUTO_CREATE
    )

    // Check that the user hasn't revoked permissions by going to Settings.
    if (!checkPermissions()) {
      requestPermissions()
    } else {
      service?.requestLocationUpdates()
    }
  }

  override fun onResume() {
    super.onResume()
    storeDisposable = store.subscribe {
      if (state == it.location) {
        return@subscribe
      }
      state = it.location.makeCopy()
    }
  }

  override fun onStop() {
    if (bound) {
      unbindService(serviceConnection)
      bound = false
    }
    super.onStop()
  }

  /**
   * Returns the current state of the permissions needed.
   */
  private fun checkPermissions(): Boolean {
    return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
      this, Manifest.permission.ACCESS_FINE_LOCATION
    )
  }

  private fun requestPermissions() {
    val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
      this, Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (shouldProvideRationale) {
      Snackbar.make(root, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE
      ).setAction(R.string.ok) {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
      }.show()
    } else {
      ActivityCompat.requestPermissions(
        this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE
      )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
  ) {
    Log.i(TAG, "onRequestPermissionResult")
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
      if (grantResults.isEmpty()) {
        Log.i(TAG, "User interaction was cancelled.")
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        service?.requestLocationUpdates()
      } else {
        Snackbar.make(root, R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.settings) {
          val intent = Intent()
          intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
          intent.data = uri
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
          startActivity(intent)
        }.show()
      }
    }
  }

  private fun changeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
                          .replace(R.id.container, fragment)
                          .commitAllowingStateLoss()
  }
}
