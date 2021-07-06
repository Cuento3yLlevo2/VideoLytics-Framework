package com.mahi.videolyticsframework.collector

import android.R.attr.data
import android.database.Cursor
import android.media.MediaMetadata.METADATA_KEY_DURATION
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData


/**
 * Extends ExoPlayer's AnalyticsListener
 * Raw playback events from the player are reported to AnalyticsListener implementations.
 * We can easily override only the methods you are interested in
 * and then process them further to make them meaningful
 */
class VideoLyticsListener(private val playbackAnalytics: PlaybackEventCallback) : AnalyticsListener {

    private var firstLoadStarted = false
    private var firstFrameShowed = false
    private var isCurrentlyPlaying = false

    override fun onLoadStarted(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
        if (!firstLoadStarted){
            if (eventTime.currentPlaybackPositionMs <= 0) {
                Log.d("VideoLyticsLogs", "******** Video starts to load(first play only) *********")
                playbackAnalytics.onVideoStartsLoading(loadEventInfo.uri, eventTime.realtimeMs)

                // First Play even wont be listen
                isCurrentlyPlaying = true
            } else {
                Log.d("VideoLyticsLogs", "******** Video starts to load after stopped at position ${eventTime.eventPlaybackPositionMs} milliseconds *********")
            }
            firstLoadStarted = true
        }
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        super.onRenderedFirstFrame(eventTime, output, renderTimeMs)

        if (!firstFrameShowed){
            if (eventTime.currentPlaybackPositionMs >= 0){
                Log.d("VideoLyticsLogs", "******** Video shows up its first frame ********* ${eventTime.eventPlaybackPositionMs}")
                playbackAnalytics.onVideoRenderedFirstFrame(eventTime.eventPlaybackPositionMs, eventTime.realtimeMs)
            }
            firstFrameShowed = true
        }
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        super.onIsPlayingChanged(eventTime, isPlaying)
        if (isPlaying) {
            if (!isCurrentlyPlaying){
                // Playback is playing
                Log.d("VideoLyticsLogs", "******** Playback Resumed *********")

                playbackAnalytics.onPlaybackResumed(eventTime.eventPlaybackPositionMs, eventTime.realtimeMs)

                isCurrentlyPlaying = true
            }
        } else {
            // Playback is paused
            Log.d("VideoLyticsLogs", "***** Playback Paused **********")
            playbackAnalytics.onPlaybackPaused(eventTime.eventPlaybackPositionMs, eventTime.realtimeMs)
            isCurrentlyPlaying = false
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(eventTime, oldPosition, newPosition, reason)
        when (reason) {
            SimpleExoPlayer.DISCONTINUITY_REASON_AUTO_TRANSITION -> Log.d("VideoLyticsLogs", "Video stutters due to period transition")
            SimpleExoPlayer.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> Log.d("VideoLyticsLogs", "Video stutters due to seek adjustment")
            SimpleExoPlayer.DISCONTINUITY_REASON_INTERNAL -> Log.d("VideoLyticsLogs", "Video stutters due to an internal problem")
            SimpleExoPlayer.DISCONTINUITY_REASON_SEEK -> Log.d("VideoLyticsLogs", "Video stutters due to a seek or User skips a portion of the video")
        }
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        super.onPlaybackStateChanged(eventTime, state)
        if (state == Player.STATE_ENDED){
            Log.d("VideoLyticsLogs", "***************** The player has finished playing the video ********************")
            playbackAnalytics.onVideoFinished(eventTime.eventPlaybackPositionMs, eventTime.realtimeMs)
        }
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        super.onPlayerReleased(eventTime)
        // Playback is paused
        Log.d("VideoLyticsLogs", "***** Playback Stopped due to PlayerReleased **********")

        playbackAnalytics.onPlaybackStopped(eventTime.eventPlaybackPositionMs, eventTime.realtimeMs)
    }


}