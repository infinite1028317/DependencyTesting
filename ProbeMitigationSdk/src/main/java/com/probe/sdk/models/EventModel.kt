package com.probe.sdk.models

import com.google.gson.JsonObject

internal class EventModel {
    var version: String? = null
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
    var timestamp: Long? = null
    var playbackPosInSec: Long? = null
    var videoId: String? = null
    var assetDuration: Long? = null
    var frameRate: Int? = null
    var aCodec: String? = null
    var vCodec: String? = null
    var bitrate: Long? = null
    var resolution: String? = null
    var throughput: Int? = null
    var has: String? = null
    var drm: String? = null
    var live: String? = null
    var event: String? = null
    var eventPrev: String? = null
    var eventData: JsonObject? = null
    var mitigationID: String? = null
    var location: String? = null
    var ua: String? = null

}