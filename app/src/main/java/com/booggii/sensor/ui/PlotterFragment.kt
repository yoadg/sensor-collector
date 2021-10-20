package com.booggii.sensor.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.androidplot.util.PixelUtils
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.StepMode
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import com.booggii.sensor.databinding.FragmentPlotterBinding
import com.booggii.sensor.model.DeviceState
import com.booggii.sensor.plotter.PlotterListener
import com.booggii.sensor.plotter.TimePlotter
import java.text.DecimalFormat

class PlotterFragment : Fragment(), PlotterListener {
        companion object {
            const val TAG = "PlotterFragment"
        }
        private var _binding: FragmentPlotterBinding? = null
        // This property is only valid between onCreateView and onDestroyView.
        private val binding get() = _binding!!
        private lateinit var viewModel: DeviceState
        private lateinit var plot: XYPlot
        private lateinit var plotter: TimePlotter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlotterBinding.inflate(inflater, container, false)
        val root = binding.root
        plot = binding.plot

        initPlotter()
        viewModel = ViewModelProvider(this).get(DeviceState::class.java)
        viewModel.hrStream.observe(viewLifecycleOwner, { data ->
            plotter.addValues(data)
        })
        return root
    }

    private fun initPlotter() {
        PixelUtils.init(context)
        plotter = TimePlotter()
        plotter.setListener(this)
        with (plot) {
            addSeries(plotter.hrSeries, plotter.hrFormatter)
            addSeries(plotter.rrSeries, plotter.rrFormatter)
            setRangeBoundaries(50, 100, BoundaryMode.AUTO)
            setDomainBoundaries(0, 360000, BoundaryMode.AUTO)
            // Left labels will increment by 10
            // Left labels will increment by 10
            setRangeStep(StepMode.INCREMENT_BY_VAL, 10.0)
            setDomainStep(StepMode.INCREMENT_BY_VAL, 60000.0)
            // Make left labels be an integer (no decimal places)
            // Make left labels be an integer (no decimal places)
            graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = DecimalFormat("#")
            // These don't seem to have an effect
            // These don't seem to have an effect
            linesPerRangeLabel = 2
        }
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun update() {
        plot.redraw()
    }

}