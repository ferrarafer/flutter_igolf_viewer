package com.filledstacks.plugins.flutter_igolf_viewer.network.request

import com.google.gson.annotations.SerializedName

class CourseListRequest(
    @SerializedName("active") val active : Int,
    @SerializedName("city") val city : String? = null,
    @SerializedName("countryFormat") val countryFormat : Int? = null,
    @SerializedName("courseName") val courseName : String? = null,
    @SerializedName("id_country") val countryId : Int? = null,
    @SerializedName("id_state") val stateId : Int? = null,
    @SerializedName("radius") val radius : String? = null,
    @SerializedName("referenceLatitude") val referenceLatitude : Double? = null,
    @SerializedName("referenceLongitude") val referenceLongitude : Double? = null,
    @SerializedName("stateFormat") val stateFormat : Int? = null,
    @SerializedName("zipcode") val zipcode : String? = null,
    @SerializedName("page") val page : Int? = null,
    @SerializedName("resultsPerPage") val resultsPerPage : Int? = null
)
