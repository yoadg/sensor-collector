package com.booggii.sensor.devices

import android.content.Context
import android.os.Handler
import com.booggii.sensor.services.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.*
import java.util.*


class Polar {
    companion object {
        const val MILLION = 1000000L
        private const val TAG = "Polar"
        private const val API_LOGGER_TAG = "API LOGGER"
        private const val EPOCH = 946684800000L // 2000-01-01
    }

    private lateinit var api: PolarBleApi
    private var deviceConnected = false
    private var bluetoothEnabled = false
    private var _deviceId: String = "96CE632E"
    private var _deviceName: String? = null
    val deviceName
        get() = _deviceName
    private lateinit var listener: DeviceStateListener

    private lateinit var broadcastDisposable: Disposable
    private var scanDisposable: Disposable? = null
    private var autoConnectDisposable: Disposable? = null
    private var ecgDisposable: Disposable? = null
    private var accDisposable: Disposable? = null


    fun init(context: Context, listener: DeviceStateListener) {
        // Notice PolarBleApi.ALL_FEATURES are enabled
        try {
            api = PolarBleApiDefaultImpl.defaultImplementation(context, PolarBleApi.ALL_FEATURES)
            this.listener = listener
            Logger.info(TAG, "Polar SDK version: " + PolarBleApiDefaultImpl.versionInfo())
            api.setPolarFilter(false)
            api.setAutomaticReconnection(true)
            api.setApiLogger { s: String -> Logger.debug(API_LOGGER_TAG, s) }
            api.setApiCallback(object : PolarBleApiCallback() {
                override fun blePowerStateChanged(powered: Boolean) {
                    Logger.debug(TAG, "BLE power: $powered")
                    bluetoothEnabled = powered
                    listener.btPowerChanged(powered)
                }

                override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                    Logger.debug(TAG, "CONNECTED: " + polarDeviceInfo.deviceId)
                    _deviceId = polarDeviceInfo.deviceId
                    _deviceName = polarDeviceInfo.name
                    deviceConnected = true
                    listener.statusChanged(Status.CONNECTED)
                }

                override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                    Logger.debug(TAG, "CONNECTING: " + polarDeviceInfo.deviceId)
                    listener.statusChanged(Status.CONNECTING)
                }

                override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                    Logger.info(TAG, "DISCONNECTED: " + polarDeviceInfo.deviceId)
                    deviceConnected = false
                    listener.statusChanged(Status.DISCONNECTED)
                }

                override fun streamingFeaturesReady(
                    identifier: String, features: Set<PolarBleApi.DeviceStreamingFeature>
                ) {
                    for (feature in features) {
                        Logger.debug(TAG, "STREAMING feature $feature is ready")
                    }
                    Handler(context.mainLooper).post {
                        for (feature in features) {
                            listener.streamReady(feature.name)
                        }
                    }


                }

                override fun hrFeatureReady(identifier: String) {
                    Logger.debug(TAG, "HR READY: $identifier")
                    listener.streamReady("HR")
                    // hr notifications are about to start
                }

                override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                    Logger.debug(TAG, "uuid: $uuid value: $value")
                }

                override fun batteryLevelReceived(identifier: String, level: Int) {
                    Logger.debug(TAG, "BATTERY LEVEL: $level")
                    listener.batteryLevelChanged(level)
                }

                override fun hrNotificationReceived(identifier: String, data: PolarHrData) {
                    val timestamp = System.currentTimeMillis()
                    with(data) {
                        Logger.debug(
                            TAG,
                            "HR value: $hr rrsMs: $rrsMs rr: $rrs" // contact: ${data.contactStatus} , ${data.contactStatusSupported}
                        )
                        val hrData = HRData(timestamp, hr, rrsMs, rrs)
                        listener.hrDataReceived(hrData)
                    }
                }

            })
        } catch (e: Throwable) {
            Logger.error(TAG, "Failed to initialize", e)
        }
    }


    fun listenBroadcast() {
        Logger.debug(TAG, "Listen broadcast")
        if (!this::broadcastDisposable.isInitialized || broadcastDisposable.isDisposed) {
            broadcastDisposable = api.startListenForPolarHrBroadcasts(null)
                .subscribe(
                    { polarBroadcastData: PolarHrBroadcastData ->
                        Logger.info(
                            TAG,
                            "HR BROADCAST ${polarBroadcastData.polarDeviceInfo.deviceId} " +
                                    "HR: ${polarBroadcastData.hr} " +
                                    "batt: ${polarBroadcastData.batteryStatus}"
                        )
                    },
                    { error: Throwable ->
                        Logger.error(TAG, "Broadcast listener failed", error)
                    },
                    { Logger.debug(TAG, "complete") }
                )
        } else {
            broadcastDisposable.dispose()
        }
    }

    fun connect(deviceId: String) {
        if (deviceConnected) {
            Logger.warning(TAG, "Already connected")
        } else {
            try {
                Logger.debug(TAG, "Connecting to ${DeviceManager.settings.deviceId}")
                api.connectToDevice(deviceId)
            } catch (polarInvalidArgument: PolarInvalidArgument) {
                Logger.error(TAG, "Failed to connect", polarInvalidArgument)
            }
        }
    }

    fun disconnect() {
        if (!deviceConnected) {
            Logger.warning(TAG, "Not connected")
        } else {
            disposeAllStreams()
            try {
                api.disconnectFromDevice(_deviceId)
            } catch (polarInvalidArgument: PolarInvalidArgument) {
                Logger.error(TAG, "Failed to disconnect", polarInvalidArgument)
            }
        }
    }

    fun autoConnect() {
        if (autoConnectDisposable != null) {
            autoConnectDisposable?.dispose()
        }
        autoConnectDisposable = api.autoConnectToDevice(-50, "180D", null)
            .subscribe(
                { Logger.debug(TAG, "auto connect search complete") },
                { throwable: Throwable -> Logger.error(TAG, "Auto connect failed", throwable) }
            )
    }

    fun scan() {
        val isDisposed = scanDisposable?.isDisposed ?: true
        if (isDisposed) {
            scanDisposable = api.searchForDevice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        Logger.debug(
                            TAG,
                            "Polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable
                        )
                    },
                    { error: Throwable ->
                        Logger.error(TAG, "Device scan failed", error)
                    },
                    { Logger.debug(TAG, "complete") }
                )
        } else {
            scanDisposable?.dispose()
        }
    }

    fun ecg(resolution: Int, sampleRate: Int) {
        val isDisposed = ecgDisposable?.isDisposed ?: true
        if (isDisposed) {
            val settings = HashMap<PolarSensorSetting.SettingType, Int>(2)
            settings[PolarSensorSetting.SettingType.RESOLUTION] = resolution
            settings[PolarSensorSetting.SettingType.SAMPLE_RATE] = sampleRate
            ecgDisposable = api.startEcgStreaming(_deviceId, PolarSensorSetting(settings))
                .subscribe(
                    { polarEcgData: PolarEcgData ->
                        //Logger.debug(TAG, "ECG yV: ${polarEcgData.samples}")
                        val timestamp = EPOCH + (polarEcgData.timeStamp / MILLION)
                        //Logger.debug(TAG, "$timestamp")
                        listener.ecgDataReceived(
                            ECGData(timestamp, polarEcgData.samples)
                        )
                    },
                    { error: Throwable ->
                        Logger.error(TAG, "ECG stream failed", error)
                    },
                    { Logger.debug(TAG, "ECG stream complete") }
                )
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable?.dispose()
        }
    }

    fun acc(resolution: Int, sampleRate: Int, range: Int) {
        val isDisposed = accDisposable?.isDisposed ?: true
        if (isDisposed) {
            val settings = HashMap<PolarSensorSetting.SettingType, Int>(2)
            settings[PolarSensorSetting.SettingType.RESOLUTION] = resolution
            settings[PolarSensorSetting.SettingType.SAMPLE_RATE] = sampleRate
            settings[PolarSensorSetting.SettingType.RANGE] = range
            accDisposable = api.startAccStreaming(_deviceId, PolarSensorSetting(settings))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarAccData: PolarAccelerometerData ->
                        /*for (data in polarAccData.samples) {
                            Logger.debug(TAG, "ACC x: ${data.x} y: ${data.y} z: ${data.z}")
                        }*/
                        listener.accDataReceived(
                            ACCData(EPOCH + (polarAccData.timeStamp / MILLION),
                                polarAccData.samples.map { data ->
                                    Point(
                                        data.x,
                                        data.y,
                                        data.z
                                    )
                                })
                        )
                    },
                    { error: Throwable ->
                        Logger.error(TAG, "ACC stream failed", error)
                    },
                    {
                        Logger.debug(TAG, "ACC stream complete")
                    }
                )
        } else {
            // NOTE dispose will stop streaming if it is "running"
            accDisposable?.dispose()
        }
    }

    fun requestAvailableStreamSettings(feature: DeviceStreamingFeature): Single<PolarSensorSetting> {
        return api.requestStreamSettings(_deviceId, feature)
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { error: Throwable ->
                val errorString = "Settings are not available for feature $feature. REASON: $error"
                Logger.warning(TAG, errorString)
                PolarSensorSetting(emptyMap())
            }
    }

    fun requestAllStreamSettings(feature: DeviceStreamingFeature): Single<PolarSensorSetting> {
        return api.requestFullStreamSettings(_deviceId, feature)
            .onErrorReturn { error: Throwable ->
                Logger.warning(
                    TAG,
                    "Full stream settings are not available for feature $feature. REASON: $error"
                )
                PolarSensorSetting(emptyMap())
            }
    }

    fun setTime() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        api.setLocalTime(this._deviceId, calendar)
            .subscribe(
                { Logger.debug(TAG, "time ${calendar.time} set to device") },
                { error: Throwable -> Logger.error(TAG, "set time failed", error) },
            )

    }

    fun backgroundEntered() {
        api.backgroundEntered()
    }

    fun foregroundEntered() {
        api.foregroundEntered()
    }

    fun shutDown() {
        api.shutDown()
    }

    private fun disposeAllStreams() {
        ecgDisposable?.dispose()
        accDisposable?.dispose()
    }


}