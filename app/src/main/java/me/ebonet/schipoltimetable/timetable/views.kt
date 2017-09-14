package me.ebonet.schipoltimetable.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.timetable_activity.*
import me.ebonet.schipoltimetable.*


@SuppressLint("ParcelCreator")
@Parcelize
data class TimeTablePresenterState(
        val displayedFlightList:Int
): Parcelable

interface TimeTableView {
    fun updateFlightList(flights: List<Flight>)

    fun showError(error: String)

    fun showLoading()

    fun hideLoading()

    companion object {
        val NOOP: TimeTableView = object: TimeTableView {
                override fun updateFlightList(flights: List<Flight>) = Unit

                override fun showError(error: String)  = Unit

                override fun showLoading()  = Unit

                override fun hideLoading()  = Unit
            }


        const val NO_VIEW = 0
        const val ARRIVAL = 1
        const val DEPARTURE = 2
    }

}

class TimeTableActivity : AppCompatActivity(), TimeTableView {

    private val presenter: TimeTablePresenter by lazy { TimeTablePresenter(createClient()) }

    private var adapter: TimeTableAdapter? = null

    private val initialState = TimeTablePresenterState(TimeTableView.NO_VIEW)

    override fun onDestroy() {
        presenter.detach()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(
                "PRESENTER_STATE",
                TimeTablePresenterState(presenter.getState())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.timetable_activity)

        flightTypeSelector.setOnNavigationItemSelectedListener {
            presenter.listChangeClicked(if (R.id.arrivals == it.itemId) TimeTableView.ARRIVAL else TimeTableView.DEPARTURE)
            true
        }
        flightListView.layoutManager = LinearLayoutManager(this)

        adapter = TimeTableAdapter()
        flightListView.adapter = adapter

        val state:TimeTablePresenterState = savedInstanceState?.getParcelable("PRESENTER_STATE")?: initialState

        presenter.attach(this, state.displayedFlightList)
    }

    companion object {
        val TAG = "TimeTableActivity"
    }

    override fun hideLoading() {
    }

    override fun showLoading() {
    }

    override fun updateFlightList(flights: List<Flight>) {
        runOnUiThread { adapter?.flightList = flights }
    }

    override fun showError(error: String) {
        Log.e(TAG, error)
    }

    class FlightViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val flightNameView: TextView? = view.findViewById(R.id.flightNameView)
    }

    class TimeTableAdapter : RecyclerView.Adapter<FlightViewHolder>() {
        var flightList: List<Flight> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onBindViewHolder(holder: FlightViewHolder?, position: Int) {
            holder?.flightNameView?.text = with(flightList[position]) {"$name - $direction"}
        }

        override fun getItemCount(): Int = flightList.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FlightViewHolder {
            val parentView = parent ?: throw IllegalArgumentException("Parent is null")
            return FlightViewHolder(LayoutInflater.from(parentView.context).inflate(R.layout.timetable_flightitem, null))
        }
    }
}

