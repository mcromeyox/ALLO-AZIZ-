package com.example.data.system

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * HapticFeedbackHelper
 *
 * Provides physical haptic feedback (vibrations) on critical user interactions
 * like placing an order, accepting delivery task, or status changes.
 */
object HapticFeedbackHelper {

    fun vibrateSuccess(context: Context) {
        vibrate(context, longArrayOf(0, 80, 60, 100), intArrayOf(0, 200, 0, 255))
    }

    fun vibrateAlert(context: Context) {
        vibrate(context, longArrayOf(0, 120, 80, 120), intArrayOf(0, 255, 0, 255))
    }

    fun vibrateClick(context: Context) {
        vibrate(context, longArrayOf(0, 40), intArrayOf(0, 150))
    }

    private fun vibrate(context: Context, timings: LongArray, amplitudes: IntArray) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (!v.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    v.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(timings[1])
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration fails or lacks permission
        }
    }
}
