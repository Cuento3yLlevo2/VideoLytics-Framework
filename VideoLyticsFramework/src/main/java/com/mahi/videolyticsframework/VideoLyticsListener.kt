package com.mahi.videolyticsframework

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
class VideoLyticsListener(val playbackAnalytics: PlaybackEventCallback) : AnalyticsListener {

    private var firstLoadStarted = false
    private var firstFrameShowed = false
    private var isCurrentlyPlaying = false
    private var plabackskipped = false


    override fun onLoadStarted(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
        if (!firstLoadStarted){
            if (eventTime.currentPlaybackPositionMs <= 0) {
                Log.d("VideoLyticsLogs", "******** Video starts to load(first play only) *********")
                loadEventInfo.uri
                eventTime.realtimeMs
                playbackAnalytics.onVideoStartsLoading(loadEventInfo.uri, eventTime.realtimeMs)
                isCurrentlyPlaying = true
            } else {
                Log.d("VideoLyticsLogs", "******** Video starts to load after stopped at position ${eventTime.currentPlaybackPositionMs} milliseconds *********")
            }
            firstLoadStarted = true
        }
    }

    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        super.onRenderedFirstFrame(eventTime, output, renderTimeMs)

        if (!firstFrameShowed){
            if (eventTime.currentPlaybackPositionMs >= 0){
                Log.d("VideoLyticsLogs", "******** Video shows up its first frame ********* ${eventTime.currentPlaybackPositionMs}")
                playbackAnalytics.onVideoRenderedFirstFrame(eventTime.realtimeMs)
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

                playbackAnalytics.onPlaybackResumed(eventTime.currentPlaybackPositionMs, eventTime.realtimeMs)

                isCurrentlyPlaying = true
            }
        } else {
            if (!plabackskipped){
                isCurrentlyPlaying = false
                // Playback is paused
                Log.d("VideoLyticsLogs", "***** Playback Paused **********")

                playbackAnalytics.onPlaybackPaused(eventTime.currentPlaybackPositionMs, eventTime.realtimeMs)

            } else {
                plabackskipped = false
            }
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
            SimpleExoPlayer.DISCONTINUITY_REASON_SEEK -> {
                Log.d("VideoLyticsLogs", "Video stutters due to a seek or User skips a portion of the video")
                plabackskipped = true
            }
        }
    }

    override fun onPlaybackStateChanged(eventTime: EventTime, state: Int) {
        super.onPlaybackStateChanged(eventTime, state)
        if (state == Player.STATE_ENDED){
            Log.d("VideoLyticsLogs", "***************** The player has finished playing the video ********************")
        }
    }

    override fun onPlayerReleased(eventTime: EventTime) {
        super.onPlayerReleased(eventTime)
        // Playback is paused
        Log.d("VideoLyticsLogs", "***** Playback Stopped due to PlayerReleased **********")

        playbackAnalytics.onPlaybackStopped(eventTime.currentPlaybackPositionMs, eventTime.realtimeMs)
    }


}