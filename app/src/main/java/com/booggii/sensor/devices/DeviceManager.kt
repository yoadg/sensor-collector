package com.booggii.sensor.devices

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.booggii.sensor.R
import com.booggii.sensor.model.*
import com.booggii.sensor.services.Logger
import com.booggii.sensor.services.Scheduler
import com.booggii.sensor.services.Streamer
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DeviceManager : Publisher<HRData>, DeviceStateListener {
    private const val TAG = "Device Manager"
    const val BILLION = 1000000000L
    private val polar = Polar()
    lateinit var settings: Settings
    val deviceState = DeviceState()
    val ecgRates = intArrayOf(130)
    val ecgRes = intArrayOf(14)
    val accRates = intArrayOf(25, 50, 100, 200)
    val accRes = intArrayOf(16)
    val accRanges = intArrayOf(2,4,8)
    private var subscriber: Subscriber<in HRData>? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var formatter  = DateTimeFormatter.ISO_DATE_TIME
    private lateinit var jobInfo: JobInfo
    //private lateinit var jobScheduler: JobScheduler

    @RequiresApi(Build.VERSION_CODES.N)
    fun init(context: Context) {
        polar.init(context, this)
        val pref = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        settings = Settings(pref)
        Streamer.init(context)
        /*
        val component = ComponentName(context, Scheduler::class.java)
        val min = JobInfo.getMinPeriodMillis()
        Logger.debug(TAG, "Min period: $min")
        jobInfo = JobInfo.Builder(1, component).setPeriodic(min).build()
        jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    */
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
        Logger.debug(TAG, "Status changed: $status")
        deviceState.statusChanged(status)
        /*
        if (status == Status.CONNECTED) {
            Logger.debug(TAG, "Scheduling job")
            jobScheduler.schedule(jobInfo)
        }
        if (status == Status.DISCONNECTED) {
            jobScheduler.cancel(jobInfo.id)
        }
         */
    }


    override fun streamReady(stream: String) {
        deviceState.streamReady(stream)
        //polar.setTime()
        if (stream == "ECG" && settings.ecg) {
            polar.ecg(settings.ecgResolution, settings.ecgSampleRate)
        }
        if (stream == "ACC" && settings.acc) {
            polar.acc(settings.accResolution, settings.accSampleRate, settings.accRange)
        }
    }

    override fun btPowerChanged(power: Boolean) {
        deviceState.btPowerChanged(power)
    }

    override fun batteryLevelChanged(level: Int) {
        deviceState.batteryLevelChanged(level)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun hrDataReceived(data: HRData) {
        with (data) {
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault())
            var rrsms = 0
            if (rrsMs.size > 0) rrsms = rrsMs[0]
            val formattedData = "${settings.userId},$hr,$rrsms,${dateTime.format(formatter)}\n"
            //Logger.debug(TAG, "HR: $formattedData")
            Streamer.addData(formattedData, Streamer.HR_STREAM)
            subscriber?.onNext(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun ecgDataReceived(data: ECGData) {
        with (data) {
            var dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault())
            samples.forEach {
                val formattedData = "${settings.userId},$it,${dateTime.format(formatter)}\n"
                //Logger.debug(TAG, data)
                Streamer.addData(formattedData, Streamer.ECG_STREAM)
                dateTime = dateTime.minusNanos(BILLION / settings.ecgSampleRate)
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun accDataReceived(data: ACCData) {
        with (data) {
            var dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault())
            samples.forEach {
                val formattedData = "${settings.userId},${it.x},${it.y},${it.z},${dateTime.format(formatter)}\n"
                //Logger.debug(TAG, data)
                Streamer.addData(formattedData, Streamer.ACC_STREAM)
                dateTime = dateTime.minusNanos(BILLION / settings.accSampleRate)

            }

        }

    }

    override fun subscribe(s: Subscriber<in HRData?>) {
        subscriber = s
    }


}