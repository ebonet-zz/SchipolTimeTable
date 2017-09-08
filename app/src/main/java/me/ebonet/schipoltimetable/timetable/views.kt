package me.ebonet.schipoltimetable.timetable

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.timetable_activity.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import me.ebonet.schipoltimetable.*

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
    }

}

class TimeTableActivity : AppCompatActivity(), TimeTableView {


    private var isArrivalShowing = false

    private val presenter: TimeTablePresenter = TimeTablePresenter(createClient())

    private var adapter: TimeTableAdapter? = null

    override fun onResume() {
        super.onResume()
        presenter.attach(this)
    }

    override fun onPause() {
        presenter.detach()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timetable_activity)

        flightTypeSelector.setOnNavigationItemSelectedListener { changeList(R.id.arrivals == it.itemId) }
        flightListView.layoutManager = LinearLayoutManager(this)

        adapter = TimeTableAdapter()
        flightListView.adapter = adapter
        changeList(true)
    }

    // This should not be in the view
    private fun changeList(toArrival: Boolean): Boolean {
        if( toArrival != isArrivalShowing ){
            if (toArrival) presenter.showFlightArriving() else presenter.showFlightsDeparting()
            isArrivalShowing = toArrival
        }
        return true
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
}

class FlightViewHolder(view:View): RecyclerView.ViewHolder(view) {
    val flightNameView: TextView? = view.findViewById(R.id.flightNameView)
}

class TimeTableAdapter() : RecyclerView.Adapter<FlightViewHolder>() {
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
