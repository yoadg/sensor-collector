package com.booggii.sensor.services

import android.content.Context
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.*
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.regions.Regions
import com.booggii.sensor.utils.CoroutinesAsyncTask


object Streamer {
    const val TAG = "Streamer"
    const val FIREHOSE_DIRECTORY = "BOOGGII"
    const val HR_STREAM = "booggii-polar-hr"
    const val ECG_STREAM = "booggii-polar-ecg"
    const val ACC_STREAM = "booggii-polar-acc"
    const val TASK_NAME = "FirehoseSender"

    private lateinit var recorder: KinesisFirehoseRecorder
    private var sending = false
    private var lastSent = 0L

    fun init(context: Context) {
        AWSMobileClient.getInstance().initialize(context, object : Callback<UserStateDetails> {
            override fun onResult(details: UserStateDetails?) {
                Logger.info(TAG, details?.userState.toString())
            }

            override fun onError(e: Exception) {
                Logger.error(TAG, "Initialization error.", e)
            }
        })
        recorder = KinesisFirehoseRecorder(
            context.getDir(FIREHOSE_DIRECTORY, 0),
            Regions.EU_WEST_1,
            AWSMobileClient.getInstance()
        )
        recorder.deleteAllRecords()
        Logger.info(TAG, "Kinesis Firehose Recorder ready")
    }

    fun addData(data: String, stream: String) {
        Logger.debug(TAG, "$stream: $data")
        recorder.saveRecord(data, stream)
        if (!sending && System.currentTimeMillis() - lastSent > 60000) {
            send()
        }
    }

    fun submitData() {
        Logger.debug(TAG, "Submitting records")
        recorder.submitAllRecords()
        lastSent = System.currentTimeMillis()
        Logger.debug(TAG, "Submitted records")
    }


    fun send() {
        sending = true
        object : CoroutinesAsyncTask<Void?, Void?, Void?>(TASK_NAME) {

            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    submitData()
                } catch (e: Throwable) {
                    Logger.error(TAG, "Failed to submit records", e)
                } finally {
                    sending = false
                }
                return null
            }
        }.execute()
    }


}