package com.mahi.videolyticsframework.collector

import android.net.Uri

/**
 * Interface that listen for VideoLyticsListener Events.
 *
 * - onVideoStartsLoading
 * - onVideoRenderedFirstFrame
 * - onPlaybackResumed
 * - onPlaybackPaused
 * - onPlaybackStopped
 * - onVideoFinished
 *
 */
interface PlaybackEventCallback {
    fun onVideoStartsLoading(uri: Uri, eventTimeMs: Long)
    fun onVideoRenderedFirstFrame(position: Long, eventTimeMs: Long)
    fun onPlaybackResumed(position: Long, eventTimeMs: Long)
    fun onPlaybackPaused(position: Long, eventTimeMs: Long)
    fun onPlaybackStopped(position: Long, eventTimeMs: Long)
    fun onVideoFinished(position: Long, eventTimeMs: Long)
}