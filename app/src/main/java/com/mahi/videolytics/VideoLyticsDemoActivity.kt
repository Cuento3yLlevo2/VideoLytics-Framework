package com.mahi.videolytics

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource.HttpDataSourceException
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.mahi.videolyticsframework.PlaybackAnalytics
import com.mahi.videolyticsframework.VideoLyticsListener
import java.io.IOException


class VideoLyticsDemoActivity : AppCompatActivity() {

    // Variable Represents ExoPlayer
    private var exoplayer: SimpleExoPlayer? = null

    private lateinit var playerView: StyledPlayerView
    private var playWhenReady = true
    private var startWindow = 0
    private var startPosition: Long = 0
    private lateinit var playbackAnalytics : PlaybackAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_lytics_demo)
        playerView = findViewById(R.id.spvStyledPlayerView)
        playbackAnalytics = PlaybackAnalytics()
        initExoplayer()
    }

    private fun initExoplayer() {
        // create an ExoPlayer instance
        exoplayer = SimpleExoPlayer.Builder(this).build()
        // Here we bind the player to the view.
        exoplayer.let { playerView.player = it }

        // I'm using the following video resource as suggested
        // http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this)
        val videoUri : Uri = Uri.parse("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8")
        val hlsMediaSource: HlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUri))

        // Implementing Framework VideoLyticsListener
        exoplayer?.let{ it.addAnalyticsListener(VideoLyticsListener(playbackAnalytics))}

        // Set the MediaSource to be played.
        exoplayer?.setMediaSource(hlsMediaSource, true)
        // Prepare the player.
        exoplayer?.prepare()

        // When a failure occurs, this method will be called
        exoplayer?.playerError?.let { onPlayerError(it) }
    }

    override fun onStart() {
        super.onStart()
        // Start the playback.
        exoplayer?.playWhenReady = playWhenReady
    }

    override fun onResume() {
        super.onResume()
        if(Util.SDK_INT < 24 || exoplayer == null){
            initExoplayer()
            // Seeks to the previous position
            exoplayer?.seekTo(startWindow, startPosition)
            // Start the playback.
            exoplayer?.playWhenReady = playWhenReady
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById(R.id.layoutRoot)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // With API 24 multi window was introduced to Android.
    // This means that two apps can be visible at the same time.
    // This has changed things and that's why the behaviour is different for API < 24.
    // After 24 you want to release the player in onStop,
    // because onPause may be called when your app is still visible in split screen.
    override fun onPause() {
        super.onPause()
        if(Util.SDK_INT < 24){
            if (exoplayer != null) {
                pausePlayer()
            }
            releasePlayer()
        } else {
            pausePlayer()
        }
    }

    private fun pausePlayer() {
        playWhenReady = exoplayer!!.playWhenReady
        startPosition = exoplayer!!.currentPosition
        startWindow = exoplayer!!.currentWindowIndex
        exoplayer?.pause()
    }

    private fun releasePlayer() {
        if(exoplayer != null){
            playWhenReady = exoplayer!!.playWhenReady
            startPosition = exoplayer!!.currentPosition
            startWindow = exoplayer!!.currentWindowIndex
            exoplayer!!.release()
            exoplayer = null
        }
    }

    private fun onPlayerError(error: ExoPlaybackException) {
        if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            val cause: IOException = error.sourceException
            if (cause is HttpDataSourceException) {
                // An HTTP error occurred.
                // querying the cause.
                if (cause is InvalidResponseCodeException) {
                    // Cast to InvalidResponseCodeException and retrieve the response code,
                    // message and headers.
                    cause.message?.let { Log.d("onPlayerError", it) }
                } else {
                    // Try calling httpError.getCause() to retrieve the underlying cause,
                    // although note that it may be null.
                    cause.message?.let { Log.d("onPlayerError", it) }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            if (exoplayer != null) {
                playerView.onPause()
            }
            releasePlayer()
        }
    }

}