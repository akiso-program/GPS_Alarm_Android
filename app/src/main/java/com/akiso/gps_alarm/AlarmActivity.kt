package com.akiso.gps_alarm

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.net.Uri
import android.os.*
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.akiso.gps_alarm.databinding.ActivityAlarmBinding


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private val myModel : MyViewModel by viewModels()
    private lateinit var alarmData: AlarmData
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibratorManager: Vibrator

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mediaPlayer = MediaPlayer()

        vibratorManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.stopButton.setOnClickListener { onStopClicked() }
        start()
    }

    private fun start(){
        if(isHeadsetConnected()) {
            val alertUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            with(mediaPlayer) {
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(applicationContext, alertUri)
                prepare()
                start()
            }
        }

        val vibrationEffect = VibrationEffect
            .createWaveform(
                longArrayOf(300, 300),
                intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE),
                -1)
        vibratorManager.vibrate(vibrationEffect)

    }

    private fun stop(){
        mediaPlayer.stop()
        vibratorManager.cancel()
    }

    private fun close(){
        // end this activity
        finishAndRemoveTask()
    }

    private fun onStopClicked(){
        stop()
        close()
    }

    private fun isHeadsetConnected(): Boolean {
        val audioManager:AudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val devices: Array<AudioDeviceInfo> = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        for (i in devices.indices) {
            val device = devices[i]
            val type = device.type
            if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                || type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                || type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                || type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}