package me.ebonet.schipoltimetable.android

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
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

class TimeTableActivity : AppCompatActivity() {


    private var isArrivalShowing = false

    private var api: SchipolApi? = null

    private var adapter: TimeTableAdapter? = null

    private val bottomBarListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.arrivals -> {
                showArrival()
            }
            R.id.departures -> {
                showDeparture()
            }
        }
        true
    }

    fun showArrival() {
        if (!isArrivalShowing) {
            async(CommonPool) {
                updateList("A")
                isArrivalShowing = true
            }
        }
    }

    fun showDeparture() {
        if (isArrivalShowing) {
            async(CommonPool) {
                updateList("D")
                isArrivalShowing = false
            }
        }
    }

    fun toModel(flightResponse: FlightResponse): Flight = Flight(
            flightResponse.flightName,
            flightResponse.flightDirection
    )

    suspend fun updateList(direction: String) { // What if I keep changing them everytime?

        val response: Response<FlightListResponse> = api?.getFlights(direction)?.execute() ?: return

        if (response.isSuccessful) {
            val flights = response.body()?.flights?.map { toModel(it) } ?: return
            runOnUiThread { adapter?.flightList = flights }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timetable_activity)

        api = createClient()
        flightTypeSelector.setOnNavigationItemSelectedListener(bottomBarListener)
        flightListView.layoutManager = LinearLayoutManager(this)

        adapter = TimeTableAdapter()
        flightListView.adapter = adapter

        showArrival()
    }

    companion object {
        val TAG = "TimeTableActivity"
    }
}

data class Flight(
        val name:String?,
        val direction: String?
)

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
