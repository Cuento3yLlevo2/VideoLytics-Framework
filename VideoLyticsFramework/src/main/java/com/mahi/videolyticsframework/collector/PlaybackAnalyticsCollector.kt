package com.mahi.videolyticsframework.collector

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mahi.videolyticsframework.VideoLytics
import com.mahi.videolyticsframework.api.VolleyService
import com.mahi.videolyticsframework.model.AnalyticsData

/**
 * Collects and transforms all data from VideoLyticsListener.
 *
 * extends PlaybackEventCallback Interface to get callbacks from needed events.
 *
 * @param context current Android app context.
 * @param analyticsData Object that storage collected data.
 *
 */
class PlaybackAnalyticsCollector(var context: Context, var analyticsData: AnalyticsData) : PlaybackEventCallback {

    private var pausedPressedCounter = 0
    private var pausedPressedEventTimeMs : Long = 0
    private var resumedPressedCounter = 0
    private var resumedPressedEventTimeMs : Long = 0
    private var resumedTimeElapsedList = ArrayList<Long>()
    var volleyService: VolleyService = VolleyService()


    override fun onVideoStartsLoading(uri: Uri, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: VideoStartsLoading --> Uri -> $uri")

        // Populating AnalyticsData Class when VideoStartsLoading and clearing previous data
        // this Even only happens once at the start of any video playback.
        analyticsData.videoUriPath = uri.toString()
        analyticsData.firstLoadTimeMs = eventTimeMs
        analyticsData.dataCreatedTimeMs = eventTimeMs
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.FIRST_LOAD
        analyticsData.videoPosition = 0
        analyticsData.renderedFrameTimeMs = null
        analyticsData.videoFinishedTimeMs = null
        analyticsData.totalTimesPaused = 0
        analyticsData.totalTimesResumed = 0
        analyticsData.timeElapsedUntilResumedAgain = null
        analyticsData.resumedTimeElapsedList.clear()

        // Post http request
        volleyService.pushAnalyticsData(context, analyticsData)

    }

    override fun onVideoRenderedFirstFrame(position: Long, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: VideoRenderedFirstFrame")
        analyticsData.renderedFrameTimeMs = eventTimeMs
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.RENDERED_FRAME
        analyticsData.videoPosition = position

        // Post http request
        volleyService.pushAnalyticsData(context, analyticsData)
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
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.TIMES_RESUMED
        analyticsData.videoPosition = position
        // Calling AnalyticsDataListener's onAnalyticsDataChanged method
        analyticsData.analyticsDataListener?.onAnalyticsDataChanged(VideoLytics.TIMES_RESUMED_CHANGED)
    }

    override fun onPlaybackPaused(position: Long, eventTimeMs: Long) {
        // Processing data
        pausedPressedCounter++
        pausedPressedEventTimeMs = eventTimeMs
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused at position $position")
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused --> $pausedPressedCounter times")

        // Populating AnalyticsData Class
        analyticsData.totalTimesPaused = pausedPressedCounter
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.TIMES_PAUSED
        analyticsData.videoPosition = position
        // Calling AnalyticsDataListener's onAnalyticsDataChanged method
        analyticsData.analyticsDataListener?.onAnalyticsDataChanged(VideoLytics.TIMES_PAUSED_CHANGED)
    }

    override fun onPlaybackStopped(position: Long, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onPlaybackStopped at position $position")
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.VIDEO_STOPPED
        analyticsData.videoPosition = position

        // Post http request
        volleyService.pushAnalyticsData(context, analyticsData)


    }

    override fun onVideoFinished(position: Long, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onVideoFinished --> PlaybackResumed --> $resumedPressedCounter times, PlaybackPaused --> $pausedPressedCounter times")
        // Populating AnalyticsData Class
        analyticsData.videoFinishedTimeMs = eventTimeMs
        analyticsData.dataLastUpdateTimeMs = eventTimeMs
        analyticsData.dataLastUpdateType = VideoLytics.VIDEO_FINISHED
        analyticsData.videoPosition = position

        // Calling AnalyticsDataListener's onVideoFinished() method
        analyticsData.analyticsDataListener?.onVideoFinished()

        // Post http request
        volleyService.pushAnalyticsData(context, analyticsData)
    }

}