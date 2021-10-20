package com.booggii.sensor.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.booggii.sensor.devices.*

class DeviceState : ViewModel(), DeviceStateListener {
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

    private val _hrStream = LiveDataReactiveStreams.fromPublisher(DeviceManager)

    val hrStream: LiveData<HRData>
        get() = _hrStream

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


    override fun btPowerChanged(power: Boolean) {
        _btPower.value = power
    }

    override fun batteryLevelChanged(level: Int) {
        _batteryLevel.value = level
    }

    override fun hrDataReceived(data: HRData) {
        TODO("Not yet implemented")
    }

    override fun ecgDataReceived(data: ECGData) {
        TODO("Not yet implemented")
    }

    override fun accDataReceived(data: ACCData) {
        TODO("Not yet implemented")
    }

    override fun statusChanged(status: Status) {
        _status.value = status
    }

    override fun streamReady(stream: String) {
        _streams.value!!.add(stream)
    }
}