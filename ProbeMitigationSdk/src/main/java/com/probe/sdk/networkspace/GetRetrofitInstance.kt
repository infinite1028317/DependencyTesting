package com.probe.sdk.networkspace

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


/**
 * Created by Robi Kumar Tomar  on 05/01/2022.
 */
internal object GetRetrofitInstance {
    @JvmStatic
    fun getRetrofitInstance(url: String?): Retrofit {

        // Below client added by rkt to prevent the time out exception, Lets see if it works correctly.....if not works just remove the client from retrofit call
        val client = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS).build()
        return Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(url).client(client)
                .build()
    }
}