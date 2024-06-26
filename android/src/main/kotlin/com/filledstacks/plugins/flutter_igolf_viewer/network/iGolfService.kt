package com.filledstacks.plugins.flutter_igolf_viewer.network

import com.filledstacks.plugins.flutter_igolf_viewer.network.request.BaseRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CountryListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseScorecardListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseTeeDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.StateListRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

@JvmSuppressWildcards
interface iGolfService{

    @POST
    fun countryList(@Url url: String, @Body body: CountryListRequest): Call<ResponseBody>

    @POST
    fun courseDetails(@Url url: String, @Body body: CourseDetailsRequest): Call<ResponseBody>

    @POST
    fun courseList(@Url url: String, @Body body: CourseListRequest): Call<ResponseBody>

    @POST
    fun stateList(@Url url: String, @Body body: StateListRequest): Call<ResponseBody>

    @POST
    fun courseTeeDetails(@Url url: String, @Body body: CourseTeeDetailsRequest): Call<ResponseBody>

    @POST
    fun courseScorecardDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

    @POST
    fun courseScorecardList(@Url url: String, @Body body: CourseScorecardListRequest): Call<ResponseBody>

    @POST
    fun courseGPSVectorDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

    @POST
    fun courseGPSDetails(@Url url: String, @Body body: BaseRequest): Call<ResponseBody>

}
