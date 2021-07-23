package com.booggii.sensor.devices

import android.content.SharedPreferences

const val DEFAULT_DEVICE = "96CE632E"

class Settings(val pref: SharedPreferences) {
    companion object {
        const val DEVICE_ID = "device_id"
        const val ECG = "ecg"
        const val ECG_SAMPLE_RATE = "ecg_sample_rate"
        const val ECG_RESOLUTION = "ecg_resolution"
        const val ACC = "acc"
        const val ACC_RANGE = "acc_range"
        const val ACC_SAMPLE_RATE = "acc_sample_rate"
        const val ACC_RESOLUTION = "acc_resolution"

    }

    var deviceId: String
        get() = pref.getString(DEVICE_ID, DEFAULT_DEVICE)!!
        set(deviceId) {
            with (pref.edit()) {
                putString(DEVICE_ID, deviceId)
                apply()
            }
        }

    var ecg: Boolean
        get() = pref.getBoolean(ECG, true)
        set(ecg) {
            with (pref.edit()) {
                putBoolean(ECG, ecg)
                apply()
            }
        }

    var ecgResolution: Int
        get() = pref.getInt(ECG_RESOLUTION, 14)
        set(ecgResolution) {
            with (pref.edit()) {
                putInt(ECG_RESOLUTION, ecgResolution)
                apply()
            }
        }

    var ecgSampleRate: Int
        get() = pref.getInt(ECG_SAMPLE_RATE, 130)
        set(ecgSampleRate) {
            with (pref.edit()) {
                putInt(ECG_SAMPLE_RATE, ecgSampleRate)
                apply()
            }
        }

    var acc: Boolean
        get() = pref.getBoolean(ACC, true)
        set(acc) {
            with (pref.edit()) {
                putBoolean(ACC, acc)
                apply()
            }
        }

    var accRange: Int
        get() = pref.getInt(ACC_RANGE, 2)
        set(accRange) {
            with (pref.edit()) {
                putInt(ACC_RANGE, accRange)
                apply()
            }
        }

    var accResolution: Int
        get() = pref.getInt(ACC_RESOLUTION, 16)
        set(accResolution) {
            with (pref.edit()) {
                putInt(ACC_RESOLUTION, accResolution)
                apply()
            }
        }

    var accSampleRate: Int
        get() = pref.getInt(ACC_SAMPLE_RATE, 25)
        set(accSampleRate) {
            with (pref.edit()) {
                putInt(ACC_SAMPLE_RATE, accSampleRate)
                apply()
            }
        }

/*

*/


}
