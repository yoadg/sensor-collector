package com.booggii.sensor

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.booggii.sensor.databinding.ActivityMainBinding
import com.booggii.sensor.devices.DeviceManager
import com.booggii.sensor.ui.TabsAdapter
import com.booggii.sensor.services.Logger
import com.booggii.sensor.services.Streamer
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun FloatingActionButton.toggleEnabled(enabled: Boolean) {
    Logger.debug("FAB", "Enabled: $enabled")
    if (enabled) {
        isEnabled = true
        backgroundTintList = ColorStateList.valueOf(Color.rgb(0, 204, 0))
    } else {
        isEnabled = false
        backgroundTintList = ColorStateList.valueOf(Color.GRAY)
    }
}

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            DeviceManager.init(this)
        } catch (e: Throwable) {
            Logger.error(TAG, "Failed to initialize", e)
            finish()
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabs
        viewPager = binding.viewPager

        tabLayout!!.addTab(tabLayout!!.newTab().setText("Device"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Settings"))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Data"))
        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = TabsAdapter(this, supportFragmentManager, tabLayout!!.tabCount)
        viewPager!!.adapter = adapter

        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager!!.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val fab: FloatingActionButton = binding.fab
        fab.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
        DeviceManager.deviceState.btPower.observe(this, { power ->
            fab.toggleEnabled(power)
        })

        fab.setOnClickListener { _ ->
            if (DeviceManager.deviceState.isConnected()) {
                DeviceManager.disconnect()
            }
            else if (!DeviceManager.deviceState.isConnecting()) {
                DeviceManager.connect()
            }
        }

        DeviceManager.deviceState.status.observe(this, { _ ->
            if (DeviceManager.deviceState.isConnected()) {
                fab.setImageResource(android.R.drawable.ic_media_pause)
                fab.backgroundTintList = ColorStateList.valueOf(Color.rgb(255,51,51))

            } else if (DeviceManager.deviceState.canConnect()){
                fab.setImageResource(android.R.drawable.ic_media_play)
                fab.backgroundTintList = ColorStateList.valueOf(Color.rgb(0,204,0))
            } else {
                fab.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            Logger.debug(TAG, "BT ready")
        }
    }

    public override fun onPause() {
        super.onPause()
        DeviceManager.backgroundEntered()
    }

    public override fun onResume() {
        super.onResume()
        DeviceManager.foregroundEntered()
    }

    public override fun onDestroy() {
        super.onDestroy()
        DeviceManager.shutDown()
    }


}