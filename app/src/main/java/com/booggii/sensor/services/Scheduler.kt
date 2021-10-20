package com.booggii.sensor.services
import android.app.job.JobParameters
import android.app.job.JobService

class Scheduler : JobService() {
    companion object {
        const val TAG = "Scheduler"
    }
    override fun onStopJob(p0: JobParameters?): Boolean {
        Logger.debug(TAG, "onStopJob")
        return false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Logger.debug(TAG, "onStartJob")
        Streamer.send()
        return false
    }
}