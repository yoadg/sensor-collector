package com.booggii.sensor.plotter

import android.graphics.Paint
import com.androidplot.xy.AdvancedLineAndPointRenderer
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYSeries
import java.util.*


class Plotter(title: String?) {
    private var listener: PlotterListener? = null
    private val plotNumbers = arrayOfNulls<Number>(500)
    private val formatter: FadeFormatter
    private val series: XYSeries
    private var dataIndex = 0
    fun getSeries(): SimpleXYSeries {
        return series as SimpleXYSeries
    }

    fun sendSingleSample(mV: Float) {
        plotNumbers[dataIndex] = mV
        if (dataIndex >= plotNumbers.size - 1) {
            dataIndex = 0
        }
        if (dataIndex < plotNumbers.size - 1) {
            plotNumbers[dataIndex + 1] = null
        }
        (series as SimpleXYSeries).setModel(
            Arrays.asList(*plotNumbers),
            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY
        )
        dataIndex++
        listener!!.update()
    }

    fun setListener(listener: PlotterListener?) {
        this.listener = listener
    }

    //Custom paint stroke to generate a "fade" effect
    class FadeFormatter(private val trailSize: Int) : AdvancedLineAndPointRenderer.Formatter() {
        override fun getLinePaint(thisIndex: Int, latestIndex: Int, seriesSize: Int): Paint {
            // offset from the latest index:
            val offset: Int = if (thisIndex > latestIndex) {
                latestIndex + (seriesSize - thisIndex)
            } else {
                latestIndex - thisIndex
            }
            val scale = 255f / trailSize
            val alpha = (255 - offset * scale).toInt()
            linePaint.alpha = Math.max(alpha, 0)
            return linePaint
        }
    }

    companion object {
        private const val TAG = "Plotter"
    }

    init {
        for (i in 0 until plotNumbers.size - 1) {
            plotNumbers[i] = 60
        }
        formatter = FadeFormatter(800)
        formatter.isLegendIconEnabled = false
        series = SimpleXYSeries(
            listOf(*plotNumbers),
            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
            title
        )
    }
}
