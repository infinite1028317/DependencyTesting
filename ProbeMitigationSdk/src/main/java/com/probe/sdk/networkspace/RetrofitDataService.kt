package com.probe.sdk.networkspace

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Created by Robi Kumar Tomar  on 05/01/2022.
 */
internal interface RetrofitDataService {
    @FormUrlEncoded
    @POST("")
    fun sendDataToServer(@Url url: String?, @Field("data") data: String?): Call<String?>?

    @FormUrlEncoded
    @POST("")
    fun sendDataToServerOnInterVal(@Url url: String?, @Field("data") data: String?): Call<String?>?

    @FormUrlEncoded
    @POST("")
    fun eventBasedDataSend(@Url url: String?, @Field("data") data: String?): Call<String?>?

    @FormUrlEncoded
    @POST("")
    fun sendDevicePayload(@Url url: String?, @Field("data") data: String?): Call<String?>?
}