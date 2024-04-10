package com.filledstacks.plugins.flutter_igolf_viewer.network.request

import com.google.gson.annotations.SerializedName

class CourseDetailsRequest(
    @SerializedName("countryFormat") val countryFormat : Int? = null,
    @SerializedName("id_course") val courseId : String,
    @SerializedName("detailLevel") val detailLevel : Int? = null,
    @SerializedName("stateFormat") val stateFormat : Int? = null
)
