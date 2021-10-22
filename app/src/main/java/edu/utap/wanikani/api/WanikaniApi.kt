package edu.utap.wanikani.api

import android.text.SpannableString
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.lang.reflect.Type


interface WanikaniApi {

    // username: EChenAP password: 123456AP Access Token: ffef2121-13e6-409a-bd8d-78437dc4338e
    //@GET("https://api.wanikani.com/v2/assignments.json")
    //suspend fun  getAssignments() : ListingResponse

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("subjects/?")
    suspend fun api_call(@Query("level") level: Int) : List<WanikaniResponse>

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("user")
    suspend fun getUser() : WanikaniUserResponse

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
//    @GET("subjects/1?types=radical,kanji")
    @GET("subjects/{id}")
    suspend fun single_character(@Path("id") id: Int) : WanikaniResponse

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @PUT("assignments/{id}/start")//example of moving a lesson into reviews once local session is done.
    suspend fun start_assignment(@Path("id") id: Int)

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("assignments?immediately_available_for_lessons") //this is to filter on available for lessons.
    suspend fun get_assignments_for_lesson(): ListingData

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("assignments?immediately_available_for_review") //this is to filter on available for lessons.
    suspend fun get_assignments_for_review(): ListingData

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("assignments") //Checks when the next available assignment is
    suspend fun get_assignments_available_after(@Query("available_after") time: String): ListingData

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("subjects?") //this is to filter on available for lessons.
    suspend fun get_subjects(@Query("ids", encoded = true) ids: String): ListingData

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @GET("subjects?") //this is to filter on available for lessons.
    suspend fun get_subjects_from_ids(@Query("ids", encoded = true) ids: String): WanikaniSubjectsResponse

    @Headers("Authorization: Bearer ffef2121-13e6-409a-bd8d-78437dc4338e")
    @POST("reviews")//example of moving a lesson into reviews once local session is done.
    suspend fun create_review(@Body Request: NestedJSON)

    class NestedJSON internal constructor(val review: NestedJSON_single)

    class NestedJSON_single internal constructor(val assignment_id: String, val incorrect_meaning_answers: String, val incorrect_reading_answers: String)


    class ListingData(
        val data: List<WaniKaniChildrenResponse>
    )
    data class WaniKaniChildrenResponse(val data: WanikaniAssignments, val id: Int)

    data class WanikaniSubjectsResponse(val data: List<WanikaniResponse>)

    data class WanikaniResponse(val data: WanikaniSubjects, val id: Int)

    data class WanikaniUserResponse(val data: WanikaniUser)

    class SpannableDeserializer : JsonDeserializer<SpannableString> {
        // @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): SpannableString {
            return SpannableString(json.asString)
        }
    }

    companion object {
        // Tell Gson to use our SpannableString deserializer
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder().registerTypeAdapter(
                SpannableString::class.java, SpannableDeserializer()
            )
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        // Keep the base URL simple
        var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("api.wanikani.com")
            .build()
        fun create(): WanikaniApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): WanikaniApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    // Enable basic HTTP logging to help with debugging.
                    this.level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.wanikani.com/v2/")
                .client(client)
                .addConverterFactory(buildGsonConverterFactory())
                .build()
                .create(WanikaniApi::class.java)
        }
    }


}