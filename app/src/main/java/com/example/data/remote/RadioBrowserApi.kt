package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RadioBrowserApi {

    @GET("json/stations/topclick/{limit}")
    suspend fun getTopStations(
        @Path("limit") limit: Int = 40,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioBrowserStationDto>

    @GET("json/stations/bytag/{tag}")
    suspend fun getStationsByTag(
        @Path("tag") tag: String,
        @Query("limit") limit: Int = 30,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioBrowserStationDto>

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String,
        @Query("limit") limit: Int = 30,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<RadioBrowserStationDto>

    companion object {
        private const val BASE_URL = "https://de1.api.radio-browser.info/"

        fun create(): RadioBrowserApi {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "GalaxyRadioApp/1.0 (Android; Cosmic)")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RadioBrowserApi::class.java)
        }
    }
}
