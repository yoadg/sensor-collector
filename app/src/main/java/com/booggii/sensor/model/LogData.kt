package com.booggii.sensor.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import com.booggii.sensor.utils.Logger

class LogData : ViewModel() {
    private val _stream = LiveDataReactiveStreams.fromPublisher(Logger)

    val logStream: LiveData<String>
        get() = _stream

}