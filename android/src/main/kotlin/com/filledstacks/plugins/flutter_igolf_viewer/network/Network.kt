package com.filledstacks.plugins.flutter_igolf_viewer.network

import android.util.Log
import com.google.gson.Gson
import com.l1inc.viewer.Course3DRenderer
import com.filledstacks.plugins.flutter_igolf_viewer.network.request.BaseRequest
import com.filledstacks.plugins.flutter_igolf_viewer.network.response.CourseScorecardDetailsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class Network {

    private var service = NetworkService.provideService()


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

    private fun loadVectorDetails(
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

    private fun loadGPSdetails(
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