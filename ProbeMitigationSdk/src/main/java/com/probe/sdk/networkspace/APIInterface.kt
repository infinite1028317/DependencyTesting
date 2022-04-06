package com.probe.sdk.networkspace

import retrofit2.Call
import retrofit2.http.*

// App APIInterface class for making http callbacks
internal interface APIInterface {
    @GET
    fun getStringResponse(@Url Url: String?): Call<String?>?

    // Below request is working perfectly for sending raw json payload in post request without sending any post param
    @Headers("Content-Type: application/json")
    @POST("/api/analysis")
    fun dataPostRequestCall(@Body body: String?): Call<String?>?

    @Headers("Content-Type: application/json")
    @POST("/register-session")
    fun doRegistrationCall(@Body body: String?): Call<String?>?

    @Headers("Content-Type: application/json")
    @POST("/get-mitigation-config")
    fun getMitigationConfigurationCall(@Body body: String?): Call<String?>?

    @Headers("Content-Type: application/json")
    @POST("/api/analysis")
    fun syncFailedRequestDataCall(@Body body: String?): Call<String?>?

    @Headers("Content-Type: application/json")
    @POST("/register-session")
    fun hitPostCall(@Body body: String?): Call<String?>?

    @GET
    fun readHeader(@Url url: String?): Call<String?>?

    @GET
    fun getIpAddressCall(@Url url: String?): Call<String?>?
}