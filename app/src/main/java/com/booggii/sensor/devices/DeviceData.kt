package com.booggii.sensor.devices

enum class Status {
    DISCONNECTED, CONNECTING, CONNECTED
}

data class HRData(val timestamp:Long, val hr: Int, val rrsMs: List<Int>, val  rrs: List<Int>)

data class ECGData(val timestamp: Long, val samples: List<Int>)

data class Point(val x: Int, val y: Int, val z: Int) {
    override fun toString(): String {
        return "X: $x, Y: $y, Z: $z"
    }
}

data class ACCData(val timestamp: Long, val samples: List<Point>)
