package com.mahi.videolytics

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
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
import com.mahi.videolyticsframework.VideoLytics
import com.mahi.videolyticsframework.collector.VideoLyticsListener
import com.mahi.videolyticsframework.model.AnalyticsDataListener
import java.io.IOException

// Add an AnalyticsDataListener Interface to the activity or fragment that contains the ExoPlayer Instance
class VideoLyticsDemoActivity : AppCompatActivity(), AnalyticsDataListener {

    // Variable Represents ExoPlayer
    private var exoplayer: SimpleExoPlayer? = null

    private lateinit var playerView: StyledPlayerView
    private lateinit var tvTotalTimesPaused: TextView
    private lateinit var tvTotalTimesResumed: TextView
    private lateinit var tvTimeElapsed: TextView
    private lateinit var videoFinishedLayoutRoot : NestedScrollView
    private lateinit var btnVideoFinishedHideBtn : Button
    private lateinit var tvVideoFinishedTotalTimesPaused : TextView
    private lateinit var tvVideoFinishedTotalTimesResumed : TextView
    private lateinit var tvVideoFinishedTimeElapsedList : TextView
    private var playWhenReady = true
    private var startWindow = 0
    private var startPosition: Long = 0
    private lateinit var videoLytics : VideoLytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_lytics_demo)
        playerView = findViewById(R.id.spvStyledPlayerView)
        tvTotalTimesPaused = findViewById(R.id.tvTotalTimesPaused)
        tvTotalTimesResumed = findViewById(R.id.tvTotalTimesResumed)
        tvTimeElapsed = findViewById(R.id.tvTimeElapsed)
        tvTotalTimesPaused.text = getString(R.string.pausedTimesNullText)
        tvTotalTimesResumed.text = getString(R.string.resumedTimesNullText)
        tvTimeElapsed.text = getString(R.string.timeElapsedNullText)
        videoFinishedLayoutRoot = findViewById(R.id.videoFinishedLayoutRoot)
        btnVideoFinishedHideBtn = findViewById(R.id.btnVideoFinishedHideBtn)
        tvVideoFinishedTotalTimesPaused = findViewById(R.id.tvVideoFinishedTotalTimesPaused)
        tvVideoFinishedTotalTimesResumed = findViewById(R.id.tvVideoFinishedTotalTimesResumed)
        tvVideoFinishedTimeElapsedList = findViewById(R.id.tvVideoFinishedTimeElapsedList)

        btnVideoFinishedHideBtn.setOnClickListener {
            videoFinishedLayoutRoot.visibility = View.GONE
            tvVideoFinishedTimeElapsedList.text = ""
        }

        // Create a VideoLytics class instance which will pass
        // as parameters the current context and the AnalyticsDataListener Interface.
        videoLytics = VideoLytics(this,this)

        // Initialize Exoplayer after that
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

        /*
            The VideoLytics Framework collects data using the VideoLyticsListener class
            that extends from ExoPlayer's AnalyticsListener Interface.

            This interface listens for the Raw playback events from a given player.
            After creating the ExoPlayer instance you should add VideoLyticsListener class
            as an AnalyticsListener with the method videoLytics.initCollector() that returns
            a VideoLyticsListener instance.
         */
        exoplayer?.addAnalyticsListener(VideoLyticsListener(videoLytics.initCollector()))

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

    /*
     * All data collected is available from your application's
     * Activity or Fragment by retrieving data from the videoLytics.analyticsData class.
     * Use the AnalyticsDataListener interface implemented methods
     * to listen for updates on videoLytics.analyticsData class to retrieve changes in real time.
     */

    /**
     * onAnalyticsDataChanged: listens for:
     * - Number of times Paused changes VideoLytics.TIMES_PAUSED_CHANGED.
     * - Number of times Resumed changes VideoLytics.TIMES_RESUMED_CHANGED.
     */
    override fun onAnalyticsDataChanged(dataType: Int) {
        when(dataType){
            VideoLytics.TIMES_PAUSED_CHANGED -> {
                val totalTimesPausedString = "Paused:    ${videoLytics.analyticsData.totalTimesPaused} times"
                tvTotalTimesPaused.text = totalTimesPausedString
            }
            VideoLytics.TIMES_RESUMED_CHANGED -> {
                val totalTimesResumedString = "Resumed: ${videoLytics.analyticsData.totalTimesResumed} times"
                val timeElapsedString = "Time Elapsed: ${videoLytics.analyticsData.timeElapsedUntilResumedAgain?.div(1000.0)} secs"
                tvTotalTimesResumed.text = totalTimesResumedString
                tvTimeElapsed.text = timeElapsedString
            }
        }
    }

    /**
     * onVideoFinished(): listens when the video playback finished.
     */
    override fun onVideoFinished() {
        videoFinishedLayoutRoot.visibility = View.VISIBLE
        val totalTimesPausedString = "Number of times Paused: ${videoLytics.analyticsData.totalTimesPaused} times"
        val totalTimesResumedString = "Number of times Resumed: ${videoLytics.analyticsData.totalTimesResumed} times"
        tvVideoFinishedTotalTimesPaused.text = totalTimesPausedString
        tvVideoFinishedTotalTimesResumed.text = totalTimesResumedString
        videoLytics.analyticsData.resumedTimeElapsedList.forEachIndexed { index, timeElapsedMilisecs ->
            var content = ""
            content += "${timeElapsedMilisecs.div(1000.0)} secs Elapsed Until Resumed Nº ${index+1}\n"
            tvVideoFinishedTimeElapsedList.append(content)
        }
    }

}