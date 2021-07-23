package com.booggii.sensor.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.booggii.sensor.devices.Status

class DeviceState : ViewModel() {
    private val _status = MutableLiveData<Status>()

    val status: LiveData<Status>
        get() = _status

    private val _btPower = MutableLiveData<Boolean>()

    val btPower: LiveData<Boolean>
        get() = _btPower

    private val _streams = MutableLiveData<MutableSet<String>>()

    val streams: LiveData<MutableSet<String>>
        get() = _streams

    private val _batteryLevel = MutableLiveData<Int>()

    val batteryLevel
        get() = _batteryLevel

    init {
        _status.value = Status.DISCONNECTED
        _btPower.value = false
        _streams.value = HashSet(3)
    }

    fun isConnected(): Boolean {
        return _status.value == Status.CONNECTED
    }

    fun isConnecting(): Boolean {
        return _status.value == Status.CONNECTING
    }

    fun canConnect(): Boolean {
        return _status.value == Status.DISCONNECTED && _btPower.value == true
    }

    fun onStatusChange(status: Status) {
        _status.value = status
    }

    fun onBtPowerChange(power: Boolean) {
        _btPower.value = power
    }

    fun onStreamReady(stream: String) {
        _streams.value!!.add(stream)
    }

    fun onBatteryLevelChange(level: Int) {
        _batteryLevel.value = level
    }
}