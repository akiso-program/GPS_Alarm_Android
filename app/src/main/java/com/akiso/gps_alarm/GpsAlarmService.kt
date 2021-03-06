package com.akiso.gps_alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GpsAlarmService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "通知のタイトル的情報を設定"
        val id = "casareal_foreground"
        val notifyDescription = "この通知の詳細情報を設定します"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            mChannel.apply {
                description = notifyDescription
            }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            setContentTitle("通知のタイトル")
            setContentText("通知の内容")
            setSmallIcon(R.drawable.ic_launcher_background)
        }.build()

        Thread(
            Runnable {
                (0..5).map {
                    Thread.sleep(1000)
                }

                stopForeground(STOP_FOREGROUND_REMOVE)

            }).start()

        startForeground(1, notification)


        return START_REDELIVER_INTENT
    }
}