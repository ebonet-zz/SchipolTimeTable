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

    private var currentView = TimeTableView.NO_VIEW

    fun listChangeClicked(targetView: Int) {
        if (targetView == currentView)
            return

        updateList(targetView)

    }

    private fun updateList(direction: Int) = async(CommonPool){ // What if I keep changing them everytime?

        currentView = if (currentView != direction) direction else return@async

        view.showLoading()

        val response: Response<FlightListResponse> = api.getFlights(if (direction==1) "A" else "D").execute() ?: return@async

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

    fun getState() = currentView

    fun attach(view: TimeTableView, initialViewState: Int) {
        this.view = view
        this.updateList(initialViewState)
    }

    fun detach() {
        view = TimeTableView.NOOP
    }
}