package com.example.userlocation.model


import com.google.gson.annotations.SerializedName

data class IdfyResponse(
    @SerializedName("capture_expires_at")
    var captureExpiresAt: Any?,
    @SerializedName("capture_link")
    var captureLink: String?,
    @SerializedName("profile_id")
    var profileId: String?
)