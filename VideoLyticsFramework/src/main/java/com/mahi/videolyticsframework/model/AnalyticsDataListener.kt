package com.mahi.videolyticsframework.model

/**
 * Interface that listens for:
 *
 * - Variable value changes on a AnalyticsData Object
 * - Video playback finished
 */
interface AnalyticsDataListener {
    fun onAnalyticsDataChanged(dataType: Int)
    fun onVideoFinished()
}