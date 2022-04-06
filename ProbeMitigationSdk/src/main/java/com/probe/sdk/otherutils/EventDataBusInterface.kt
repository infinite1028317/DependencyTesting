package com.probe.sdk.otherutils

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

internal interface EventDataBusInterface {

    fun deviceAndUserDataBus(ueid: String?, appContext: Context, enable_logs: Boolean)

    fun videoAssetDetailsDataBus(mPlayer: SimpleExoPlayer?, videoAssetDetails: String)

    fun eventReportingDataBus(eventName: String, eventDetailsIfAny: String)

    fun bufferingConfigDataBus(builder: DefaultLoadControl.Builder)
    fun bitrateConfigDataBus(): DefaultTrackSelector.Parameters?


}