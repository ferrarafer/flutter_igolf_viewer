package com.filledstacks.plugins.flutter_igolf_viewer.network.request

import com.google.gson.annotations.SerializedName

class CourseScorecardListRequest(
    @SerializedName("id_courseArray") val coursesIds : ArrayList<String>,
    @SerializedName("courseName") val courseName : Int
)
