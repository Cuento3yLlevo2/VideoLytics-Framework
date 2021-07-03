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
import java.io.IOException


class VideoLyticsDemoActivity : AppCompatActivity() {

    // Variable Represents ExoPlayer
    private var exoplayer: SimpleExoPlayer? = null

    private lateinit var playerView: StyledPlayerView
    private var playWhenReady = true
    private var startWindow = 0
    private var startPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_lytics_demo)
        playerView = findViewById(R.id.spvStyledPlayerView)
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

        // Set the MediaSource to be played.
        exoplayer?.setMediaSource(hlsMediaSource, true)
        // Prepare the player.
        exoplayer?.prepare()
        // Start the playback.
        exoplayer?.playWhenReady = playWhenReady

        exoplayer?.seekTo(startWindow, startPosition)

        // When a failure occurs, this method will be called
        exoplayer?.playerError?.let { onPlayerError(it) }
    }

    override fun onStart() {
        super.onStart()
        initExoplayer()
    }

    override fun onResume() {
        super.onResume()
        if(Util.SDK_INT < 24 || exoplayer == null){
            initExoplayer()
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

    override fun onPause() {
        releasePlayer()
        super.onPause()
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
        releasePlayer()
        super.onStop()
    }

}