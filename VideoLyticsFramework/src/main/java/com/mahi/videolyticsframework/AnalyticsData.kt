package com.mahi.videolyticsframework

class AnalyticsData(var analyticsDataListener: AnalyticsDataListener?) {
    // variables
    var totalTimesPaused: Int = 0
    var totalTimesResumed: Int = 0
    var timeElapsedUntilResumedAgain : Long? = null
    var resumedTimeElapsedList = ArrayList<Long>()

    companion object {
        const val TIMES_PAUSED_CHANGED = 0
        const val TIMES_RESUMED_CHANGED = 1
    }
}
