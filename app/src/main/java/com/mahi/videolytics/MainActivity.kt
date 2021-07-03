package com.mahi.videolytics

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity(), View.OnClickListener, StyledPlayerControlView.VisibilityListener {

    // Variable Represents ExoPlayer
    private var exoplayer: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var playbackPosition : Long = 0
    private var currentWindow = 0
    private lateinit var playerView: StyledPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.spvStyledPlayerView)

        initExoplayer()

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this)

        // I'm using the following video resource as suggested
        // http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8
        val videoUri : Uri = Uri.parse("http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8")

        val hlsMediaSource: HlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUri))

        /*
        val field : Field = R.raw::class.java.getField("catlicking")
        val uri = Uri.parse(
            "android.resource://" + packageName + "/" + resources.getIdentifier(
                field.name,
                "raw",
                packageName
            )
        )
        val finalUri = Uri.parse("https://i.imgur.com/7bMqysJ.mp4")

        // Build the media item needed to populate Exoplayer View.
        val mediaItem: MediaItem = MediaItem.fromUri(uri)
        */


        // Set the MediaSource to be played.
        exoplayer?.setMediaSource(hlsMediaSource)
        // Prepare the player.
        exoplayer?.prepare()
        // Start the playback.
        exoplayer?.playWhenReady

        // exoplayer?.seekTo(currentWindow, playbackPosition)

    }

    private fun initExoplayer() {
        // create an ExoPlayer instance
        exoplayer = SimpleExoPlayer.Builder(this).build()
        // Here we bind the player to the view.
        exoplayer.let { playerView.player = it }
    }

    override fun onStart() {
        super.onStart()
        /*
        if(Util.SDK_INT >= 24){
            initExoplayer()
        }*/
    }

    override fun onResume() {
        super.onResume()
        if(Util.SDK_INT < 24 || exoplayer == null){
            initExoplayer()
            hideSystemUI()
        }
    }


    private fun hideSystemUI() {
        playerView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }

    override fun onPause() {
        if(Util.SDK_INT < 24){
            releasePlayer()
        }
        super.onPause()

    }

    private fun releasePlayer() {
        if(exoplayer != null){
            playWhenReady = exoplayer!!.playWhenReady
            playbackPosition = exoplayer!!.currentPosition
            currentWindow = exoplayer!!.currentWindowIndex
            exoplayer!!.release()
            exoplayer = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoplayer?.release()
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }

    override fun onVisibilityChange(visibility: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        // Saved instance state keys.
        const private val KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters"
        const private val KEY_WINDOW = "window"
        const private val KEY_POSITION = "position"
        const private val KEY_AUTO_PLAY = "auto_play"
    }

}