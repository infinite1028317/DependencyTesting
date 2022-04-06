package com.probe.sdk.sdk


import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log.d
import android.view.Surface
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.probe.sdk.models.EventModel
import com.probe.sdk.models.ProbeModel
import com.probe.sdk.networkspace.AppRepository
import com.probe.sdk.otherutils.EventDataBusInterface
import com.probe.sdk.otherutils.MyUtils
import com.probe.sdk.otherutils.ProbeInterface
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


internal class ProbeSdk : AppRepository.InterfaceUpdateAfterJobFinish, EventDataBusInterface {

    private var logsKey = "ProbeSdkLogs"
    private var pingTimerHandler = Handler(Looper.getMainLooper())
    private lateinit var pingRunnable: Runnable
    private var isPingTimerRunning = false

    private var mitigationTimerHandler = Handler(Looper.getMainLooper())
    private lateinit var mitigationRunnable: Runnable
    private var isMitigationTimerRunning = false


    private var myPlayer: SimpleExoPlayer? = null

    private var playerState: String = "Idle"
    private var lastStateOfPlayer: String = "Idle"
    private var isInitialLatency: Boolean = false
    private var bufferStartTime = 0L
    private var pingIntervalTime = 60000L
    private var mitigationIntervalTime = 120000L
    private var dateAndTimeWhenBufferStarted = ""
    private var errorDetails = ""
    private var isVideoDurationFetched: Boolean = false


    private var totalBufferDurSinceLastPing =
        0L    // This is the total buffer duration since last ping, make sure to reset it in each ping & on each new session
    private var totalBufferDurInThisPlaySession =
        0L   // This is the total buffer duration in single complete session, make sure to reset it in each new session start


    private var appSharedPreferences: SharedPreferences? = null
    private var appEditor: SharedPreferences.Editor? = null

    private lateinit var appContext: Context
    private var bufferCount = 0
    private var totalBitrateSwitchUpCount = 0
    private var totalBitrateSwitchDownCount = 0
    private var jsonObjectUpSwitch = JsonObject()
    private var jsonObjectDownSwitch = JsonObject()

    private var ipRequestTryCount = 0

    private var totalPlayBackDurSinceLastPing =
        0L

    private var totalPlayBackDurForThisSession =
        0L


    private var playBackStartTime = 0L

    private var videoRestartTime =
        0L   // Video restart time is the number of seconds after user-initiated seeking until video begins playing.
    private var seekStartTime = 0L


    private fun getPlayBackPosition(): Long {
        return try {
            ((myPlayer!!.currentPosition).div(1000))
        } catch (e: Exception) {
            0
        }
    }

    private fun getPlayDurationSincePing(): Long { // This is to calculate totalDurationOfPlayBack since last ping
        return try {
            if (totalPlayBackDurSinceLastPing.toInt() != 0) {
                totalPlayBackDurSinceLastPing / 1000
            } else {

                0
            }
        } catch (e: Exception) {
            0
        }

    }

    private fun getTotalDurationOfPlayBackInSession(): Long {  // This is to calculate "totalDurationOfPlayback" in this complete session
        return try {
            if (totalPlayBackDurForThisSession.toInt() != 0) {
                totalPlayBackDurForThisSession / 1000
            } else {

                0
            }
        } catch (e: Exception) {
            getPlayBackPosition()
        }


    }

    private fun sendPeriodicPayload(
        myContext: Context
    ): String {  // This function get call for generating the periodic payload.(Payload sending at particular given interval)

        calculateTotalPlayBack()  // here we need to update the playback duration because may be there is no pause,buffering etc since playback started, So how we will get the updated playback.....Here this line will calculate the most updated playback values before sending the ping


        val arrayList = ArrayList<ProbeModel>()

        val tm = ProbeModel()

        tm.version = MyUtils.version
        tm.diffTime = getDiffTime()
        tm.sdkVersion = MyUtils.sdkVersion
        tm.player = MyUtils.player_name
        tm.playerApp = MyUtils.getPlayerAppName()
        if (!TextUtils.isEmpty(getConfigurationFromLocal("cdn"))) {
            tm.cdn = MyUtils.firstLetterCapitalize(getConfigurationFromLocal("cdn"))
        } else {
            tm.cdn = ""
        }
        tm.ip = MyUtils.encryptInMd5(getConfigurationFromLocal("get_ip_address"))
        tm.provider = getConfigurationFromLocal("provider")
        tm.ueid = MyUtils.encryptInMd5(getConfigurationFromLocal("ueid"))
        tm.udid = MyUtils.getDeviceID(myContext)
        tm.platform = MyUtils.platform
        tm.deviceType = MyUtils.deviceType
        tm.manufacturer = MyUtils.getDeviceManufacturer()
        tm.model = MyUtils.getDeviceModel()
        tm.networkType = MyUtils.getInternetConnectionType(myContext)
        tm.sessionId = getConfigurationFromLocal("sessionId")

        try {
            tm.sbl = getConfigurationFromLocal("startupBuffDuration").toLong()
            tm.rbl = getConfigurationFromLocal("rebufferingDuration").toLong()
        } catch (e: Exception) {
        }

        try {
            tm.timestamp = ((System.currentTimeMillis()) / 1000)
        } catch (e: Exception) {
            tm.timestamp = 0
        }
        tm.playbackPosInSec = getPlayBackPosition()
        tm.videoId = getConfigurationFromLocal("videoId")
        try {
            tm.assetDuration = getConfigurationFromLocal("video_duration").toLong()
        } catch (e: Exception) {
            tm.assetDuration = 0
        }
        try {
            tm.frameRate = MyUtils.getStandardisedFrameRate(getConfigurationFromLocal("frame_rate"))
        } catch (e: Exception) {
            tm.frameRate = 0
        }

        try {
            tm.frameLoss = MyUtils.getFrameLoss(
                getConfigurationFromLocal("frame_rate"),
                getConfigurationFromLocal("last_frame_rate")
            )
        } catch (e: Exception) {
            tm.frameLoss = 0
        }


        tm.aCodec = getConfigurationFromLocal("audio_codec")
        tm.vCodec = getConfigurationFromLocal("video_codec")
        try {
            tm.bitrate = getConfigurationFromLocal("initial_bitrate_selected").toLong()
        } catch (e: Exception) {
            tm.bitrate = 0
        }
        tm.resolution = getConfigurationFromLocal("resolution")

        try {
            tm.throughput = getConfigurationFromLocal("throughput").toInt()
        } catch (e: Exception) {
            tm.throughput = 0
        }
        if (!TextUtils.isEmpty(getConfigurationFromLocal("has"))) {
            tm.has = (getConfigurationFromLocal("has")).uppercase()
        } else {
            tm.has = ""
        }

        if (!TextUtils.isEmpty(getConfigurationFromLocal("drm"))) {
            tm.drm = MyUtils.firstLetterCapitalize(getConfigurationFromLocal("drm"))
        } else {
            tm.drm = ""
        }

        if (!TextUtils.isEmpty(getConfigurationFromLocal("is_live_content"))) {
            tm.live = getConfigurationFromLocal("is_live_content")
        } else {
            tm.live = "false"
        }


        tm.durationOfPlayback = getPlayDurationSincePing().toInt()
        tm.totalDurationOfPlayback = getTotalDurationOfPlayBackInSession().toInt()
        tm.totalStallDuration = totalBufferDurInThisPlaySession
        tm.stall = createStallsPayload()
        tm.switch = createBitRateSwitchPayload()
        tm.totalSwitchesUp = totalBitrateSwitchUpCount
        tm.totalSwitchesDown = totalBitrateSwitchDownCount
        tm.mitigationID = MyUtils.mitigationID
        tm.location = getConfigurationFromLocal("location")
        tm.ua = MyUtils.getUserAgent()

        arrayList.add(tm)

        var strBurnSessionActivity = ""
        try {
            if (arrayList.size != 0 && arrayList.size > 0) {
                val gson = GsonBuilder().create()
                strBurnSessionActivity = (gson.toJsonTree(arrayList).asJsonArray).toString()

                val getJsonObject = JSONArray(strBurnSessionActivity).getJSONObject(0)

                strBurnSessionActivity = JSONObject().put("ping", getJsonObject).toString()
            }
        } catch (e: Exception) {
        }

        saveConfigurationInLocal(
            "last_frame_rate",
            getConfigurationFromLocal("frame_rate")
        )  // here save frame_rate as a last_frame_rate for next computation of frame_loss

        clearDataAfterPingSend()

        return strBurnSessionActivity
    }

    private fun clearDataAfterPingSend() {
        // Below creating jsonObject again, so that previous data can be deleted
        jsonObjectUpSwitch = JsonObject()
        jsonObjectDownSwitch = JsonObject()
        bufferCount =
            0  // make it zero, because for this ping we have already send the buffer count
        totalBufferDurSinceLastPing =
            0  // make it zero, because for this ping we have already utilize this param, for next subsequent pings its value is being updating from other place
        totalPlayBackDurSinceLastPing = 0

        if (!TextUtils.isEmpty(playerState) && playerState.equals("STATE_PLAYING", true)) {
            playBackStartTime =
                System.currentTimeMillis()  // We need to reset this value here, only in that case if playback is continue playing & we just send the ping, so to calculate current delta we need to assume playback start time is equal to this ping time(Because playback is continuously playing)
        }


    }


    private fun sendEventPayload(myContext: Context, eventName: String): String {
        val arrayList = ArrayList<EventModel>()
        var updatedEventName = eventName

        /* check here if network is connected or not, If it is not connected then send event as Error with details*/

        if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(myContext)) && (MyUtils.getInternetConnectionType(
                myContext
            )).equals("no_network", true)
        ) {
            updatedEventName = "Error"
            errorDetails = MyUtils.noInternetDetail
        }


        val tm = EventModel()

        tm.version = MyUtils.version
        tm.sdkVersion = MyUtils.sdkVersion
        tm.player = MyUtils.player_name
        tm.playerApp = MyUtils.getPlayerAppName()
        if (!TextUtils.isEmpty(getConfigurationFromLocal("cdn"))) {
            tm.cdn = MyUtils.firstLetterCapitalize(getConfigurationFromLocal("cdn"))
        } else {
            tm.cdn = ""
        }
        tm.ip = MyUtils.encryptInMd5(getConfigurationFromLocal("get_ip_address"))
        tm.provider = getConfigurationFromLocal("provider")
        tm.ueid = MyUtils.encryptInMd5(getConfigurationFromLocal("ueid"))
        tm.udid = MyUtils.getDeviceID(myContext)
        tm.platform = MyUtils.platform
        tm.deviceType = MyUtils.deviceType
        tm.manufacturer = MyUtils.getDeviceManufacturer()
        tm.model = MyUtils.getDeviceModel()
        tm.networkType = MyUtils.getInternetConnectionType(myContext)
        tm.sessionId = getConfigurationFromLocal("sessionId")
        try {
            tm.timestamp = ((System.currentTimeMillis()) / 1000)
        } catch (e: Exception) {
            tm.timestamp = 0
        }
        tm.playbackPosInSec = getPlayBackPosition()
        tm.videoId = getConfigurationFromLocal("videoId")
        try {
            tm.assetDuration = getConfigurationFromLocal("video_duration").toLong()
        } catch (e: Exception) {
            tm.assetDuration = 0
        }
        try {
            tm.frameRate = MyUtils.getStandardisedFrameRate(getConfigurationFromLocal("frame_rate"))
        } catch (e: Exception) {
            tm.frameRate = 0
        }
        tm.aCodec = getConfigurationFromLocal("audio_codec")
        tm.vCodec = getConfigurationFromLocal("video_codec")
        try {
            tm.bitrate = getConfigurationFromLocal("initial_bitrate_selected").toLong()
        } catch (e: Exception) {
            tm.bitrate = 0
        }
        tm.resolution = getConfigurationFromLocal("resolution")


        try {
            tm.throughput = getConfigurationFromLocal("throughput").toInt()
        } catch (e: Exception) {
            tm.throughput = 0
        }

        if (!TextUtils.isEmpty(getConfigurationFromLocal("has"))) {
            tm.has = (getConfigurationFromLocal("has")).uppercase()
        } else {
            tm.has = ""
        }

        if (!TextUtils.isEmpty(getConfigurationFromLocal("drm"))) {
            tm.drm = MyUtils.firstLetterCapitalize(getConfigurationFromLocal("drm"))
        } else {
            tm.drm = ""
        }

        if (!TextUtils.isEmpty(getConfigurationFromLocal("is_live_content"))) {
            tm.live = getConfigurationFromLocal("is_live_content")
        } else {
            tm.live = "false"
        }
        tm.event = MyUtils.getStandardisedEventName(updatedEventName)
        tm.eventPrev =
            MyUtils.getStandardisedEventName(getConfigurationFromLocal("previous_event_name"))

        val myJsonObject = JsonObject()
        try {


            if (videoRestartTime != 0L) {  // As per the document, only one value will be send to cloud from vrt & latency. Both value cant be present in eventData in a single time.
                myJsonObject.addProperty("vrt", videoRestartTime)
                videoRestartTime = 0L
            } else {
                val getLatency = getConfigurationFromLocal("latency")
                if (!TextUtils.isEmpty(getLatency)) {
                    myJsonObject.addProperty(
                        "latency",
                        getConfigurationFromLocal("latency").toLong()
                    )
                } else {
                    myJsonObject.addProperty("latency", 0)
                }
            }




            tm.eventData = myJsonObject

            if (!TextUtils.isEmpty(MyUtils.getStandardisedEventName(updatedEventName)) && MyUtils.getStandardisedEventName(
                    updatedEventName
                ).equals("error", true)
            ) {
                myJsonObject.addProperty("desc", errorDetails)
            }

        } catch (e: Exception) {
            tm.eventData = myJsonObject
            e.printStackTrace()
        }

        tm.mitigationID = MyUtils.mitigationID
        tm.location = getConfigurationFromLocal("location")
        tm.ua = MyUtils.getUserAgent()

        arrayList.add(tm)

        var strBurnSessionActivity = ""
        try {
            if (arrayList.size != 0 && arrayList.size > 0) {

                val gson = GsonBuilder().create()
                strBurnSessionActivity = (gson.toJsonTree(arrayList).asJsonArray).toString()

                val getJsonArray = JSONArray(strBurnSessionActivity).getJSONObject(0)

                strBurnSessionActivity = JSONObject().put("event", getJsonArray).toString()
            }
        } catch (e: Exception) {
            MyUtils.printMyLogs("Exception in sendEventPayload")
        }
        return strBurnSessionActivity
    }


    private fun saveConfigurationInLocal(config_key: String, config_value: String) {
        try {
            appEditor?.putString(config_key, config_value)?.apply()
        } catch (e: Exception) {
        }
    }

    private fun getConfigurationFromLocal(config_key: String): String {
        return try {
            appSharedPreferences?.getString(config_key, "").toString()
        } catch (e: Exception) {
            ""
        }
    }


    private fun syncSdkWithPlayer(player: SimpleExoPlayer?) {
        //For "dateAndTimeWhenBufferStarted"...... This we assuming, like this function will get triggered before on each video play, so it will help in calculating the initial buffer time

        isInitialLatency = true
        isVideoDurationFetched = false
        dateAndTimeWhenBufferStarted = MyUtils.getTodayDateTime()
        bufferStartTime = System.currentTimeMillis()
        totalBufferDurSinceLastPing = 0
        totalBufferDurInThisPlaySession = 0
        totalPlayBackDurForThisSession = 0
        totalPlayBackDurSinceLastPing = 0
        totalBitrateSwitchUpCount = 0
        totalBitrateSwitchDownCount = 0
        // Lets assume it as "STATE_BUFFERING", because we need initial buffering
        lastStateOfPlayer = "STATE_BUFFERING"
        /* try {
             myPlayer?.release()
         } catch (e: Exception) {
         }*/
        myPlayer = player

        initPlayerListener()

        /* MyUtils.conditionalLocalStorageDelete("clear_data_on_new_session", appEditor)*/

        /*saveConfigurationInLocal(
            "previous_event_name",
            ("STATE_CLICKED")
        )


         logMyEvents("STATE_CLICKED")*/

        MyUtils.printMyLogs("ProbeAppName : " + MyUtils.getPlayerAppName())

    }

    private fun initPlayerListener() {


        myPlayer?.addAnalyticsListener(object : AnalyticsListener {


            override fun onRenderedFirstFrame(
                eventTime: AnalyticsListener.EventTime,
                surface: Surface?
            ) {
                super.onRenderedFirstFrame(eventTime, surface)

                saveConfigurationInLocal(
                    "frame_rate",
                    (myPlayer?.videoFormat?.frameRate).toString()
                )

                /* saveConfigurationInLocal(
                     "frame_drop",
                     (myPlayer?.videoFormat?.).toString()
                 )*/
                saveConfigurationInLocal(
                    "resolution",
                    (myPlayer?.videoFormat?.width).toString() + "*" + (myPlayer?.videoFormat?.height).toString()
                )
                saveConfigurationInLocal(
                    "initial_bitrate_selected",
                    (myPlayer?.videoFormat?.bitrate!!).toString()
                )
                saveConfigurationInLocal(
                    "last_bitrate_selected",
                    (myPlayer?.videoFormat?.bitrate!!).toString()
                ) // Just to calculate bitrate delta, we adding one more param in local storage


                saveConfigurationInLocal(
                    "video_codec",
                    (myPlayer?.videoFormat?.codecs).toString()
                )
                saveConfigurationInLocal(
                    "audio_codec",
                    (myPlayer?.audioFormat?.codecs).toString()
                )
                saveConfigurationInLocal(
                    "is_live_content",
                    (myPlayer?.isCurrentWindowLive).toString()
                )

            }

            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long
            ) {
            }


            override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
                super.onSeekStarted(eventTime)

                seekStartTime = System.currentTimeMillis()

                playerState = "STATE_SEEK"

                calculateTotalPlayBack()

                logMyEvents(playerState)


            }


            override fun onDownstreamFormatChanged(
                eventTime: AnalyticsListener.EventTime,
                mediaLoadData: MediaSourceEventListener.MediaLoadData
            ) {
                super.onDownstreamFormatChanged(eventTime, mediaLoadData)



                saveConfigurationInLocal(
                    "bitrate_switch_to",
                    (mediaLoadData.trackFormat?.bitrate!!).toString()
                )
                saveConfigurationInLocal(
                    "frame_rate",
                    (myPlayer?.videoFormat?.frameRate).toString()
                )
                saveConfigurationInLocal(
                    "resolution",
                    (myPlayer?.videoFormat?.width).toString() + "*" + (myPlayer?.videoFormat?.height).toString()
                )


                calculateBitrateSwitch()

            }


            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long
            ) {
                super.onBandwidthEstimate(
                    eventTime,
                    totalLoadTimeMs,
                    totalBytesLoaded,
                    bitrateEstimate
                )

                saveConfigurationInLocal(
                    "throughput",
                    (bitrateEstimate).toString()
                )

            }


        })

        myPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                reportExoPlayerErrors(error)

            }

            override fun onPlayerStateChanged(
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                when (playbackState) {


                    Player.STATE_BUFFERING -> {
                        playerState = "STATE_BUFFERING"
                        lastStateOfPlayer = "STATE_BUFFERING"

                        calculateTotalPlayBack()

                        bufferStartTime = System.currentTimeMillis()
                        dateAndTimeWhenBufferStarted = MyUtils.getTodayDateTime()
                        bufferCount++

                    }
                    Player.STATE_ENDED -> {

                        playerState = "STATE_ENDED"
                        calculateTotalPlayBack()
                        stopPingAndMitigationJob()
                    }

                    Player.STATE_IDLE ->
                        playerState = "STATE_IDLE"
                    Player.STATE_READY -> {
                        playerState = "STATE_READY"

                        if (playWhenReady && playbackState == Player.STATE_READY) {
                            // media actually playing
                            playerState = "STATE_PLAYING"

                            playBackStartTime = System.currentTimeMillis()

                            MyUtils.printMyLogs("Media_Downloaded_In_This_Buffer_Duration : " + myPlayer!!.totalBufferedDuration)

                            if (isInitialLatency) {
                                isInitialLatency = false
                                saveConfigurationInLocal(
                                    "latency",
                                    (System.currentTimeMillis() - bufferStartTime).toString()
                                )
                                MyUtils.printMyLogs("Latency_Buffer_Duration : " + myPlayer!!.totalBufferedDuration)

                                totalBufferDurSinceLastPing =
                                    System.currentTimeMillis() - bufferStartTime // Here we calculate initial buffering

                                totalBufferDurInThisPlaySession =
                                    System.currentTimeMillis() - bufferStartTime // Here we calculate initial buffering


                                bufferCount++   // increase by one because here we including the initial buffering also

                            }

                            if (lastStateOfPlayer.equals(
                                    "STATE_BUFFERING",
                                    true
                                )
                            )// First here check, if last player state was buffering then only you need calculate the stall duration, because this block may be called in many scenarios like play>pause>Wait for 2 minutes> then play will not be considered as stall duration
                            {

                                lastStateOfPlayer = "STATE_PLAYING"
                                /* calculateStalls(
                                     System.currentTimeMillis() - bufferStartTime
                                 )*/

                                if (isVideoDurationFetched) { // this means buffering happening other than initial buffering, so calculate. Other than this, just leave it because initial buffering already calculated above
                                    totalBufferDurSinceLastPing += (System.currentTimeMillis() - bufferStartTime)  // here we calculating total buffer since last ping, which is :  sum of total buffering so far + last most buffering duration

                                    totalBufferDurInThisPlaySession += (System.currentTimeMillis() - bufferStartTime)  // here we calculating total buffer in a session, which is :  sum of total buffering so far + last most buffering


                                    if (seekStartTime != 0L) {
                                        videoRestartTime =
                                            System.currentTimeMillis() - seekStartTime
                                        seekStartTime = 0
                                        MyUtils.printMyLogs("VideoRestartTime : $videoRestartTime")
                                    }


                                }





                                MyUtils.printMyLogs("Buffer_Duration : " + (System.currentTimeMillis() - bufferStartTime))
                            }

                            // make sure playerState variable value get change after buffer calculation in this block


                            if (!isVideoDurationFetched) {
                                val realDurationMillis: Long =
                                    (myPlayer!!.duration) / 1000

                                isVideoDurationFetched = true
                                saveConfigurationInLocal(
                                    "video_duration",
                                    realDurationMillis.toString()
                                )

                            }


                        } else {
                            calculateTotalPlayBack()
                            playerState = "STATE_PAUSED"
                            lastStateOfPlayer = "STATE_PAUSED"


                        }

                    }


                }

                if (!TextUtils.isEmpty(getConfigurationFromLocal("previous_event_name")) && (getConfigurationFromLocal(
                        "previous_event_name"
                    )).equals(
                        "STATE_PAUSED",
                        true
                    ) && playerState.equals("STATE_PLAYING", true)
                ) {
                    logMyEvents("STATE_RESUMED")  // We don't get any resume from player, so here we calculating it on last state assumptions
                } else {
                    logMyEvents(playerState)
                }


            }

        })
    }


    private fun makeEventRequest(
        requestType: String,
        eventName: String,
    ) {

        /*  if (!TextUtils.isEmpty(eventName) && eventName.equals("deviceRegistration", true)) {
              initAll(myContext) // init all local variables & pass the context

          }*/


        if (!TextUtils.isEmpty(requestType) && requestType.equals(
                "post_play_events_request",
                true
            )
        ) {
            AppRepository.initDataSender(
                appContext,
                MyUtils.getBaseUrl(),
                "post_play_events_request",
                sendEventPayload(appContext, eventName), eventName,
                this
            )


        } else if (!TextUtils.isEmpty(requestType) && requestType.equals(
                "periodic_events_request",
                true
            )
        ) {


            AppRepository.initDataSender(
                appContext,
                MyUtils.getBaseUrl(),
                "periodic_events_request",
                sendPeriodicPayload(appContext), eventName,
                this
            )


        }


    }

    private fun saveDataFromApp(videoAssetDetails: String) {

        // This block will execute when we play any video then video related data we need to save in shared preference)
        MyUtils.printMyLogs("RequestType : Fetch Video Asset Details,  DataReceivedFromBingeApp : $videoAssetDetails")
        val myJsonObject: JSONObject?
        try {
            if (!TextUtils.isEmpty(videoAssetDetails) && MyUtils.isJsonValid(
                    videoAssetDetails
                )
            ) {

                myJsonObject = JSONObject(videoAssetDetails)


                if (myJsonObject.has("videoUrl")) {
                    saveConfigurationInLocal(
                        "has",
                        MyUtils.getHas(myJsonObject.get("videoUrl").toString())
                    )

                    // Whenever we getting asset details for each video session, then we need to find out the CDN from video url header

                    AppRepository.readHeaderByHittingVideoUrl(
                        appContext,
                        myJsonObject.get("videoUrl").toString(),
                        "get_cdn",
                        "get_cdn",
                        "get_cdn",
                        this
                    )

                    //   AppRepository.directNetWorkCall(myJsonObject.get("videoUrl").toString())


                }
                if (myJsonObject.has("videoId")) {
                    saveConfigurationInLocal(
                        "videoId",
                        myJsonObject.get("videoId").toString()
                    )
                }


                if (myJsonObject.has("drm")) {
                    saveConfigurationInLocal(
                        "drm",
                        myJsonObject.get("drm").toString()
                    )
                }

                if (myJsonObject.has("provider")) {
                    saveConfigurationInLocal(
                        "provider",
                        myJsonObject.get("provider").toString()
                    )
                }

                if (myJsonObject.has("sessionId") && !TextUtils.isEmpty(
                        myJsonObject.get("sessionId").toString()
                    )
                ) {
                    saveConfigurationInLocal(
                        "sessionId",
                        myJsonObject.get("sessionId").toString()
                    )
                } else {
                    saveConfigurationInLocal(
                        "sessionId",
                        MyUtils.generateSessionId()
                    )   // Generating session id, which will be last long with current playing video, once new video will start session id will be reset.

                }

            }


        } catch (e: Exception) {
            MyUtils.printMyLogs(e.printStackTrace().toString())
        }


        //  val myJsonObject = JsonArray().get(0).JSONObject(userAndAssetData)

    }


    /*   private fun releasePlayer() {
           try {
               myPlayer?.run {

                   release()
               }
           } catch (e: Exception) {
               e.printStackTrace()
           }

       }*/

    private fun logMyEvents(logEventName: String) {


        if (!TextUtils.isEmpty(logEventName) && logEventName.equals(
                "STATE_BUFFERING",
                true
            ) || logEventName.equals(
                "STATE_PLAYING",
                true
            ) || logEventName.equals(
                "STATE_PAUSED",
                true
            ) || logEventName.equals(
                "STATE_ENDED",
                true
            )

            || logEventName.equals(
                "STATE_SEEK",
                true
            )
            || logEventName.equals(
                "STATE_RESUMED",
                true
            )

            || logEventName.equals(
                "STATE_CLICKED",
                true
            )
        ) {

            makeEventRequest(
                "post_play_events_request",
                logEventName
            )  // Only trigger network calls for these events


            if (!TextUtils.isEmpty(logEventName) && logEventName.equals(
                    "STATE_PLAYING",
                    true
                )
            ) {
                // Here check if timer is running for capturing event on given interval, if not running, just start it
                if (!isPingTimerRunning) {
                    isPingTimerRunning = true
                    startPingTimer()

                }

                if (!isMitigationTimerRunning) {
                    isMitigationTimerRunning = true
                    startMitigationTimer()
                }
            }



            MyUtils.printMyLogs("Event : " + MyUtils.getStandardisedEventName(logEventName))

            MyUtils.enableDebugging(
                "#ProbeMitigationSDK : " + MyUtils.getStandardisedEventName(
                    logEventName
                ) + " triggered successful"
            )


        } else if (!TextUtils.isEmpty(logEventName) && logEventName.equals(
                "onBackPressed",
                true
            )
        ) {
            // At the time of video playing if user hit device back button then playerState should be idle in this case

            playerState = "STATE_IDLE"


            MyUtils.printMyLogs("Event : " + MyUtils.getStandardisedEventName(logEventName))


        }


    }


    private fun startPingTimer() {
        try {
            if (!TextUtils.isEmpty(getConfigurationFromLocal("kaInterval"))) {
                pingIntervalTime = 1000 * (getConfigurationFromLocal("kaInterval").toLong())

                // pingIntervalTime = 60000
            }
        } catch (e: Exception) {
        }

        pingTimerHandler.postDelayed(
            Runnable { //do something
                try {

                    pingTimerHandler.postDelayed(
                        pingRunnable,
                        pingIntervalTime
                    )
                    calculateDurationSinceLastReport()

                    makeEventRequest("periodic_events_request", "")


                } catch (e: Exception) {
                    isPingTimerRunning = false
                }
            }.also { pingRunnable = it },
            pingIntervalTime
        )
    }


    private fun startMitigationTimer() {

        try {
            if (!TextUtils.isEmpty(getConfigurationFromLocal("kcInterval"))) {
                mitigationIntervalTime = 1000 * (getConfigurationFromLocal("kcInterval").toLong())

                //  mitigationIntervalTime = 120000
            }
        } catch (e: Exception) {
        }

        mitigationTimerHandler.postDelayed(
            Runnable { //do something
                try {

                    mitigationTimerHandler.postDelayed(
                        mitigationRunnable,
                        mitigationIntervalTime
                    )

                    makeGetMitigationConfigCall() // making mitigation call at each mitigation time meetup

                } catch (e: Exception) {
                    isMitigationTimerRunning = false
                }
            }.also { mitigationRunnable = it },
            mitigationIntervalTime
        )
    }

    private fun calculateDurationSinceLastReport() {
        try {
            if (!TextUtils.isEmpty(getConfigurationFromLocal("last_periodic_event_time")) && getConfigurationFromLocal(
                    "last_periodic_event_time"
                ).toLong() > 0
            ) {
                val getDurationSinceLastReport =
                    (System.currentTimeMillis() - (getConfigurationFromLocal("last_periodic_event_time")).toLong()) / 1000
                saveConfigurationInLocal(
                    "duration_since_last_report",
                    getDurationSinceLastReport.toString()
                )
                MyUtils.printMyLogs("duration_since_last_report $getDurationSinceLastReport")
            } else {
                saveConfigurationInLocal(
                    "duration_since_last_report",
                    "0"
                ) // This is first time periodic request Or the new session started, so hence it will also calculated as first time periodic request
            }


            saveConfigurationInLocal(
                "last_periodic_event_time",
                System.currentTimeMillis().toString()
            ) // Save the current periodic event time, so that delta(duration_since_last_report) can be calculated for next interval sync-up
        } catch (e: Exception) {
        }

    }

    private fun stopPingAndMitigationJob() {
        try {
            pingTimerHandler.removeCallbacks(pingRunnable) //stop handler when player ui is not visible
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mitigationTimerHandler.removeCallbacks(mitigationRunnable) //stop mitigation handler when player ui is not visible or back button hit or video ended after playing full content
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isPingTimerRunning = false
        isMitigationTimerRunning = false
    }

    private fun calculateBitrateSwitch() {
        // This function is to calculate ABR switch, where we need to check if bitrate selected or switch is up/down

        val getBitrateSwitchDelta = try {
            (getConfigurationFromLocal(
                "bitrate_switch_to"
            )).toInt() - (getConfigurationFromLocal("last_bitrate_selected")).toInt()
        } catch (e: Exception) {
            0
        }

        if (getBitrateSwitchDelta > 0) {
            // This means bitrate is positive since last bitrate selected, so we can say switchType is "up"

            totalBitrateSwitchUpCount++

            insertUpSwitch(getConfigurationFromLocal("bitrate_switch_to").toLong())
        } else if (getBitrateSwitchDelta != 0) {
            totalBitrateSwitchDownCount++
            insertDownSwitch(getConfigurationFromLocal("bitrate_switch_to").toLong())
        }


        saveConfigurationInLocal(
            "last_bitrate_selected",
            getConfigurationFromLocal("bitrate_switch_to")
        )

    }

    private fun insertUpSwitch(selectedBitrate: Long) {

        try {
            jsonObjectUpSwitch.addProperty(
                ((System.currentTimeMillis()) / 1000).toString(),
                selectedBitrate
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun insertDownSwitch(selectedBitrate: Long) {
        try {
            jsonObjectDownSwitch.addProperty(
                ((System.currentTimeMillis()) / 1000).toString(),
                selectedBitrate
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createBitRateSwitchPayload(): JsonObject {
        val finalJsonObject = JsonObject()
        try {
            if (jsonObjectUpSwitch.size() > 0) {
                finalJsonObject.add("up", jsonObjectUpSwitch)
            }

            if (jsonObjectDownSwitch.size() > 0) {
                finalJsonObject.add("down", jsonObjectDownSwitch)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        /*  val returnPayloadJsonString: String
          try {
              returnPayloadJsonString = finalJsonObject.toString()


          } catch (e: Exception) {
              return ""
          }*/

        return finalJsonObject
    }


    private fun createStallsPayload(): JsonObject {
        // This function is to calculate the total stalls during last ping to this ping

        val obj = JsonObject()
        try {
            if (bufferCount != 0) {
                obj.addProperty("count", bufferCount)
                obj.addProperty("duration", totalBufferDurSinceLastPing)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        return obj

    }

    private fun initAll(myContext: Context) {
        MyUtils.setLibContext(myContext) // initiate Context first
        appContext = myContext
        appSharedPreferences = myContext.getSharedPreferences("rkt_probe_pref", 0)
        appEditor = appSharedPreferences?.edit()


        /*  appEditor?.remove("rejected_payload_data")?.apply()
          appEditor?.remove("rejected_payload_data_size")?.apply()*/

        /*handleLastSavedRequest()*/


    }

    private fun handleLastSavedRequest() {
        // This block execute in two cases one when user comes first time on the app & also on each new session started, so we check if there is any last saved data that could not send to server due to any http connection issue, So now send that request again and clear all shared preference data
        MyUtils.printMyLogs("Last Rejected Payload : " + getConfigurationFromLocal("rejected_payload_data"))

        if (!TextUtils.isEmpty(getConfigurationFromLocal("rejected_payload_data"))) {

            val finalPayLoad = "[" + getConfigurationFromLocal("rejected_payload_data") + "]"

            /* var finalPayLoad =
                 JSONArray().put(JSONObject(getConfigurationFromLocal("rejected_payload_data"))).toString()*/

            AppRepository.syncLastRejectedPayloadsToServer(
                appContext,
                MyUtils.getBaseUrl(),
                "sync_last_failed_request",
                finalPayLoad, playerState,
                this
            )
        }


        // Here we assuming that this block will be executed only once throughout the current session, So just clear old saved session from shared preference data on receiving this request response

    }


    private fun reportExoPlayerErrors(error: ExoPlaybackException) {

        when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> {

                hitErrorLog("Error Code : " + "0, " + "Error Name : " + "TYPE_SOURCE, " + "Error Details : " + error.sourceException.message)

            }
            ExoPlaybackException.TYPE_RENDERER -> {

                hitErrorLog("Error Code : " + "1, " + "Error Name : " + "TYPE_RENDERER, " + "Error Details : " + error.sourceException.message)
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> {

                hitErrorLog("Error Code : " + "2, " + "Error Name : " + "TYPE_UNEXPECTED, " + "Error Details : " + error.sourceException.message)
            }
            ExoPlaybackException.TYPE_REMOTE -> {

                hitErrorLog("Error Code : " + "3, " + "Error Name : " + "TYPE_REMOTE, " + "Error Details : " + error.sourceException.message)
            }
            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> {

                hitErrorLog("Error Code : " + "4, " + "Error Name : " + "TYPE_OUT_OF_MEMORY, " + "Error Details : " + error.sourceException.message)
            }
        }
    }

    private fun hitErrorLog(stringErrorDetails: String) {

        saveConfigurationInLocal(
            "previous_event_name",
            ("ERROR")
        )

        errorDetails = stringErrorDetails

        makeEventRequest(
            "post_play_events_request",
            "ERROR"
        )

        d("Exo_Error", stringErrorDetails)
    }


    private fun releasePlayerCallBack() {
        if (isPingTimerRunning || isMitigationTimerRunning) { // check if job is running or not, if running then stop it, else ignore
            stopPingAndMitigationJob()
        }

    }


    private fun getDiffTime(): Long // Reset those things that are not required to fetched from local storage on each new app launch
    {
        return if (!TextUtils.isEmpty(getConfigurationFromLocal("duration_since_last_report"))) {
            getConfigurationFromLocal("duration_since_last_report").toLong()
        } else {
            0
        }

    }

    private fun makeRegistrationCall() {

        //  makeTestPostCall()

        AppRepository.doDeviceRegistration(
            appContext,
            MyUtils.registration_base_url,
            "deviceRegistration",
            MyUtils.createRegistrationPayload(
                appContext,
                MyUtils.encryptInMd5(getConfigurationFromLocal("ueid")),
                getConfigurationFromLocal("mitigationApplTime"),
                getConfigurationFromLocal("get_ip_address")   // No need to encrypt, requested by rupesh
            ), "",
            this
        )


    }


    private fun makeGetMitigationConfigCall() {
        AppRepository.getMitigationConfiguration(
            appContext,
            MyUtils.mitigation_base_url,
            "fetchMitigationConfig",
            MyUtils.createGetMitigationPayload(
                appContext,
                MyUtils.encryptInMd5(getConfigurationFromLocal("ueid")),
                getConfigurationFromLocal("mitigationApplTime"),
                getConfigurationFromLocal("get_ip_address")
            ), "",
            this
        )

    }


    private fun makeTestPostCall() {
        AppRepository.doTestDeviceHandshake(
            appContext,
            MyUtils.test_base_url,
            "deviceRegistration",
            MyUtils.createRegistrationPayload(
                appContext,
                MyUtils.encryptInMd5(getConfigurationFromLocal("ueid")),
                getConfigurationFromLocal("mitigationApplTime"),
                getConfigurationFromLocal("get_ip_address")   // No need to encrypt, requested by rupesh
            ), "",
            this
        )
    }


    private fun hitGetIp() {

        val getIpInputURL = if (ipRequestTryCount == 0) {
            MyUtils.get_ip_base_url_1
        } else {
            MyUtils.get_ip_base_url_2
        }
        ipRequestTryCount++

        AppRepository.hitToGetIpAddress(
            appContext,
            getIpInputURL,
            "get_ip_address",
            "get_ip_address", "",
            this
        )


    }


    override fun updateAfterJobFinish(
        apiResponse: String,
        requestStatus: String,
        requestType: String,
        eventNameIfAny: String,
        payloadJson: String // payloadJson is a json string that we have sent to cloud
    ) {

        if (!TextUtils.isEmpty(requestStatus) && requestStatus.equals(
                "success",
                true
            ) && requestType.equals("deviceRegistration", true)
        ) {


            MyUtils.printMyLogs("RequestType : $requestType,  Device Registration Response From Api: $apiResponse")

            MyUtils.saveDeviceRegistrationResponse(apiResponse, appEditor)


            handleLastSavedRequest()   // This we need to discuss, like the place where we can hit this request

        } else if (!TextUtils.isEmpty(requestStatus) && requestType.equals("get_ip_address", true)
        ) {

            if (!TextUtils.isEmpty(apiResponse) && MyUtils.isReceivedIpValid(apiResponse)) {
                saveConfigurationInLocal("get_ip_address", apiResponse)
                makeRegistrationCall()
                //   makeGetMitigationConfigCall()

            } else if (ipRequestTryCount < 2) {
                hitGetIp()
            } else {
                // Lets say both ip fetch request get failed then we will give-up our attempts & do the registration with device ip

                saveConfigurationInLocal("get_ip_address", MyUtils.getdeviceIpAddress().toString())
                makeRegistrationCall()
                //  makeGetMitigationConfigCall()
            }


        } else if (!TextUtils.isEmpty(requestStatus) && requestStatus.equals(
                "success",
                true
            ) && requestType.equals("fetchMitigationConfig", true)
        ) {


            MyUtils.printMyLogs("RequestType : $requestType,  Fetch Mitigation Response From Api: $apiResponse")

            MyUtils.saveMitigationData(apiResponse, appEditor)

        } else if (!TextUtils.isEmpty(requestStatus) && requestStatus.equals(
                "success",
                true
            ) && requestType.equals("post_play_events_request", true)
        ) {
            MyUtils.printMyLogs("Server Response : $apiResponse")

            saveConfigurationInLocal(
                "previous_event_name",
                (eventNameIfAny)
            )


        } else if (!TextUtils.isEmpty(requestStatus) && requestStatus.equals(
                "success",
                true
            ) && requestType.equals("periodic_events_request", true)
        ) {

            /* MyUtils.enableDebugging("Ping triggered successful")*/
            MyUtils.printMyLogs("Server Response : $apiResponse")


            /*trackSelector?.let { MitigationSdk.applyMitigation(it, appContext, appSharedPreferences,myPlayer) }*/


        } else if (!TextUtils.isEmpty(requestStatus) && requestStatus.equals(
                "success",
                true
            ) && requestType.equals("sync_last_failed_request", true)
        ) {

            MyUtils.printMyLogs("Server Response : $apiResponse")
            MyUtils.conditionalLocalStorageDelete("rejected_payload_data", appEditor)


        } else if (requestType.equals("get_cdn", true)
        ) {
            // here we need to override the last saved cdn for previous video, So whatever is the status of request failed or success we dont botthered here....Just save the cdn value

            MyUtils.printMyLogs("Api Response for fetch CDN : $apiResponse")

            if (!TextUtils.isEmpty(apiResponse) && apiResponse.equals("null", true)) {
                saveConfigurationInLocal("cdn", "NA")
            } else {
                saveConfigurationInLocal("cdn", apiResponse)
            }


        } else {
            // Whatever request getting failed, we handling those request in this block

            if (!TextUtils.isEmpty(payloadJson) && (appSharedPreferences!!.getInt(
                    "rejected_payload_data_size",
                    0
                ) < 6)
            ) { // Here we keeping the data for which request could not send to server.(This include only last failed attempt data)
                if (!requestType.equals("sync_last_failed_request", true)) {
                    MyUtils.saveAllFailedRequestResponse(
                        payloadJson, getConfigurationFromLocal("rejected_payload_data"),
                        appSharedPreferences!!.getInt("rejected_payload_data_size", 0), appEditor

                    )

                    // Save all request data other than the "sync_last_failed_request". Because there is no sense of storing the
                    // response same failed response again and again....because "sync_last_failed_request" request we use to sync data of other failed request like event and periodic etc.
                }


            }


            MyUtils.printMyLogs("Server Response : $apiResponse")
        }


    }


    fun calculateTotalPlayBack() // This function we use to calculate total playback duration within this active session also playback duration since last ping
    {

        if (playBackStartTime.toInt() != 0) {
            totalPlayBackDurSinceLastPing += ((System.currentTimeMillis()) - playBackStartTime)
            totalPlayBackDurForThisSession += ((System.currentTimeMillis()) - playBackStartTime)
            playBackStartTime = 0  // after calculating the delta, just make it zero
        }


    }

    override fun deviceAndUserDataBus(
        ueid: String?,
        appContext: Context, enable_logs: Boolean
    ) {
        MyUtils.isLogsEnable = enable_logs

        initAll(appContext) // init all local variables


        MyUtils.enableDebugging("#ProbeMitigationSDK : Registration for mitigation successful")



        if (!TextUtils.isEmpty(ueid)) {
            saveConfigurationInLocal(
                "ueid",
                ueid.toString()
            )
        }



        hitGetIp()  // Here we hitting get  api request, so after getting response from above api request, we will hit the registration...Condition : if above request failed then we try hitting the same request with different url, and we only try total 2 attempts to fetch the api, if all 2 request get failed we hit the registration normally...if get success in any these 2 request then also do the registration


    }

    override fun videoAssetDetailsDataBus(mPlayer: SimpleExoPlayer?, videoAssetDetails: String) {
        syncSdkWithPlayer(mPlayer)

        saveDataFromApp(videoAssetDetails)
    }


    override fun eventReportingDataBus(eventName: String, eventDetailsIfAny: String) {
        if (!TextUtils.isEmpty(eventName) && eventName.equals("ERROR", true)) {
            errorDetails = eventDetailsIfAny   // tracking kind of sos errors from binge app
            hitErrorLog("ERROR")

            MyUtils.enableDebugging(
                "#ProbeMitigationSDK : ERROR triggered successful"
            )
        } else if (!TextUtils.isEmpty(eventName) && (eventName.equals(
                "STOPPED",
                true
            ) || eventName.equals(
                "PLAYCLICKED",
                true
            )) // Only two user generated events will be entertained by probesdk.(STOPPED & PLAYCLICKED) all other will be ignored
        ) {


            MyUtils.printMyLogs("Event : " + MyUtils.getStandardisedEventName(eventName))


            MyUtils.enableDebugging(
                "#ProbeMitigationSDK : " + MyUtils.getStandardisedEventName(
                    eventName
                ) + " triggered successful"
            )

            if (eventName.equals("PLAYCLICKED", true)) {
                MyUtils.conditionalLocalStorageDelete("clear_data_on_new_session", appEditor)
                saveConfigurationInLocal(
                    "previous_event_name",
                    ("STATE_CLICKED")
                )
            }


            releasePlayerCallBack() // If event is equal to "STOPPED" or PLAY CLICKED then release the player and stop the ping timer
            makeEventRequest(
                "post_play_events_request",
                eventName
            )
        }


    }

    override fun bufferingConfigDataBus(builder: DefaultLoadControl.Builder) {
        MitigationSdk.applyBufferingConfiguration(builder)
    }

    override fun bitrateConfigDataBus(): DefaultTrackSelector.Parameters? {
        return MitigationSdk.applyBitrateConfiguration()
    }


}