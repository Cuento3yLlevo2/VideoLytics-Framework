package com.mahi.videolyticsframework

interface AnalyticsDataListener {
    fun onAnalyticsDataChanged(dataType: Int)
    fun onVideoFinished()
}