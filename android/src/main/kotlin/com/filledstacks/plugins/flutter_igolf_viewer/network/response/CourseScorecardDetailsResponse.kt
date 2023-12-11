package com.filledstacks.plugins.flutter_igolf_viewer.network.response

import com.google.gson.annotations.SerializedName

class CourseScorecardDetailsResponse(
    @SerializedName("menScorecardList") val scorecardList : ArrayList<ScorecardListItem>? = null)

class ScorecardListItem(@SerializedName("hcpHole") val hcpArray : ArrayList<Int>?,
                        @SerializedName("parHole") val parArray : ArrayList<Int>?)