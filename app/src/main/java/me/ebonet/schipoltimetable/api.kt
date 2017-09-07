package me.ebonet.schipoltimetable

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

data class FlightResponse(
        val flightName: String,
        val scheduledDate: String,
        val estimatedLandingTime: String?,
        val actualLandingTime: String?,
        val mainFlight: String,
        val flightDirection: String,
        val publicFlightStater: List<String>?,
        val route: Map<String, List<String>>?,
        val gate: String?,
        val terminal: Int?
)

data class FlightListResponse(
        val flights: List<FlightResponse>
)

const val API_ID = "571f5068"
const val API_KEY = "c3484aa517cea02e7edf103c56ab2bfb"

interface SchipolApi {

    @GET("flights")
    @Headers("ResourceVersion: v3")
    fun getFlights(
            @Query("flightdirection") direction: String,
            @Query("includeDelays") includeDelays: Boolean = false,
            @Query("page") page:Int = 0,
            @Query("app_id") apiID:String = API_ID,
            @Query("app_key") apiKey: String =  API_KEY
    ): Call<FlightListResponse>
}

fun createClient(): SchipolApi = Retrofit.Builder()
            .baseUrl("https://api.schiphol.nl/public-flights/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SchipolApi::class.java);