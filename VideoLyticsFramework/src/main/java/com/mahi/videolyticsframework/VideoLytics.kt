package com.mahi.videolyticsframework

class VideoLytics(var analyticsDataListener : AnalyticsDataListener?) {
    private var playbackAnalyticsCollector : PlaybackAnalyticsCollector

    var analyticsData : AnalyticsData = AnalyticsData(analyticsDataListener)

    init {
        playbackAnalyticsCollector = PlaybackAnalyticsCollector(analyticsData)
    }

    fun initCollector(): PlaybackEventCallback {
        return playbackAnalyticsCollector
    }
}
