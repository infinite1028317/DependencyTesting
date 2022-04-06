package com.probe.sdk.models

import com.google.gson.JsonObject
import org.json.JSONObject

/**
 * Created by Robi Kumar Tomar  on 05/01/2022.
 */

internal class ProbeModel {

    var version: String? = null
    var diffTime: Long? = null
    var sdkVersion: String? = null
    var player: String? = null
    var playerApp: String? = null
    var cdn: String? = null
    var ip: String? = null
    var provider: String? = null
    var ueid: String? = null
    var udid: String? = null
    var platform: String? = null
    var deviceType: String? = null
    var manufacturer: String? = null
    var model: String? = null
    var networkType: String? = null
    var sessionId: String? = null
    var sbl: Long? = null
    var rbl: Long? = null
    var timestamp: Long? = null
    var playbackPosInSec: Long? = null
    var videoId: String? = null
    var assetDuration: Long? = null
    var aCodec: String? = null
    var vCodec: String? = null
    var bitrate: Long? = null
    var resolution: String? = null
    var throughput: Int? = null
    var frameRate: Int? = null
    var frameLoss: Int? = null
    var has: String? = null
    var drm: String? = null
    var live: String? = null
    var durationOfPlayback: Int = 0
    var totalDurationOfPlayback: Int = 0
    var totalStallDuration: Long? = null
    var stall: JsonObject? = null
    var switch: JsonObject? = null
    var totalSwitchesUp: Int? = null
    var totalSwitchesDown: Int? = null
    var from: String? = null
    var to: String? = null
    var stallDuration: String? = null
    var mitigationID: String? = null
    var location: String? = null
    var ua: String? = null


    /*   var streamingType: String? = null
       var isLive: String? = null
       var event: String? = null
       var eventData: String? = null
       var frameRate: String? = null
       var deviceID: String? = null
       var deviceModel: String? = null
       var deviceBrandName: String? = null
       var androidVersion: String? = null
       var userAgent: String? = null
       var userNetWorkType: String? = null
       var initialBitrateSelected: String? = null
       var durationSinceLastReport: String? = null
       var playTime: String? = null*/

}