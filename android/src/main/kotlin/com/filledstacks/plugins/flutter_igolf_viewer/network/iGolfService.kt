package com.filledstacks.plugins.flutter_igolf_viewer.network

import com.filledstacks.plugins.flutter_igolf_viewer.network.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

@JvmSuppressWildcards
interface iGolfService{

    @POST
    fun courseScorecardDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

    @POST
    fun courseGPSVectorDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

    @POST
    fun courseGPSDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

}