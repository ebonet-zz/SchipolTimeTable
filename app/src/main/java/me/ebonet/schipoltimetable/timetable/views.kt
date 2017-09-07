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
import retrofit2.Response

interface TimeTableView {
    fun updateFlightList(flights: List<Flight>)

    fun showError(error: String)
}

class TimeTableActivity : AppCompatActivity(), TimeTableView {
    override fun updateFlightList(flights: List<Flight>) {
        runOnUiThread { adapter?.flightList = flights }
    }

    override fun showError(error: String) {
        Log.e(TAG, error)
    }

    private var isArrivalShowing = false

    private var presenter: TimeTablePresenter? = null

    private var adapter: TimeTableAdapter? = null

    override fun onDestroy() {
        presenter = null
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timetable_activity)

        val api = createClient()
        presenter = TimeTablePresenter(api, this)

        flightTypeSelector.setOnNavigationItemSelectedListener { changeList(R.id.arrivals == it.itemId) }
        flightListView.layoutManager = LinearLayoutManager(this)

        adapter = TimeTableAdapter()
        flightListView.adapter = adapter
        changeList(true)

    }

    private fun changeList(toArrival: Boolean): Boolean {
        if( toArrival != isArrivalShowing ){
            if (toArrival) presenter?.showFlightArriving() else presenter?.showFlightsDeparting()
            isArrivalShowing = toArrival
        }
        return true
    }

    companion object {
        val TAG = "TimeTableActivity"
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
