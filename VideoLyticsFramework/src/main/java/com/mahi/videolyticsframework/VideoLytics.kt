package com.mahi.videolyticsframework

import android.content.Context
import com.mahi.videolyticsframework.collector.PlaybackAnalyticsCollector
import com.mahi.videolyticsframework.collector.PlaybackEventCallback
import com.mahi.videolyticsframework.model.AnalyticsData
import com.mahi.videolyticsframework.model.AnalyticsDataListener

/**
 * VideoLytics Framework point of access for Android App.
 *
 * this class needs to be implemented on the Android App Activity or Fragment in charge on reproducing
 * video with an ExoPlayer media player.
 *
 * @param context the current context from Activity.
 * @param analyticsDataListener is an AnalyticsDataListener that should extends.
 * @constructor Creates a PlaybackAnalyticsCollector that will take care of data collection.
 */
class VideoLytics(var context: Context, analyticsDataListener : AnalyticsDataListener?) {

    private var playbackAnalyticsCollector : PlaybackAnalyticsCollector

    var analyticsData : AnalyticsData = AnalyticsData(analyticsDataListener)

    init {
        playbackAnalyticsCollector = PlaybackAnalyticsCollector(context, analyticsData)
    }

    fun initCollector(): PlaybackEventCallback {
        return playbackAnalyticsCollector
    }

    companion object {
        // Events for AnalyticsDataListener
        const val TIMES_PAUSED_CHANGED = 0
        const val TIMES_RESUMED_CHANGED = 1
        // dataLastUpdate Types
        const val FIRST_LOAD = "FIRST_LOAD"
        const val RENDERED_FRAME = "RENDERED_FRAME"
        const val VIDEO_FINISHED = "VIDEO_FINISHED"
        const val VIDEO_STOPPED = "VIDEO_STOPPED"
        const val TIMES_PAUSED = "TIMES_PAUSED"
        const val TIMES_RESUMED = "TIMES_RESUMED"
        // fake API for testing and prototyping.
        const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }
}
