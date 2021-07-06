package com.mahi.videolyticsframework.model

interface AnalyticsDataListener {
    fun onAnalyticsDataChanged(dataType: Int)
    fun onVideoFinished()
}