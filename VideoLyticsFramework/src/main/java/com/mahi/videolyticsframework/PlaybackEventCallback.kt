package com.mahi.videolyticsframework

import android.net.Uri

interface PlaybackEventCallback {
    fun onVideoStartsLoading(uri: Uri, eventTimeMs: Long)
    fun onVideoRenderedFirstFrame(eventTimeMs: Long)
    fun onPlaybackResumed(position: Long, eventTimeMs: Long)
    fun onPlaybackPaused(position: Long, eventTimeMs: Long)
    fun onPlaybackStopped(position: Long, eventTimeMs: Long)
    fun onVideoFinished(eventTimeMs: Long)
    fun observeData()
}