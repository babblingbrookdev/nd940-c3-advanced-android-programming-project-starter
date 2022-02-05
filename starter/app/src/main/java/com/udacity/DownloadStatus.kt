package com.udacity

sealed class DownloadStatus {
    object Success : DownloadStatus()
    object Failure : DownloadStatus()
}