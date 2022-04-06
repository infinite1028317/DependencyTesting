package com.probe.sdk.otherutils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.core.content.pm.PackageInfoCompat
import com.google.gson.Gson
import com.probe.sdk.models.MitigationConfigModel
import com.probe.sdk.models.RegistrationResponseModel
import com.probe.sdk.networkspace.GetRetrofitInstance
import com.probe.sdk.networkspace.RetrofitDataService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


internal object MyUtils {

    /* <!--Read Carefully : Below information should be change if there is any change in sdk,payload or integrated it to other platform like firestick etc-->*/
    const val version =
        "1.0.0"  // String - Version of the this payload. Any kind of change in structure should be reflected in the version change. initial version is : 1.0.0
    const val sdkVersion =
        "1.0.0"  // String - sdkVersion is the Version of the this probe sdk. Any new release of this sdk should be reflected in the version change. initial version is : 1.0.0
    const val platform =
        "Android" /*<!--"Platform from where the beacons were sent. For example - iOS,Android, Web*/
    const val player_name = "ExoPlayer"  /* <!--"Name of the player from where the beacons were sent. For example -
    Binge1.0-Exoplayer2.0.",-->*/
    const val deviceType = "Mobile"  // for ex: Mobile, FireTv, Stb

    var dynamicBaseUrl = ""

    /*const val probe_base_url =
        "http://65.1.227.110:8000/"*/

    /*const val probe_base_url =
        "https://qoe.tskytech.com/"*/

    const val probe_base_url =
        "http://3.108.121.176:8000/"


    const val mitigation_base_url = "http://3.108.121.176:8004/"

    const val registration_base_url = "http://3.108.121.176:8004/"

    /*const val registration_base_url = "http://3.108.121.176:8081/r/"*/

    const val get_ip_base_url_1 = "https://checkip.amazonaws.com/"
    const val get_ip_base_url_2 = "https://api.ipify.org/"


    const val noInternetDetail = "NoInternet"
    private var Logs_Key = "ProbeMetrics"

    private var Logs_Key_for_tatasky = "enable_logs"

    const val test_base_url = "http://3.108.121.176:8081/register-session/"


    val sampleCdnURL =
        "https://movieshls001-b-latest.akamaized.net/hls/movie/4/1000224/fulllength/1000224/1000224_1000224_IPAD_ALL_NEW_multi.m3u8?hdnts=st=1644999897~exp=1644999967~acl=/*~hmac=21b5509aea04e2cac9e0d15bda4cbc17792db591008a6d559df4422d2695a25f"


    var appContext: Context? = null

    var isLogsEnable: Boolean = false

    var mitigationID: String =
        "ef02h23342123"  // this is default mitigation id, pass dynamic value to this variable. If dynamic value is null then send the default one.

    internal fun getDeviceID(myContext: Context?): String {
        var deviceId: String? = ""
        try {
            deviceId = Settings.Secure.getString(myContext?.contentResolver, "android_id")
        } catch (e: Exception) {
        }
        return deviceId!!
    }

    internal fun getTodayDateTime(): String {
        return try {
            val cal = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            df.format(cal.time)
        } catch (e: Exception) {
            "Permission Denied"
        }
    }


    internal fun getDeviceManufacturer(): String {
        val manufacturer = Build.MANUFACTURER
        return capitalize(manufacturer)

    }

    internal fun getDeviceModel(): String {
        val model = Build.MODEL
        return capitalize(model)

    }


    internal fun getDeviceBrandName(): String {
        val manufacturer = Build.DEVICE
        val deviceBrandName = Build.BRAND
        return if (deviceBrandName.startsWith(manufacturer)) {
            capitalize(deviceBrandName)
        } else capitalize(deviceBrandName)
    }


    internal fun getDeviceVersion(): String {
        return try {
            val builder = StringBuilder()
            builder.append("android : ").append(Build.VERSION.RELEASE)
            val fields = Build.VERSION_CODES::class.java.fields
            for (field in fields) {
                val fieldName = field.name
                var fieldValue = -1
                try {
                    fieldValue = field.getInt(Any())
                } catch (e: Exception) {
                }
                if (fieldValue == Build.VERSION.SDK_INT) {
                    builder.append(" : ").append(fieldName).append(" : ")
                    builder.append("sdk=").append(fieldValue)
                }
            }
            "OS: $builder"
        } catch (e: Exception) {
            val versionNumber = Build.VERSION.SDK_INT
            "Android Version Name : " + Build.VERSION.CODENAME + "Version API : " + versionNumber
        }
    }

    internal fun getUserAgent(): String {
        return try {
            //   String webViewUserAgent = new WebView(context).getSettings().getUserAgentString();
            val userAgent = System.getProperty("http.agent")
            "Device User Agent : $userAgent"
        } catch (e: Exception) {
            "permission denied by system"
        }
    }


    internal fun isJsonValid(Json: String?): Boolean {
        try {
            JSONObject(Json)
        } catch (ex: JSONException) {
            try {
                JSONArray(Json)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }


    internal fun getAppVersionCodeAndName(context: Context?): String {
        return try {
            val pInfo = context!!.packageManager.getPackageInfo(context!!.packageName, 0)
            val version = pInfo.versionName

            val longVersionCode = PackageInfoCompat.getLongVersionCode(pInfo)
            val versionCode =
                longVersionCode.toInt() // avoid huge version numbers and you will be ok

            "App Version Name : $version, App Version Code : $versionCode"

        } catch (e: Exception) {
            e.printStackTrace()
            "null"
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (!Character.isUpperCase(first)) Character.toUpperCase(first)
            .toString() + s.substring(1) else s
    }

    internal fun firstLetterCapitalize(myString: String?): String {
        try {
            if (myString == null || myString.isEmpty()) {
                return ""
            }

            return myString.substring(0, 1).toUpperCase() + myString.substring(1).toLowerCase();
        } catch (e: Exception) {
            return ""
        }
    }


    internal fun createHandlerForRepetitiveTask() {
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {


                /* ProbeSdk.printMyLogs("0")*/

                Handler(Looper.getMainLooper()).postDelayed(
                    this,
                    5000
                )
            }
        }, 5000)
    }

    private fun sendDataToServer(requestedUrl: String, payloadData: String) {
        val scalarService = GetRetrofitInstance.getRetrofitInstance(requestedUrl)
            .create(RetrofitDataService::class.java)
        val stringCall = scalarService.sendDataToServer("", payloadData)

        stringCall?.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if (response.isSuccessful) {
                    if (!TextUtils.isEmpty(response.body())) {

                        printMyLogs(response.body().toString())
                    }
                } else {

                    printMyLogs("No Data")
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {


                printMyLogs("onFailure :    " + "Request Failed")
            }
        })


    }

    internal fun printMyLogs(logMsg: String) {
        Log.d(Logs_Key, logMsg)
    }

    internal fun enableDebugging(logMsg: String) {
        if (isLogsEnable) {
            /*Log.d(Logs_Key_for_tatasky, logMsg)*/
        }

    }


    internal fun getCDN(myUrl: String): String {

        return try {
            val myURI = Uri.parse(myUrl)

            myURI.host.toString()
        } catch (e: Exception) {
            ""
        }

    }


    internal fun getInternetConnectionType(myContext: Context): String {
        // Checking internet connectivity type
        var netWorkType: String = "no_network"
        try {
            val connectivityMgr =
                myContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            var activeNetwork: NetworkInfo? = null
            if (connectivityMgr != null) {
                activeNetwork = connectivityMgr.activeNetworkInfo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val nc = connectivityMgr.getNetworkCapabilities(connectivityMgr.activeNetwork)
                    if (nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        // connected to mobile data
                        netWorkType = "Cellular"
                    } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        // connected to wifi
                        netWorkType = "WiFi"
                    }
                } else {
                    if (activeNetwork!!.type == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifiT
                        netWorkType = "wifi_data"
                    } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                        // connected to mobile data
                        netWorkType = "Cellular"
                    }
                }
            }
        } catch (e: Exception) {
            netWorkType = "no_network"
        }
        return netWorkType
    }

    internal fun getPublicIpAddress(): String {

        var ipAddress = ""
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        ipAddress = inetAddress.getHostAddress().toString()
                        Log.i("Here is the Address", ipAddress)
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            return ""
        }

        return ipAddress;
    }


    internal fun getdeviceIpAddress(): String? {
        try {
            var myIpAddress: String = ""
            var myIpAddress4: String = ""
            var myIpAddress6: String = ""
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        myIpAddress4 = inetAddress.getHostAddress()

                    }
                    /*  if (!inetAddress.isLoopbackAddress && inetAddress is Inet6Address) {
                          myIpAddress6 = inetAddress.getHostAddress()

                      }*/
                }
            }
            return myIpAddress4
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    internal fun getIpv4HostAddress(): String {
        try {
            NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
                networkInterface.inetAddresses?.toList()?.find {
                    !it.isLoopbackAddress && it is Inet4Address
                }?.let { return it.hostAddress }
            }
            return ""
        } catch (e: Exception) {
            return ""
        }
    }

    internal fun getStandardisedEventName(systemEvent: String): String {
        if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_IDLE", true)) {
            return "IDLE"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_READY", true)) {
            return "READY"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_PLAYING", true)) {
            return "STARTED"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_BUFFERING", true)) {
            return "BUFFERING"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals(
                "STATE_CLICKED",
                true
            )
        ) {
            return "PLAYCLICKED"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_SEEK", true)) {
            return "SEEKED"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_PAUSED", true)) {
            return "PAUSED"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_RESUMED", true)) {
            return "RESUMED"
        } else if (!TextUtils.isEmpty(systemEvent) && systemEvent.equals("STATE_ENDED", true)) {
            return "STOPPED"
        } else if (!TextUtils.isEmpty(systemEvent) && (systemEvent.equals(
                "ERROR",
                true
            ) || systemEvent.equals("STATE_ERROR", true))
        ) {
            return "ERROR"
        } else if (TextUtils.isEmpty(systemEvent)) {
            return "NA"

        } else {
            return systemEvent
        }
    }


    /* fun getStandardisedErrorCode(errorCode: Int): String {
         when (errorCode) {
             (-2) -> {
                // return "9"
                 return "-2"
             }
             (-3) -> {
               //  return "10"
                 return "-3"
             }
             (400) -> {
               //  return "11"
                 return "400"
             }
             (401) -> {
                // return "12"
                 return "401"
             }
             (403) -> {
                 // return "13"
                 return "403"
             }
             (500) -> {
                // return "14"
                 return "500"
             }
             (550) -> {
                 // return "15"
                 return "550"
             }
             (-500) -> {
               //  return "16"
                 return "-500"
             }
             else -> {
                 return errorCode.toString()
             }
         }
     }*/


    /*fun getStandardisedErrorName(errorCode: Int): String {
        when (errorCode) {
            (-2) -> {
                return "SUBSCRIPTION_HAS_EXPIRED"
            }
            (-3) -> {
                return "DEVICE_LIMIT_REACHED"
            }
            (400) -> {
                return "ERROR_401"
            }
            (401) -> {
                return "SESSION_EXPIRED"
            }
            (403) -> {
                return "SUBSCRIPTION_ERROR"
            }
            (500) -> {
                return "SUBSCRIPTION_ERROR"
            }
            (550) -> {
                return "GENERIC_ERROR"
            }
            (-500) -> {
                return "NETWORK_ERROR"
            }
            else -> {
                return "GENERIC_ERROR"
            }
        }
    }*/

    internal fun getStandardisedFrameRate(inputFrameRate: String): Int {
        return try {
            (inputFrameRate.toFloat()).toInt()
        } catch (e: Exception) {
            0
        }
    }

    internal fun getFrameLoss(currentFrameRate: String, lastFrameRate: String): Int {
        try {
            return if (TextUtils.isEmpty(lastFrameRate)) {
                0
            } else if (!TextUtils.isEmpty(currentFrameRate) && !TextUtils.isEmpty(lastFrameRate)) {
                var calculateFrameLoss =
                    ((lastFrameRate.toFloat()) - (currentFrameRate.toFloat())).toInt()

                if (calculateFrameLoss > 0) {
                    calculateFrameLoss
                } else {
                    0
                }

            } else {
                0
            }
        } catch (e: Exception) {
            return 0
        }


    }


    internal fun generateSessionId(): String {
        return try {
            (UUID.randomUUID().toString()
                .substring(0, 3)) + System.currentTimeMillis() + (UUID.randomUUID().toString()
                .substring(0, 3))
        } catch (e: Exception) {
            "COG" + System.currentTimeMillis() + "MIG"
        }
    }

    internal fun encryptInMd5Old(inputString: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest
                .getInstance(MD5)
            digest.update(inputString.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = java.lang.StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return inputString
        }
        return inputString
    }


    internal fun encryptInMd5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }


    internal fun createRegistrationPayload(
        myContext: Context, ueid: String,
        mitigationApplTime: String, clientIP: String
    ): String {
        val obj = JSONObject()
        return try {
            obj.put("version", version)
            obj.put("ueid", ueid)
            obj.put("udid", getDeviceID(myContext))
            obj.put("clientClock", System.currentTimeMillis() / 1000)
            obj.put("mitigationCfgID", mitigationID)
            if (!TextUtils.isEmpty(mitigationApplTime)) {
                obj.put("mitigationApplTime", mitigationApplTime.toLong())
            } else {
                obj.put("mitigationApplTime", ((System.currentTimeMillis()) / 1000))

            }


            obj.put("clientIP", clientIP)
            obj.put("ua", getUserAgent())
            JSONObject().put("req", obj).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }


    }


    internal fun createGetMitigationPayload(
        myContext: Context, ueid: String,
        mitigationApplTime: String, clientIP: String
    ): String {
        val obj = JSONObject()
        return try {
            obj.put("version", version)
            obj.put("ueid", ueid)
            obj.put("udid", getDeviceID(myContext))
            obj.put("mitigationCfgID", mitigationID)
            if (!TextUtils.isEmpty(mitigationApplTime)) {
                obj.put("mitigationApplTime", mitigationApplTime.toLong())
            } else {
                obj.put("mitigationApplTime", ((System.currentTimeMillis()) / 1000))

            }
            obj.put("clientIP", clientIP)
            obj.put("ua", getUserAgent())
            JSONObject().put("configRequest", obj).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }


    }


    internal fun saveAllFailedRequestResponse(
        lastMostRejectedPayload: String,
        lastRejectedSavedPayload: String,
        lastRejectedSavedPayloadSize: Int,
        app_editor: SharedPreferences.Editor?
    ) {
        /*  val obj = JSONObject()*/
        /* val jsonArray = JSONArray()*/

        var finalPayload = ""
        try {

            /*jsonArray.put(lastMostRejectedPayload)*/
            finalPayload = lastMostRejectedPayload
            if (!TextUtils.isEmpty(lastRejectedSavedPayload)) {

                /* jsonArray.put(lastRejectedSavedPayload)*/
                finalPayload = "$finalPayload,$lastRejectedSavedPayload"
            }

            app_editor?.putString(
                "rejected_payload_data",
                finalPayload
            )?.apply()

            app_editor?.putInt(
                "rejected_payload_data_size", lastRejectedSavedPayloadSize + 1

            )?.apply()


        } catch (e: Exception) {
            e.printStackTrace()

        }


    }


    private fun saveConfigurationInLocal(
        config_key: String,
        config_value: String,
        app_editor: SharedPreferences.Editor?
    ) {
        try {
            app_editor?.putString(config_key, config_value)?.apply()
        } catch (e: Exception) {
        }
    }

    internal fun saveDeviceRegistrationResponse(
        apiResponse: String,
        app_editor: SharedPreferences.Editor?
    ) {
        if (!TextUtils.isEmpty(apiResponse) && isJsonValid(apiResponse)) {
            val gson = Gson()
            val outPutResponse = gson.fromJson(apiResponse, RegistrationResponseModel::class.java)
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.version)) {
                saveConfigurationInLocal(
                    "mitigation_version",
                    outPutResponse.registration_response?.version.toString(),
                    app_editor
                )
            }

            if (!TextUtils.isEmpty(outPutResponse.registration_response?.bu)) {
                /*saveConfigurationInLocal(
                    "bu", outPutResponse.registration_response?.bu.toString(), app_editor
                )*/

               /* dynamicBaseUrl = "http://qoe.tskytech.com/"*/

                dynamicBaseUrl = "http://3.108.121.176:8000/"


            }

            if (!TextUtils.isEmpty(outPutResponse.registration_response?.kaInterval)) {
                saveConfigurationInLocal(
                    "kaInterval",
                    outPutResponse.registration_response?.kaInterval.toString(),
                    app_editor
                )
            }

            if (!TextUtils.isEmpty(outPutResponse.registration_response?.kcInterval)) {
                saveConfigurationInLocal(
                    "kcInterval",
                    outPutResponse.registration_response?.kcInterval.toString(),
                    app_editor
                )
            }


            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.pc?.renditionSwitch)) {
                saveConfigurationInLocal(
                    "renditionSwitch",
                    outPutResponse.registration_response?.cfg?.pc?.renditionSwitch.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.pc?.stalls)) {
                saveConfigurationInLocal(
                    "stalls",
                    outPutResponse.registration_response?.cfg?.pc?.stalls.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.pc?.userActions)) {
                saveConfigurationInLocal(
                    "userActions",
                    outPutResponse.registration_response?.cfg?.pc?.userActions.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.pc?.qualityChanged)) {
                saveConfigurationInLocal(
                    "qualityChanged",
                    outPutResponse.registration_response?.cfg?.pc?.qualityChanged.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.startupBuffDuration)) {
                saveConfigurationInLocal(
                    "startupBuffDuration",
                    outPutResponse.registration_response?.cfg?.mc?.startupBuffDuration.toString(),
                    app_editor
                )

                outPutResponse.registration_response?.cfg?.mc?.startupBuffDuration?.let {
                    setStartupBuffDuration(
                        it.toInt()
                    )
                }
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.rebufferingDuration)) {
                saveConfigurationInLocal(
                    "rebufferingDuration",
                    outPutResponse.registration_response?.cfg?.mc?.rebufferingDuration.toString(),
                    app_editor
                )

                outPutResponse.registration_response?.cfg?.mc?.rebufferingDuration?.let {
                    setRebufferingDuration(
                        it.toInt()
                    )
                }
            }

            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.estimatedDownloadRate)) {
                saveConfigurationInLocal(
                    "estimatedDownloadRate",
                    outPutResponse.registration_response?.cfg?.mc?.estimatedDownloadRate.toString(),
                    app_editor
                )

                outPutResponse.registration_response?.cfg?.mc?.estimatedDownloadRate?.let {
                    setEstimatedDownloadRate(
                        it.toInt()
                    )
                }
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.bufferingStyle)) {
                saveConfigurationInLocal(
                    "bufferingStyle",
                    outPutResponse.registration_response?.cfg?.mc?.bufferingStyle.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.mitigationID)) {


                setMitigationID(outPutResponse.registration_response?.cfg?.mc?.mitigationID.toString())
            }
            if (!TextUtils.isEmpty(outPutResponse.registration_response?.cfg?.mc?.mitigationTimestamp)) {
                saveConfigurationInLocal(
                    "mitigationTimestamp",
                    outPutResponse.registration_response?.cfg?.mc?.mitigationTimestamp.toString(),
                    app_editor
                )
            }

            if (!TextUtils.isEmpty(outPutResponse.registration_response?.deviceMapping?.location)) {
                saveConfigurationInLocal(
                    "location",
                    outPutResponse.registration_response?.deviceMapping?.location.toString(),
                    app_editor
                )
            }
        }


    }

    internal fun saveMitigationData(apiResponse: String, app_editor: SharedPreferences.Editor?) {
        if (!TextUtils.isEmpty(apiResponse) && isJsonValid(apiResponse)) {
            val gson = Gson()
            val outPutResponse = gson.fromJson(apiResponse, MitigationConfigModel::class.java)
            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.version)) {
                saveConfigurationInLocal(
                    "mitigation_version",
                    outPutResponse.mitigation_config_response?.version.toString(),
                    app_editor
                )
            }

            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.startupBuffDuration)) {
                saveConfigurationInLocal(
                    "startupBuffDuration",
                    outPutResponse.mitigation_config_response?.mc?.startupBuffDuration.toString(),
                    app_editor
                )

                outPutResponse.mitigation_config_response?.mc?.startupBuffDuration?.let {
                    setStartupBuffDuration(
                        it.toInt()
                    )
                }
            }

            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.rebufferingDuration)) {
                saveConfigurationInLocal(
                    "rebufferingDuration",
                    outPutResponse.mitigation_config_response?.mc?.rebufferingDuration.toString(),
                    app_editor
                )

                outPutResponse.mitigation_config_response?.mc?.rebufferingDuration?.let {
                    setRebufferingDuration(
                        it.toInt()
                    )
                }
            }
            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.estimatedDownloadRate)) {
                saveConfigurationInLocal(
                    "estimatedDownloadRate",
                    outPutResponse.mitigation_config_response?.mc?.estimatedDownloadRate.toString(),
                    app_editor
                )

                outPutResponse.mitigation_config_response?.mc?.estimatedDownloadRate?.let {
                    setEstimatedDownloadRate(
                        it.toInt()
                    )
                }
            }
            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.bufferingStyle)) {
                saveConfigurationInLocal(
                    "bufferingStyle",
                    outPutResponse.mitigation_config_response?.mc?.bufferingStyle.toString(),
                    app_editor
                )
            }
            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.mitigationID)) {

                setMitigationID(outPutResponse.mitigation_config_response?.mc?.mitigationID.toString())
            }
            if (!TextUtils.isEmpty(outPutResponse.mitigation_config_response?.mc?.mitigationTimestamp)) {
                saveConfigurationInLocal(
                    "mitigationTimestamp",
                    outPutResponse.mitigation_config_response?.mc?.mitigationTimestamp.toString(),
                    app_editor
                )
            }


        }


    }


    internal fun conditionalLocalStorageDelete(
        whatDataNeedToDelete: String,
        app_editor: SharedPreferences.Editor?
    ) {
        if (whatDataNeedToDelete.equals("deleteAll", true)) {
            app_editor?.clear()?.apply()

        } else if (whatDataNeedToDelete.equals("rejected_payload_data", true)) {
            app_editor?.remove("rejected_payload_data")?.apply()
            app_editor?.remove("rejected_payload_data_size")?.apply()
        } else if (whatDataNeedToDelete.equals("clear_data_on_new_session", true)) {
            app_editor?.remove("latency")?.apply()
            app_editor?.remove("audio_codec")?.apply()
            app_editor?.remove("video_codec")?.apply()
            app_editor?.remove("resolution")?.apply()
            app_editor?.remove("throughput")?.apply()
            app_editor?.remove("initial_bitrate_selected")?.apply()
            app_editor?.remove("frame_rate")?.apply()
            app_editor?.remove("last_frame_rate")?.apply()
        } else {

        }


    }


    internal fun getHas(videoUrl: String): String {

        try {

            val uri = Uri.parse(videoUrl)

            return if (uri != null) {
                if ((uri.lastPathSegment?.contains("mp3") == true) || (uri.lastPathSegment?.contains(
                        "mp4"
                    ) == true)
                ) {
                    "MSS"
                } else if (uri.lastPathSegment?.contains("m3u8") == true) {
                    "HLS"
                } else {
                    "DASH"
                }
            } else {
                "DASH"
            }
        } catch (e: Exception) {
            return "DASH"
        }


    }


    internal fun isReceivedIpValid(ip: String?): Boolean {
        return Patterns.IP_ADDRESS.matcher(ip).matches()
    }


    private fun setStartupBuffDuration(startupBuffDuration: Int) {

        MitigationConfiguration.access?.setStartupBuffDuration(startupBuffDuration)  // dynamic params
        // MitigationConfiguration.access?.setStartupBuffDuration(15000)  // static params

    }

    private fun setRebufferingDuration(rebufferingDuration: Int) {
        MitigationConfiguration.access?.setRebufferingDuration(rebufferingDuration) // dynamic params
        //   MitigationConfiguration.access?.setRebufferingDuration(5) // static params
    }

    private fun setEstimatedDownloadRate(estimatedDownloadRate: Int) {
        MitigationConfiguration.access?.setEstimatedDownloadRate(estimatedDownloadRate) // dynamic params
        // MitigationConfiguration.access?.setEstimatedDownloadRate(55000)  // static params
    }

    private fun setUeid(ueid: String) {

    }

    fun getLibContext(): Context? {
        return appContext

    }


    fun getPlayerAppName(): String {
        return try {
            getLibContext()?.applicationInfo?.loadLabel(
                getLibContext()
                    ?.packageManager!!
            ).toString()
        } catch (e: Exception) {
            "N/A"
        }
    }

    internal fun setLibContext(sharedContext: Context) {
        appContext = sharedContext

    }


    internal fun setMitigationID(inputMitigationID: String) { // setting mitigation id & creating a global point of access
        if (!TextUtils.isEmpty(inputMitigationID)) {
            mitigationID = inputMitigationID
        }
    }


    internal fun getBaseUrl(): String {

        if (!TextUtils.isEmpty(dynamicBaseUrl)) {
            var checkForLastChar = (dynamicBaseUrl.substring(dynamicBaseUrl.length - 1))

            // here we checking if base url is already have slash or not, If it does not have then append the slash at last of bash url
            if (!(checkForLastChar.equals("/", true))) {
                dynamicBaseUrl = "$dynamicBaseUrl/"
            }

        } else {
            dynamicBaseUrl = probe_base_url
        }

        return dynamicBaseUrl

    }


    /* builder.setBufferDurationsMs(
               30000, 120000,
               15000, DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
           )*/

    /* builder.setBufferDurationsMs(
     12000, 12000,
     1000, 2000
     )*/

    /* Sets the buffer duration parameters.
     Params:
     minBufferMs – The minimum duration of media that the player will attempt to ensure is buffered at all times, in milliseconds.
     maxBufferMs – The maximum duration of media that the player will attempt to buffer, in milliseconds.
     bufferForPlaybackMs – The duration of media that must be buffered for playback to start or resume following a user action such as a seek, in milliseconds.
     bufferForPlaybackAfterRebufferMs – The default duration of media that must be buffered for playback to resume after a rebuffer, in milliseconds. A rebuffer is defined to be caused by buffer depletion rather than a user action.
     Returns:
     This builder, for convenience.*/

}