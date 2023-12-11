package com.filledstacks.plugins.flutter_igolf_viewer.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

object NetworkService {

    private val host = "https://api-connect.igolf.com/rest/action/"

    private val CONNECT_TIMEOUT = 40
    private val WRITE_TIMEOUT = 40
    private val TIMEOUT = 40

    private val logging =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create())
            .client(getHttpClient())
            .build()
    }

    fun provideService(): iGolfService {
        return provideRetrofit().create(iGolfService::class.java)
    }

    private fun getHttpClient() = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(logging)
        .hostnameVerifier(getHostnameVerifier())
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    private fun getHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ ->
            true
        }
    }


}