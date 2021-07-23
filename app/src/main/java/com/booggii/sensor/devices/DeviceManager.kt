package com.booggii.sensor.devices

import android.content.Context
import androidx.databinding.BaseObservable
import com.booggii.sensor.R
import com.booggii.sensor.model.DeviceState
import com.booggii.sensor.utils.Logger

enum class Status {
    DISCONNECTED, CONNECTING, CONNECTED
}

object DeviceManager : BaseObservable(), DeviceStateListener  {
    private const val TAG = "Device Manager"

    private val polar = Polar()
    lateinit var settings: Settings
    val deviceState = DeviceState()
    val ecgRates = intArrayOf(130)
    val ecgRes = intArrayOf(14)
    val accRates = intArrayOf(25, 50, 100, 200)
    val accRes = intArrayOf(16)
    val accRanges = intArrayOf(2,4,8)

    fun init(context: Context) {
        polar.init(context, this)
        val pref = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        settings = Settings(pref)
        Logger.debug(TAG, "Init completed")
    }

    fun getDeviceName(): String? {
        return polar.deviceName
    }


    fun connect() {
        polar.connect(settings.deviceId)
    }

    fun disconnect() {
        polar.disconnect()
    }

    fun backgroundEntered() {
        polar.backgroundEntered()
    }

    fun foregroundEntered() {
        polar.foregroundEntered()
    }

    fun shutDown() {
        polar.shutDown()
    }

    override fun statusChanged(status: Status) {
        deviceState.onStatusChange(status)
    }

    override fun streamReady(stream: String) {
        deviceState.onStreamReady(stream)
        if (stream == "ECG" && settings.ecg) {
            polar.ecg(settings.ecgResolution, settings.ecgSampleRate)
        }
        if (stream == "ACC" && settings.acc) {
            polar.acc(settings.accResolution, settings.accSampleRate, settings.accRange)
        }
    }

    override fun btPowerChanged(power: Boolean) {
        deviceState.onBtPowerChange(power)
    }

    override fun batteryLevelChanged(level: Int) {
        deviceState.onBatteryLevelChange(level)
    }

    override fun hrDataReceived(data: HRData) {
        Logger.info(TAG,"HR: ${data.hr} rrsMS: ${data.rrsMs} rrs: ${data.rrs}")
    }

    override fun ecgDataReceived(data: ECGData) {
        Logger.info(TAG, "ECG: ${data.samples}")
    }

    override fun accDataReceived(data: ACCData) {
        Logger.info(TAG, "ACC: ${data.samples}")
    }

}