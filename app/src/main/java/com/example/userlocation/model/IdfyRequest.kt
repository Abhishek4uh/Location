package com.example.userlocation.model


import com.google.gson.annotations.SerializedName

data class IdfyRequest(
    @SerializedName("config")
    var config: Config?,
    @SerializedName("data")
    var data: Data?,
    @SerializedName("reference_id")
    var referenceId: String?
)