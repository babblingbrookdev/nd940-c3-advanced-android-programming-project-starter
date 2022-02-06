package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.udacity.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var url = ""

    private lateinit var binding: ActivityMainBinding

    private lateinit var notificationManager: NotificationManager

    private var itemSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        notificationManager = getSystemService(NotificationManager::class.java)

        binding.customButton.setOnClickListener {
            if (itemSelected) {
                download(url)
            } else {
                showSnackbar(getString(R.string.no_selection_message))
            }
        }

        binding.selectionRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.glide_radio_selection -> url = GLIDE_URL
                R.id.loadapp_radio_selection -> url = LOADAPP_URL
                R.id.retrofit_radio_selection -> url = RETROFIT_URL
            }
            itemSelected = true
        }

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                description = getString(R.string.notification_description)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val id = it.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (it.action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query()
                    query.setFilterById(id)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            sendNotification(getDownloadStatus(cursor))
                        }
                    }
                    showSnackbar(getString(R.string.download_completed_message))
                    binding.customButton.onDownloadCompleted()
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun sendNotification(downloadStatus: DownloadStatus) {
        val filenameExtra = getFilenameText()
        val statusExtra = getStatusText(downloadStatus)
        val messageBody = getNotifMessageBody(downloadStatus)

        notificationManager.cancelNotifications()
        notificationManager.sendNotification(
            messageBody = messageBody,
            filenameExtra = filenameExtra,
            statusExtra = statusExtra,
            applicationContext = applicationContext
        )
    }

    private fun download(url: String) {
        binding.customButton.onStartDownload()

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun getDownloadStatus(cursor: Cursor): DownloadStatus {
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        return when (cursor.getInt(columnIndex)) {
            DownloadManager.STATUS_SUCCESSFUL ->
                DownloadStatus.Success
            else -> {
                // there are other status types as well as reasons for failure
                // here we are waiting for completed intent and just see if we are successful
                // from https://developer.android.com/reference/android/app/DownloadManager
                DownloadStatus.Failure
            }
        }
    }

    private fun getFilenameText(): String {
        val selectedId = binding.selectionRadioGroup.checkedRadioButtonId
        val radioSelected: RadioButton = findViewById(selectedId)
        return radioSelected.text.toString()
    }

    private fun getStatusText(downloadStatus: DownloadStatus): String {
        return when (downloadStatus) {
            DownloadStatus.Success -> getString(R.string.download_success)
            DownloadStatus.Failure -> getString(R.string.download_failed)
        }
    }

    private fun getNotifMessageBody(downloadStatus: DownloadStatus): String {
        return when (downloadStatus) {
            DownloadStatus.Success -> getString(R.string.download_completed_message)
            DownloadStatus.Failure -> getString(R.string.download_failed_message)
        }
    }

    companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val LOADAPP_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
    }

}
