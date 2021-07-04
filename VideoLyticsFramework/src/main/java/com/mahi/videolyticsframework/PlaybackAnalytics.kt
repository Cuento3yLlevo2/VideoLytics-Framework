package com.mahi.videolyticsframework

import android.net.Uri
import android.util.Log

class PlaybackAnalytics : PlaybackEventCallback {

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
        resumedPressedCounter++
        resumedPressedEventTimeMs = eventTimeMs
        val resumedTimeElapsed = resumedPressedEventTimeMs - pausedPressedEventTimeMs
        resumedTimeElapsedList.add(resumedTimeElapsed)
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackResumed at position $position")
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackResumed --> $pausedPressedCounter times (TimeElapsed -> ${resumedTimeElapsedList.last().div(1000)} seconds)")
    }

    override fun onPlaybackPaused(position: Long, eventTimeMs: Long) {
        pausedPressedCounter++
        pausedPressedEventTimeMs = eventTimeMs
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused at position $position")
        Log.d("VideoLyticsLogs", "PlaybackEvent: PlaybackPaused --> $pausedPressedCounter times")
    }

    override fun onPlaybackStopped(position: Long, eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onPlaybackStopped at position $position")
    }

    override fun onVideoFinished(eventTimeMs: Long) {
        Log.d("VideoLyticsLogs", "PlaybackEvent: onVideoFinished --> PlaybackResumed --> $resumedPressedCounter times, PlaybackPaused --> $pausedPressedCounter times")
    }

    override fun observeData() {
        // do nothing
    }
}