package com.booggii.sensor.services

import android.util.Log
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

object Logger: Publisher<String> {
    private var subscriber: Subscriber<in String>? = null

    fun info(tag: String, message: String) {
        Log.i(tag, message)
        subscriber?.onNext(message)
    }

    fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun error(tag: String, message: String, exception: Throwable?) {
        Log.e(tag, message, exception)
    }

    fun warning(tag: String, message: String) {
        Log.w(tag, message)
        subscriber?.onNext(message)

    }

    override fun subscribe(s: Subscriber<in String>?) {
        subscriber = s
    }

}