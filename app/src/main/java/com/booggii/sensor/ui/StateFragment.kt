package com.booggii.sensor.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.booggii.sensor.databinding.FragmentStateBinding
import com.booggii.sensor.devices.DeviceManager
import com.booggii.sensor.devices.Status

class StateFragment : Fragment() {
    private var _binding: FragmentStateBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStateBinding.inflate(inflater, container, false)
        val root = binding.root
        val deviceId = binding.deviceId
        val deviceName = binding.deviceName
        val batteryLevel = binding.batteryLevel
        val deviceStatus = binding.deviceStatus
        val deviceFeatures = binding.deviceFeatures
        deviceId.setText(DeviceManager.settings.deviceId)
        DeviceManager.deviceState.batteryLevel.observe(viewLifecycleOwner, { level ->
            batteryLevel.text = level.toString()
        })
        DeviceManager.deviceState.status.observe(viewLifecycleOwner, {status ->
            if (status == Status.DISCONNECTED) {
                deviceName.text = null
                batteryLevel.text = null
                deviceFeatures.text = null
            } else {
                deviceName.text = DeviceManager.getDeviceName()
            }
            deviceStatus.text = status.name
        })
        DeviceManager.deviceState.streams.observe(viewLifecycleOwner, {streams ->
            deviceFeatures.text = streams.toString()
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}// Required empty public constructor