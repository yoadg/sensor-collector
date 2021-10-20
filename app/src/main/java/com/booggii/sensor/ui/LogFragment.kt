package com.booggii.sensor.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.booggii.sensor.databinding.FragmentLogBinding
import com.booggii.sensor.model.LogData

class LogFragment : Fragment() {
    private var _binding: FragmentLogBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: LogData
    private lateinit var logView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        val root = binding.root
        logView = binding.log
        viewModel = ViewModelProvider(this).get(LogData::class.java)
        viewModel.logStream.observe(viewLifecycleOwner, { message ->
            logView.append(message + '\n')
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}// Required empty public constructor