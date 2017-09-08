package me.ebonet.schipoltimetable.timetable

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import me.ebonet.schipoltimetable.FlightListResponse
import me.ebonet.schipoltimetable.FlightResponse
import me.ebonet.schipoltimetable.SchipolApi
import retrofit2.Response

data class Flight(
        val name:String?,
        val direction: String?
)

class TimeTablePresenter(private var api: SchipolApi) {

    private var view: TimeTableView = TimeTableView.NOOP

    fun showFlightArriving() = updateList("A")

    fun showFlightsDeparting() = updateList("D")

    private fun updateList(direction: String) = async(CommonPool){ // What if I keep changing them everytime?

        view.showLoading()
        val response: Response<FlightListResponse> = api.getFlights(direction).execute() ?: return@async

        if (response.isSuccessful) {
            val flights = response.body()?.flights?.map { networkResponseToModel(it) } ?: return@async
            view.updateFlightList(flights)
        } else {
            view.showError("wtf")
        }

        view.hideLoading()

    }

    private fun networkResponseToModel(flightResponse: FlightResponse): Flight = Flight(
            flightResponse.flightName,
            flightResponse.flightDirection
    )

    fun attach(view: TimeTableView) {
        this.view = view
    }

    fun detach() {
        view = TimeTableView.NOOP
    }
}