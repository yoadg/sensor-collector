package com.booggii.sensor.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.booggii.sensor.databinding.FragmentSettingsBinding
import com.booggii.sensor.devices.DeviceManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        with (binding) {
            val userId = userId
            userId.setText(DeviceManager.settings.userId)
            userId.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    DeviceManager.settings.userId = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                }


            })
            val deviceId = deviceId
            deviceId.setText(DeviceManager.settings.deviceId)
            deviceId.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    DeviceManager.settings.deviceId = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int,
                                               count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence, start: Int,
                                           before: Int, count: Int) {
                }


            })
            ecg.isEnabled = DeviceManager.settings.ecg
            DeviceManager.ecgRates.forEach {
                ecgRate.addView(createRadioButton(it))
            }
            ecgRate.check(DeviceManager.settings.ecgSampleRate)
            ecgRate.setOnCheckedChangeListener { _, checkedId ->
                DeviceManager.settings.ecgSampleRate = checkedId
            }

            DeviceManager.ecgRes.forEach {
                ecgRes.addView(createRadioButton(it))
            }
            ecgRes.check(DeviceManager.settings.ecgResolution)
            ecgRes.setOnCheckedChangeListener { _, checkedId ->
                DeviceManager.settings.ecgResolution = checkedId
            }

            acc.isEnabled = DeviceManager.settings.acc
            DeviceManager.accRates.forEach {
                accRate.addView(createRadioButton(it))
            }
            accRate.check(DeviceManager.settings.accSampleRate)
            accRate.setOnCheckedChangeListener { _, checkedId ->
                DeviceManager.settings.accSampleRate = checkedId
            }

            DeviceManager.accRes.forEach {
                accRes.addView(createRadioButton(it))
            }
            accRes.check(DeviceManager.settings.accResolution)
            accRes.setOnCheckedChangeListener { _, checkedId ->
                DeviceManager.settings.accResolution = checkedId
            }
            DeviceManager.accRanges.forEach {
                accRange.addView(createRadioButton(it))
            }
            accRange.check(DeviceManager.settings.accRange)
            accRange.setOnCheckedChangeListener { _, checkedId ->
                DeviceManager.settings.accRange = checkedId
            }

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createRadioButton(value: Int): RadioButton {
        val rButton = RadioButton(context)
        rButton.id = value
        rButton.text = value.toString()
        rButton.textSize = 20F
        //rButton.isChecked = (value == checkedValue)
        return rButton
    }

}// Required empty public constructor