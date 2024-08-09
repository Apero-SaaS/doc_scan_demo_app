package com.apero.app.poc_ml_docscan.home

import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _sensorRotationDegrees = MutableStateFlow(0)
    val sensorRotationDegrees = _sensorRotationDegrees.asStateFlow()
    fun updateSensorRotation(cameraController: LifecycleCameraController) {
        viewModelScope.launch {
            while (isActive) {
                _sensorRotationDegrees.value =
                    cameraController.cameraInfo?.sensorRotationDegrees ?: 0
                delay(1000L)
            }
        }
    }
}