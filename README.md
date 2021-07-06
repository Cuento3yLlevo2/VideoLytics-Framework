# VideoLytics

VideoLytics is a custom Analytics Collector for [ExoPlayer][] events during Video Playback.  

![VideoLytics demo gif](https://media1.giphy.com/media/KGzkoSaEzILTx51X2G/giphy.gif?cid=790b7611c878772d04c73330c84b2b2a8c7e70599b1ddc87&rid=giphy.gif&ct=g)

* Counts how many times a video is paused during playback
* Counts how many times a video is resumed/restarted. 
* Tracks the time elapsed between every pause and resume event.
* Provides data collected modified  in real time to display on Activity.
* Makes HTTP POST requests to https://jsonplaceholder.typicode.com/ with JSON Object  with data collected. 


[ExoPlayer]:https://github.com/google/ExoPlayer

## VideoLytics Repository ##

This repository contains a test application with one activity using ExoPlayer for reproducing video and the VideoLytics Framework module as a dependency library.  

#### 1. Installing The VideoLytics Framework ####

To install the VideoLytics Framework, simply download the zip file (using the Clone or download option on GitHub) and import only the VideoLyticsFramework module into your existing Android Studio project.

* Choose **File > New > Import Module**.
* Select the ```VideoLyticsFramework``` folder.
* Add the following line to your app-level ```build.gradle``` file:

```gradle
implementation project(':VideoLyticsFramework')
```

If not implemented already, you also need to [Add ExoPlayer module dependencies][] and use ExoPlayer to reproduce the video content instead of Android’s MediaPlayer.

VideoLytics Framework does not support Android’s MediaPlayer yet.

[Add ExoPlayer module dependencies]:https://github.com/google/ExoPlayer#1-add-exoplayer-module-dependencies

#### 2. Using the VideoLytics Framework ####

Add an ``AnalyticsDataListener`` Interface to the activity or fragment that contains the [ExoPlayer Instance][]

```kotlin
class VideoLyticsDemoActivity : AppCompatActivity(), AnalyticsDataListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {...}
    
    // implement its members
    override fun onAnalyticsDataChanged(dataType: Int) {
        ...
    }

    override fun onVideoFinished() {
        ...
    }

}
```
Create a ``VideoLytics`` class instance which will pass as parameters the current context and the ``AnalyticsDataListener`` Interface.
```kotlin
    private lateinit var videoLytics : VideoLytics
    private var exoplayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_lytics_demo)
        videoLytics = VideoLytics(
         this /* context */, 
         this /* AnalyticsDataListener */)
        
        // Initialize Exoplayer after that 
        initExoplayer()
    }
```
The VideoLytics Framework collects data using the ``VideoLyticsListener`` class that extends from ExoPlayer's [AnalyticsListener Interface][]. This interface listens for the Raw playback events from a given player.

After creating the ExoPlayer instance you should add ``VideoLyticsListener`` class as an AnalyticsListener with the method ``videoLytics.initCollector()`` that returns a ``VideoLyticsListener`` instance.  
```kotlin
  private fun initExoplayer() {
        // create an ExoPlayer instance
        exoplayer = SimpleExoPlayer.Builder(this).build()
        
        // Adding VideoLyticsListener as AnalyticsListener
        exoplayer?.addAnalyticsListener(
            VideoLyticsListener(
                videoLytics.initCollector()
            )
        )
    }
```
All data collected is available from your application's Activity or Fragment by retrieving data from the ``videoLytics.analyticsData`` class. 

Use the ``AnalyticsDataListener interface`` implemented methods to listen for updates on ``videoLytics.analyticsData`` class to retrieve changes in real time.
```Kotlin
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
```
``onAnalyticsDataChanged``: listens for: 
* Number of times Paused changes ``VideoLytics.TIMES_PAUSED_CHANGED``. 
* Number of times Resumed changes ``VideoLytics.TIMES_RESUMED_CHANGED``. 

``onVideoFinished()``: listens when the video playback finished. 

[AnalyticsListener Interface]:https://exoplayer.dev/analytics.html#event-collection-with-analyticslistener
[ExoPlayer Instance]:https://exoplayer.dev/hello-world.html#creating-the-player

#### 3. About HTTP POST requests fake API for testing ####

The VideoLytics Framework uses [Volley HTTP library][] to make a fake HTTP POST request to https://jsonplaceholder.typicode.com/ for testing purposes every time that:

* Video starts to load(first play only) --> (Json Request Body Example)
```Json
{"dataLastUpdateTimeMs":1059924668,"dataLastUpdateType":"FIRST_LOAD","firstLoadTimeMs":1059924668,"totalTimesPaused":0,"totalTimesResumed":0,"videoPosition":0,"videoUriPath":"http://yourvideosrc"}
```
* Video shows up its first frame --> (Json Request Body Example) 
```Json
{"dataLastUpdateTimeMs":1059926784,"dataLastUpdateType":"RENDERED_FRAME","firstLoadTimeMs":1059924668,"renderedFrameTimeMs":1059926784,"totalTimesPaused":0,"totalTimesResumed":0,"videoPosition":0,"videoUriPath":"yourvideosrc"}
```
* Video has been stopped. --> (Json Request Body Example)
```Json
{"dataLastUpdateTimeMs":1060023857,"dataLastUpdateType":"VIDEO_STOPPED","firstLoadTimeMs":1059924668,"renderedFrameTimeMs":1059926784,"timeElapsedUntilResumedAgain":870,"totalTimesPaused":4,"totalTimesResumed":3,"videoPosition":91099,"videoUriPath":"yourvideosrc8"}
```
* Video is finished. --> (Json Request Body Example)
```Json
{"dataLastUpdateTimeMs":1060110600,"dataLastUpdateType":"VIDEO_FINISHED","firstLoadTimeMs":1059924668,"renderedFrameTimeMs":1060033828,"timeElapsedUntilResumedAgain":214,"totalTimesPaused":9,"totalTimesResumed":9,"videoFinishedTimeMs":1060110600,"videoPosition":5365002,"videoUriPath":"yourvideosrc"}
```

[Volley HTTP library]:https://developer.android.com/training/volley

#### 4. External Dependencies ####

The VideoLytics Framework uses in its module the following external dependencies:
```gradle
    // An extensible media player for Android (Only Core Module)
    implementation 'com.google.android.exoplayer:exoplayer-core:2.14.1'
    // Volley HTTP library
    implementation 'com.android.volley:volley:1.2.0'
    // Library to convert Java Objects into JSON and back
    implementation 'com.google.code.gson:gson:2.8.7'
```
