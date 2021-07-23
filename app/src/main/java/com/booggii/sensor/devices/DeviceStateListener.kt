package com.booggii.sensor.devices

data class HRData(val hr: Int, val rrsMs: List<Int>, val  rrs: List<Int>)

data class ECGData(val timestamp: Long, val samples: List<Int>)

data class Point(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "X: $x, Y: $y, Z: $z"
    }
}

data class ACCData(val timestamp: Long, val samples: List<Point>)

interface DeviceStateListener {

    fun statusChanged(status: Status)

    fun streamReady(stream: String)

    fun btPowerChanged(power: Boolean)

    fun batteryLevelChanged(level: Int)

    fun hrDataReceived(data: HRData)

    fun ecgDataReceived(data: ECGData)

    fun accDataReceived(data: ACCData)
}