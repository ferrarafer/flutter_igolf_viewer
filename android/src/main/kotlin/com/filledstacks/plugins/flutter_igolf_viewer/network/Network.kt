package com.filledstacks.plugins.flutter_igolf_viewer.network

import android.util.Log
import com.google.gson.Gson
import com.l1inc.viewer.Course3DRenderer
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.BaseRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CountryListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseScorecardListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseTeeDetailsRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.CourseListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.StateListRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.response.CourseListResponse
import com.filledstacks.plugins.flutter_igolf_viewer.network.response.CourseScorecardDetailsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class Network {

    private var service = NetworkService.provideService()

    fun getCountryList(
        apiKey: String,
        secretKey: String,
        continentId: String,
        onLoaded: (countryList: String) -> Unit
    ) {
        service.countryList(
            Auth.getUrlForRequest("CountryList", apiKey, secretKey),
            CountryListRequest(continentId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getCourseDetails(
        apiKey: String,
        secretKey: String,
        courseDetailsRequest: CourseDetailsRequest,
        onLoaded: (courseDetails: String) -> Unit
    ) {
        service.courseDetails(
            Auth.getUrlForRequest("CourseDetails", apiKey, secretKey),
            courseDetailsRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getCourseList(
        apiKey: String,
        secretKey: String,
        courseListRequest: CourseListRequest,
        onLoaded: (courseList: String) -> Unit
    ) {
        service.courseList(
            Auth.getUrlForRequest("CourseList", apiKey, secretKey),
            courseListRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                result.error("COURSE_LIST_ERROR"0, t.message, null)
                onLoaded.invoke(Gson().toJson(mapOf(
                    "page" to 1,
                    "totalPages" to 1,
                    "totalCourses" to 0,
                    "courseList" to null,
                    "Status" to -1,
                    "ErrorMessage" to "${t.message}"
                )))
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getStateList(
        apiKey: String,
        secretKey: String,
        countryId: Int,
        onLoaded: (stateList: String) -> Unit
    ) {
        service.stateList(
            Auth.getUrlForRequest("StateList", apiKey, secretKey),
            StateListRequest(countryId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getTypedCourseList(
        apiKey: String,
        secretKey: String,
        courseListRequest: CourseListRequest,
        onLoaded: (courseListResponse: CourseListResponse) -> Unit
    ) {
        service.courseList(
            Auth.getUrlForRequest("CourseList", apiKey, secretKey),
            courseListRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val courseListResponse = Gson().fromJson(
                    response.body()?.string() ?: "",
                    CourseListResponse::class.java
                )
                onLoaded.invoke(courseListResponse)
            }

        })
    }

    fun getCourseTeeDetails(
        apiKey: String,
        secretKey: String,
        courseTeeDetailsRequest: CourseTeeDetailsRequest,
        onLoaded: (courseTeeDetails: String) -> Unit
    ) {
        service.courseTeeDetails(
            Auth.getUrlForRequest("CourseTeeDetails", apiKey, secretKey),
            courseTeeDetailsRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getCourseScorecardDetails(
        apiKey: String,
        secretKey: String,
        courseId: String,
        onLoaded: (courseScorecardDetails: String) -> Unit
    ) {
        service.courseScorecardDetails(
            Auth.getUrlForRequest("CourseScorecardDetails", apiKey, secretKey),
            BaseRequest(courseId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun getCourseScorecardList(
        apiKey: String,
        secretKey: String,
        courseScorecardListRequest: CourseScorecardListRequest,
        onLoaded: (courseScorecardList: String) -> Unit
    ) {
        service.courseScorecardList(
            Auth.getUrlForRequest("CourseScorecardList", apiKey, secretKey),
            courseScorecardListRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun loadCourseData(
        apiKey: String,
        secretKey: String,
        courseId: String,
        onLoaded: (parDataMap: Map<String?, Array<Int>?>, vectorDataJsonMap: HashMap<String?, String?>) -> Unit
    ) {
        val vectorDataJsonMap = HashMap<String?, String?>()

        loadScorecardDetails(apiKey, secretKey, courseId) { scorecardDetails ->
            loadVectorDetails(apiKey, secretKey, courseId) { vectorDetails ->
                loadGPSdetails(apiKey, secretKey, courseId) { gpsDetails ->
                    try {
                        vectorDataJsonMap[courseId] = vectorDetails
                        vectorDataJsonMap[Course3DRenderer.COURSE_ID] = courseId
                        vectorDataJsonMap[Course3DRenderer.GPS_DETAILS] = gpsDetails
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    onLoaded.invoke(getParData(courseId, scorecardDetails), vectorDataJsonMap)
                }
            }
        }
    }

    private fun getParData(courseId: String, courseScorecardData: String): Map<String?, Array<Int>?> {
        val scorecardResponse =
            Gson().fromJson(courseScorecardData, CourseScorecardDetailsResponse::class.java)
        val parDataMap = HashMap<String?, Array<Int>?>()
        val parData = scorecardResponse?.scorecardList?.firstOrNull()?.parArray
        parDataMap[courseId] = parData?.toTypedArray()
        return parDataMap
    }

    private fun loadScorecardDetails(
        apiKey: String,
        secretKey: String,
        courseId: String,
        onLoaded: (scorecardDetails: String) -> Unit
    ) {
        service.courseScorecardDetails(
            Auth.getUrlForRequest("CourseScorecardDetails", apiKey, secretKey),
            BaseRequest(courseId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun loadVectorDetails(
        apiKey: String,
        secretKey: String,
        courseId: String,
        onLoaded: (vectorDetails: String) -> Unit
    ) {
        service.courseGPSVectorDetails(
            Auth.getUrlForRequest("CourseGPSVectorDetails", apiKey, secretKey),
            BaseRequest(courseId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    fun loadGPSdetails(
        apiKey: String,
        secretKey: String,
        courseId: String,
        onLoaded: (gpsDetails: String) -> Unit
    ) {
        service.courseGPSDetails(
            Auth.getUrlForRequest("CourseGPSDetails", apiKey, secretKey),
            BaseRequest(courseId)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                onLoaded.invoke(response.body()?.string() ?: "")
            }

        })
    }

    private fun addLog(mes: String) = Log.e(javaClass.simpleName, mes)


}