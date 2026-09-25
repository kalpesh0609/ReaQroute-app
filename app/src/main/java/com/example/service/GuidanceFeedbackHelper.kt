/**
 * ResQRoute - Disaster Evacuation & Dynamic Routing Platform
 * File: /service/GuidanceFeedbackHelper.kt
 *
 * PURPOSE & AIM:
 * Hardware and sensory orchestration service for live disaster evacuation.
 * Provides accessible hands-free Text-to-Speech (TTS) voice announcements,
 * precision haptic vibration patterns for turn cues and danger zones,
 * and compass bearing orientation tracking.
 *
 * LINKINGS & CONNECTIONS:
 * - Class: [GuidanceFeedbackHelper].
 * - Consumed By: [LiveGuidanceScreen], [ResQRouteViewModel].
 * - Platform Services: [TextToSpeech], [Vibrator], [SensorManager].
 */

package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Handles sensory feedback (voice guidance, haptic pulses, and compass rotation)
 * during emergency evacuation navigation.
 */
class GuidanceFeedbackHelper(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "GuidanceFeedback"
    }

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    var onHeadingChanged: ((Float) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                Log.w(TAG, "TTS Initialization failed with status: $status")
            }
        }
    }

    /**
     * Speaks an evacuation navigation prompt out loud if TTS is available.
     *
     * @param text Instruction to announce (e.g. "In 200 meters, turn left onto Ridge Crest Way").
     */
    fun speak(text: String) {
        if (!isTtsReady || text.isBlank()) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ResQGuidancePrompt")
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking TTS: ${e.message}")
        }
    }

    /**
     * Triggers a subtle tactile pulse alerting the citizen that a turn is approaching in 100 meters.
     */
    fun vibrateTurnUpcoming() {
        vibrate(longArrayOf(0, 150, 100, 150), intArrayOf(0, 180, 0, 180))
    }

    /**
     * Triggers an urgent high-intensity vibration pattern signaling immediate turn execution.
     */
    fun vibrateImmediateManeuver() {
        vibrate(longArrayOf(0, 250), intArrayOf(0, 255))
    }

    /**
     * Triggers a rapid double-pulse alert when the citizen approaches a detected flood node or detour.
     */
    fun vibrateHazardWarning() {
        vibrate(longArrayOf(0, 100, 80, 100, 80, 200), intArrayOf(0, 255, 0, 255, 0, 255))
    }

    /**
     * Triggers a sustained celebratory confirmation pulse upon reaching the safe haven shelter gate.
     */
    fun vibrateArrivalCelebration() {
        vibrate(longArrayOf(0, 300, 120, 450), intArrayOf(0, 200, 0, 255))
    }

    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        if (vibrator?.hasVibrator() != true) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibrator exception: ${e.message}")
        }
    }

    /**
     * Starts monitoring device compass orientation.
     */
    fun startCompass() {
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Stops monitoring compass orientation to conserve battery during disaster.
     */
    fun stopCompass() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthDegrees < 0) azimuthDegrees += 360f
            onHeadingChanged?.invoke(azimuthDegrees)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Releases system TTS and sensor resources.
     */
    fun destroy() {
        stopCompass()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "TTS shutdown error: ${e.message}")
        }
    }
}
