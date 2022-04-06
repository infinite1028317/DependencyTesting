package com.probe.sdk.otherutils

import android.content.Context
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.probe.sdk.sdk.ProbeSdk


object ProbeInterface {

    private var eventDataBusInterface: EventDataBusInterface = ProbeSdk()

    fun registerForMitigationSession(ueid: String?, appContext: Context, enable_logs: Boolean) {

        eventDataBusInterface.deviceAndUserDataBus(ueid, appContext, enable_logs)
    }

    fun initSdk(mPlayer: SimpleExoPlayer?, videoAssetDetails: String) {
        eventDataBusInterface.videoAssetDetailsDataBus(mPlayer, videoAssetDetails)
    }

    fun sendEvent(eventName: String, eventDetailsIfAny: String) {
        eventDataBusInterface.eventReportingDataBus(eventName, eventDetailsIfAny)
    }


    fun configBufferingProp(builder: DefaultLoadControl.Builder) {
        eventDataBusInterface.bufferingConfigDataBus(builder)
    }

    fun configEstDownloadRate(): DefaultTrackSelector.Parameters? {
        return eventDataBusInterface.bitrateConfigDataBus()
    }


}