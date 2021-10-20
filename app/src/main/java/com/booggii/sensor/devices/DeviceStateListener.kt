package com.booggii.sensor.devices

import com.booggii.sensor.model.*

interface DeviceStateListener {

    fun statusChanged(status: Status)

    fun streamReady(stream: String)

    fun btPowerChanged(power: Boolean)

    fun batteryLevelChanged(level: Int)

    fun hrDataReceived(data: HRData)

    fun ecgDataReceived(data: ECGData)

    fun accDataReceived(data: ACCData)
}