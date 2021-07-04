package com.mahi.videolyticsframework

import android.net.Uri
import android.util.Log

class PlaybackAnalyticsCollector(var analyticsData: AnalyticsData) : PlaybackEventCallback {

    private var pausedPressedCounter = 0
    private var pausedPressedEventTimeMs : Long = 0
    private var resumedPressedCounter = 0
    private var resumedPressedEventTimeMs : Long = 0
    private var resumedTimeElapsedList = ArrayList<Long>()


    override fun onVideoStartsLoading(uri: Uri, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: VideoStartsLoading --> Uri -> $uri")
    }

    override fun onVideoRenderedFirstFrame(eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: VideoRenderedFirstFrame")
    }

    override fun onPlaybackResumed(position: Long, eventTimeMs: Long) {
        // Processing data
        resumedPressedCounter++
        resumedPressedEventTimeMs = eventTimeMs
        val resumedTimeElapsed = resumedPressedEventTimeMs - pausedPressedEventTimeMs
        resumedTimeElapsedList.add(resumedTimeElapsed)
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackResumed at position $position")
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackResumed --> $pausedPressedCounter times (TimeElapsed -> ${resumedTimeElapsedList.last().div(1000)} seconds)")

        // Populating AnalyticsData Class
        analyticsData.totalTimesResumed = pausedPressedCounter
        analyticsData.timeElapsedUntilResumedAgain = resumedTimeElapsed
        analyticsData.resumedTimeElapsedList = resumedTimeElapsedList

        // Calling AnalyticsDataListener's onAnalyticsDataChanged method
        analyticsData.analyticsDataListener?.onAnalyticsDataChanged(AnalyticsData.TIMES_RESUMED_CHANGED)
    }

    override fun onPlaybackPaused(position: Long, eventTimeMs: Long) {
        // Processing data
        pausedPressedCounter++
        pausedPressedEventTimeMs = eventTimeMs
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused at position $position")
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused --> $pausedPressedCounter times")

        // Populating AnalyticsData Class
        analyticsData.totalTimesPaused = pausedPressedCounter

        // Calling AnalyticsDataListener's onAnalyticsDataChanged method
        analyticsData.analyticsDataListener?.onAnalyticsDataChanged(AnalyticsData.TIMES_PAUSED_CHANGED)
    }

    override fun onPlaybackStopped(position: Long, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onPlaybackStopped at position $position")
    }

    override fun onVideoFinished(eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onVideoFinished --> PlaybackResumed --> $resumedPressedCounter times, PlaybackPaused --> $pausedPressedCounter times")
        // Calling AnalyticsDataListener's onVideoFinished() method
        analyticsData.analyticsDataListener?.onVideoFinished()
    }
}