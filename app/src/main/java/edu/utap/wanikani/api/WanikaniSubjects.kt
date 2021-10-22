package edu.utap.wanikani.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.json.JSONArray

data class WanikaniSubjects  (
    @SerializedName("characters")
    val cha : String,
    @SerializedName("meaning_mnemonic")
    val meaning_mnemonic : String,
    @SerializedName("meanings")
    val meanings : MutableList<Any>,
    @SerializedName("reading_mnemonic")
    val reading_mnemonic : String,
    @SerializedName("id")
    var subject_id : Int,
    @SerializedName("character_images")
    var character_image : MutableList<Any>,
    @SerializedName("amalgamation_subject_ids")
    var related_subject_ids : MutableList<Int>

)

data class WanikaniAssignments  (
    @SerializedName("subject_id")
    var sub_id:Int,
    @SerializedName("meaning_mnemonic")
    val meaning_mnemonic : String,
    @SerializedName("meanings")
    val meanings : MutableList<Any>,
    @SerializedName("reading_mnemonic")
    val reading_mnemonic : String,
    @SerializedName("available_at")
    val available_at : String
)

data class WanikaniUser (
    @SerializedName("username")
    var username:String,
    @SerializedName("level")
    var level:String
)
