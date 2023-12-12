package com.filledstacks.plugins.flutter_igolf_viewer.network.response

import com.google.gson.annotations.SerializedName

class CourseListResponse(
    @SerializedName("Status") val status : Int,
    @SerializedName("ErrorMessage") val errorMessage : String,
    @SerializedName("courseList") val courseList : ArrayList<Course>? = null,
    @SerializedName("page") val page : Int? = null,
    @SerializedName("totalPages") val totalPages : Int? = null,
    @SerializedName("totalCourses") val totalCourses : Int? = null
)

class Course(
    @SerializedName("active") val active : Int,
    @SerializedName("id_course") val courseId : String? = null,
    @SerializedName("address1") val address1 : String? = null,
    @SerializedName("address2") val address2 : String? = null,
    @SerializedName("city") val city : String? = null,
    @SerializedName("id_country") val countryId : Int? = null,
    @SerializedName("countryFull") val courseFull : String? = null,
    @SerializedName("countryShort") val courseShort : String? = null,
    @SerializedName("courseName") val courseName : String? = null,
    @SerializedName("distance") val distance : Double? = null,
    @SerializedName("latitude") val latitude : Double? = null,
    @SerializedName("longitude") val longitude : Double? = null,
    @SerializedName("layoutHoles") val layoutHoles : Int? = null,
    @SerializedName("layoutTotalHoles") val layoutTotalHoles : Int? = null,
    @SerializedName("layoutName") val layoutName : String? = null,
    @SerializedName("gpsAvailable") val gpsAvailable : Int? = null,
    @SerializedName("scorecardAvailable") val scorecardAvailable : Int? = null,
    @SerializedName("vectorAvailable") val vectorAvailable : Int? = null,
    @SerializedName("elevationAvailable") val elevationAvailable : Int? = null,
    @SerializedName("recommendRating") val recommendRating : Double? = null,
    @SerializedName("conditionRating") val conditionRating : Double? = null,
    @SerializedName("otherState") val otherState : String? = null,
    @SerializedName("id_state") val stateId : Int? = null,
    @SerializedName("stateFull") val stateFull : String? = null,
    @SerializedName("stateShort") val stateShort : String? = null,
    @SerializedName("thumbnailImage") val thumbnailImage : String? = null
)