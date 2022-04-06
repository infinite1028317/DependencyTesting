package com.probe.sdk.networkspace

// App repository class for making http connection

import android.content.Context
import android.text.TextUtils
import com.probe.sdk.otherutils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.MalformedURLException
import java.net.URL

internal class AppRepository {


    companion object {
        private var INSTANCE: AppRepository? = null
        fun getInstance() = INSTANCE
            ?: AppRepository().also {
                INSTANCE = it
            }

        // Use this for postRequest
        fun makeWebServiceCall(
            requestedUrl: String,
            payloadJson: String,
            context: Context,
            requestType: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {
            val scalarService = GetRetrofitInstance.getRetrofitInstance(requestedUrl).create(
                RetrofitDataService::class.java
            )
            val stringCall = scalarService.sendDataToServerOnInterVal(
                "",
                MyUtils.getDeviceID(MyUtils.getLibContext())
            )
            stringCall?.enqueue(object : Callback<String?> {
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        if (!TextUtils.isEmpty(response.body())) {
                            if (!TextUtils.isEmpty(response.body())) {
                                if (!TextUtils.isEmpty(response.body()) && JSONObject(response.body()).getString(
                                        "status"
                                    ).equals("success")
                                ) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        "",
                                        payloadJson
                                    )
                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, "", payloadJson
                                    )
                                }
                            }
                        } else {
                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, "", payloadJson
                            )
                        }
                    } else {
                        InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                            "",
                            "failed",
                            requestType, "", payloadJson
                        )
                    }
                }

                override fun onFailure(call: Call<String?>, t: Throwable) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        "", payloadJson
                    )
                }
            })


        }

        // Use this for getRequest
        fun makeGetRequest(
            requestURL: String,
            requestType: String,
            deviceInfoJson: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {
            val apiInterface: APIInterface =
                GetRetrofitInstance.getRetrofitInstance(requestURL).create(
                    APIInterface::class.java
                )
            val callRootData = apiInterface.getStringResponse(requestURL)
            callRootData?.enqueue(object : Callback<String?> {
                var rsp: String = ""
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    rsp = response.body().toString()

                    //  MyUtility.logMyEvents(rsp)
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        response.body().toString(), "success", requestType, "", deviceInfoJson
                    )

                }

                override fun onFailure(call: Call<String?>, t: Throwable) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        "", deviceInfoJson
                    )
                    call.cancel()
                }
            })
        }

        fun doDeviceRegistration(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(requestURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)

                try {
                    val userCall: Call<String?>? = apiInterface.doRegistrationCall(payloadJson)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if (response.isSuccessful) {
                                if (!TextUtils.isEmpty(response.body())) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        eventNameIfAny, payloadJson
                                    )

                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, eventNameIfAny, payloadJson
                                    )
                                }


                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )
                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        eventNameIfAny, payloadJson
                    )
                    e.printStackTrace()
                }
            }


        }


        fun doTestDeviceHandshake(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(MyUtils.mitigation_base_url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)

                try {
                    val userCall: Call<String?>? = apiInterface.hitPostCall(payloadJson)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if (response.isSuccessful) {
                                if (!TextUtils.isEmpty(response.body())) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        eventNameIfAny, payloadJson
                                    )

                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, eventNameIfAny, payloadJson
                                    )
                                }


                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )
                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        eventNameIfAny, payloadJson
                    )
                    e.printStackTrace()
                }
            }


        }


        fun getMitigationConfiguration(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(requestURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)

                try {
                    val userCall: Call<String?>? =
                        apiInterface.getMitigationConfigurationCall(payloadJson)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if (response.isSuccessful) {
                                if (!TextUtils.isEmpty(response.body())) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        eventNameIfAny, payloadJson
                                    )

                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, eventNameIfAny, payloadJson
                                    )
                                }


                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )
                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        eventNameIfAny, payloadJson
                    )
                    e.printStackTrace()
                }
            }


        }


        // Use this for postRequest for sending payload json as in raw format

        fun initDataSender(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(requestURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)

                printPeriodicPayload(requestType, payloadJson)



                try {
                    val userCall: Call<String?>? = apiInterface.dataPostRequestCall(payloadJson)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if (response.isSuccessful) {
                                if (!TextUtils.isEmpty(response.body())) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        eventNameIfAny, payloadJson
                                    )

                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, eventNameIfAny, payloadJson
                                    )
                                }


                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )
                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        eventNameIfAny, payloadJson
                    )
                    e.printStackTrace()
                }
            }


        }


        fun syncLastRejectedPayloadsToServer(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(requestURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)


                try {
                    val userCall: Call<String?>? =
                        apiInterface.syncFailedRequestDataCall(payloadJson)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {
                            if (response.isSuccessful) {
                                if (!TextUtils.isEmpty(response.body())) {

                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        response.body().toString(),
                                        "success",
                                        requestType,
                                        eventNameIfAny, payloadJson
                                    )

                                } else {
                                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                        "",
                                        "failed",
                                        requestType, eventNameIfAny, payloadJson
                                    )
                                }


                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )
                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType,
                        eventNameIfAny, payloadJson
                    )
                    e.printStackTrace()
                }
            }


        }


        fun hitToGetIpAddress(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {

            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {

                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(requestURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)


                try {
                    val userCall: Call<String?>? = apiInterface.getIpAddressCall(requestURL)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {

                            if (!TextUtils.isEmpty(response.body())) {

                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    response.body()?.trim().toString(),
                                    "success",
                                    requestType,
                                    eventNameIfAny, payloadJson
                                )

                            } else {
                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    "",
                                    "failed",
                                    requestType, eventNameIfAny, payloadJson
                                )
                            }
                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {
                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                "",
                                "failed",
                                requestType, eventNameIfAny, payloadJson
                            )

                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType, eventNameIfAny, payloadJson
                    )

                }
            }


        }


        fun readHeaderByHittingVideoUrl(
            context: Context,
            requestURL: String,
            requestType: String,
            payloadJson: String,
            eventNameIfAny: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {
            var getCDN = ""
            if (!TextUtils.isEmpty(MyUtils.getInternetConnectionType(context)) && (MyUtils.getInternetConnectionType(
                    context
                )).equals("no_network", true)
            ) {
                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                    "",
                    "No Network",
                    requestType, eventNameIfAny, payloadJson
                )
            } else {
                var baseUrl = ""


                MyUtils.printMyLogs("CDN : $requestURL")

                try {
                    val mainUrl = URL(requestURL)
                    baseUrl = mainUrl.protocol + "://" + mainUrl.host
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        "",
                        "failed",
                        requestType, eventNameIfAny, getCDN
                    )
                }


                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)


                try {
                    val userCall: Call<String?>? = apiInterface.readHeader(requestURL)
                    userCall?.enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>, response: Response<String?>) {

                            try {
                                getCDN = response.headers().get("server").toString()
                            } catch (e: Exception) {

                                InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                    getCDN,
                                    "failed",
                                    requestType, eventNameIfAny, getCDN
                                )
                            }


                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                getCDN,
                                "success",
                                requestType,
                                eventNameIfAny, getCDN
                            )


                        }

                        override fun onFailure(call: Call<String?>, t: Throwable) {

                            InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                                getCDN,
                                "failed",
                                requestType, eventNameIfAny, getCDN
                            )

                        }
                    })
                } catch (e: Exception) {
                    InterfaceUpdateAfterJobFinish.updateAfterJobFinish(
                        getCDN,
                        "failed",
                        requestType, eventNameIfAny, getCDN
                    )

                }
            }


        }


        fun readHeader2(
            requestURL: String,
            requestType: String,
            deviceInfoJson: String,
            InterfaceUpdateAfterJobFinish: InterfaceUpdateAfterJobFinish
        ) {
            var baseUrl = ""

            try {
                val url = URL(requestURL)
                baseUrl = url.protocol + "://" + url.host
            } catch (e: MalformedURLException) {
                MyUtils.printMyLogs("CDN : Exception")
            }


            val apiInterface: APIInterface =
                GetRetrofitInstance.getRetrofitInstance(baseUrl).create(
                    APIInterface::class.java
                )
            val callRootData = apiInterface.readHeader(requestURL)
            callRootData?.enqueue(object : Callback<String?> {
                var rsp: String = ""
                override fun onResponse(call: Call<String?>, response: Response<String?>) {
                    if (response.isSuccessful) {
                        var getCDN = response.headers().get("server")
                        MyUtils.printMyLogs("CDN : $getCDN")
                    }

                }

                override fun onFailure(call: Call<String?>, t: Throwable) {
                    MyUtils.printMyLogs("Error on header read for CDN")
                }
            })
        }


        private fun printPeriodicPayload(requestType: String, payloadJson: String) {
            if (!TextUtils.isEmpty(requestType) && requestType.equals(
                    "periodic_events_request",
                    true
                )
            ) {

                MyUtils.printMyLogs("Periodic Payload Sent : $payloadJson")

            } else if (!TextUtils.isEmpty(requestType) && requestType.equals(
                    "post_play_events_request",
                    true
                )
            ) {

                MyUtils.printMyLogs("Event based Payload Sent : $payloadJson")

            }
        }


    }


    interface InterfaceUpdateAfterJobFinish {

        fun updateAfterJobFinish(
            apiResponse: String,
            requestStatus: String,
            requestType: String,
            eventNameIfAny: String,
            payloadJson: String
        )
    }


}