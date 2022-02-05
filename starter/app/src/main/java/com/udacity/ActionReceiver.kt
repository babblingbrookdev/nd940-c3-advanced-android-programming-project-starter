package com.udacity

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            ContextCompat.getSystemService(context, NotificationManager::class.java)
        notificationManager?.cancelAll()
        val detailIntent = Intent(context, DetailActivity::class.java)
        detailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        detailIntent.putExtras(Bundle(intent.extras))
        context.startActivity(detailIntent)
    }
}