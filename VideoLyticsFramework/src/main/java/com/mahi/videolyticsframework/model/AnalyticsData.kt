package com.mahi.videolyticsframework.model

import android.net.Uri
import com.google.gson.annotations.Expose

class AnalyticsData(var analyticsDataListener: AnalyticsDataListener?) {
    var dataCreatedTimeMs: Long? = null
    @Expose
    var dataLastUpdateTimeMs: Long? = null
    @Expose
    var dataLastUpdateType: String? = null
    @Expose
    var videoUriPath: String? = null
    @Expose
    var videoPosition: Long? = null
    @Expose
    var firstLoadTimeMs: Long? = null
    @Expose
    var renderedFrameTimeMs: Long? = null
    @Expose
    var videoFinishedTimeMs: Long? = null
    @Expose
    var totalTimesPaused: Int = 0
    @Expose
    var totalTimesResumed: Int = 0
    @Expose
    var timeElapsedUntilResumedAgain : Long? = null
    // Not exposed to JSON
    var resumedTimeElapsedList = ArrayList<Long>()
}
