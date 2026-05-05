package com.easyfitness.fonte

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOCardio
import com.easyfitness.DAO.DAOFonte
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAOStatic
import com.easyfitness.DAO.Profile
import com.easyfitness.GraphData
import com.easyfitness.R
import com.easyfitness.graph.BarGraph
import com.easyfitness.graph.DateGraph
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.UnitConverter
import com.fitworkoutfast.MainActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry

class FonteGraphFragment : Fragment() {
    var mainActivity: MainActivity? = null
    lateinit var mAdapterMachine: ArrayAdapter<String?>

    //Profile mProfile = null;
    lateinit var mMachinesArray: MutableList<String?>
    val name: String?
        get() = requireArguments().getString("name")
    private val id = 0
    private var functionList: Spinner? = null
    private var machineList: Spinner? = null
    private var currentZoom = DateGraph.zoomType.ZOOM_ALL
    private var mDateGraph: DateGraph? = null
    private var mLineChart: LineChart? = null
    private var mGraphZoomSelector: LinearLayout? = null
    private var mBarGraph: BarGraph? = null
    private var mBarChart: BarChart? = null
    var dB: DAOFonte? = null
        private set
    private var mDbCardio: DAOCardio? = null
    private var mDbStatic: DAOStatic? = null
    private var mDbMachine: DAOMachine? = null
    private var mFragmentView: View? = null
    private val onItemSelectedList: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?,
                position: Int, id: Long
            ) {
                if (parent.getId() == R.id.filterGraphMachine) {
                    updateFunctionSpinner() // Update functions only when changing exercise
                } else if (parent.getId() == R.id.filterGraphFunction) {
                    saveSharedParams()
                }
                drawGraph()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    private val onZoomClick = View.OnClickListener { v: View? ->
        val id = v!!.getId()
        if (id == R.id.allbutton) {
            currentZoom = DateGraph.zoomType.ZOOM_ALL
        } else if (id == R.id.lastweekbutton) {
            currentZoom = DateGraph.zoomType.ZOOM_WEEK
        } else if (id == R.id.lastmonthbutton) {
            currentZoom = DateGraph.zoomType.ZOOM_MONTH
        } else if (id == R.id.lastyearbutton) {
            currentZoom = DateGraph.zoomType.ZOOM_YEAR
        }
        mDateGraph!!.setZoom(currentZoom)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.tab_graph, container, false)
        mFragmentView = view
        functionList = view.findViewById<Spinner?>(R.id.filterGraphFunction)
        machineList = view.findViewById<Spinner?>(R.id.filterGraphMachine)
        val allButton = view.findViewById<Button>(R.id.allbutton)
        val lastyearButton = view.findViewById<Button>(R.id.lastyearbutton)
        val lastmonthButton = view.findViewById<Button>(R.id.lastmonthbutton)
        val lastweekButton = view.findViewById<Button>(R.id.lastweekbutton)

        /* Initialisation des evenements */
        machineList!!.setOnItemSelectedListener(onItemSelectedList)
        functionList!!.setOnItemSelectedListener(onItemSelectedList)

        allButton.setOnClickListener(onZoomClick)
        lastyearButton.setOnClickListener(onZoomClick)
        lastmonthButton.setOnClickListener(onZoomClick)
        lastweekButton.setOnClickListener(onZoomClick)

        /* Initialise le graph */
        mGraphZoomSelector = view.findViewById<LinearLayout?>(R.id.graphZoomSelector)
        mLineChart = view.findViewById<LineChart?>(R.id.graphLineChart)
        mDateGraph = DateGraph(
            getContext(),
            mLineChart,
            getResources().getText(R.string.weightLabel).toString()
        )
        mBarChart = view.findViewById<BarChart?>(R.id.graphBarChart)
        mBarGraph = BarGraph(
            getContext(),
            mBarChart,
            getResources().getText(R.string.weightLabel).toString()
        )

        /* Initialisation de l'historique */
        if (this.dB == null) this.dB = DAOFonte(requireContext())
        if (mDbCardio == null) mDbCardio = DAOCardio(requireContext())
        if (mDbStatic == null) mDbStatic = DAOStatic(requireContext())
        if (mDbMachine == null) mDbMachine = DAOMachine(requireContext())

        return view
    }

    override fun onStart() {
        super.onStart()

        if (this.profil != null) {
            mMachinesArray =
                ArrayList<String?>(0) //Data are refreshed on show //mDbFonte.getAllMachinesStrList(getProfil());
            // lMachinesArray = prepend(lMachinesArray, "All");
            mAdapterMachine = ArrayAdapter<String?>(
                requireContext(), android.R.layout.simple_spinner_item,
                mMachinesArray
            )
            mAdapterMachine!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            machineList!!.setAdapter(mAdapterMachine)
            dB!!.closeCursor()
        }



        if (this.getUserVisibleHint()) refreshData()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.mainActivity = activity as MainActivity
    }

    override fun onStop() {
        super.onStop()
        // Save Shared Preferences
    }

    private fun updateFunctionSpinner() {
        if (machineList!!.getSelectedItem() == null) return  // List not yet initialized.

        val lMachineStr = machineList!!.getSelectedItem().toString()
        val machine = mDbMachine!!.getMachine(lMachineStr)
        if (machine == null) return

        var adapterFunction: ArrayAdapter<String?>? = null
        if (machine.type == DAOMachine.TYPE_STRENGTH) {
            adapterFunction = ArrayAdapter<String?>(
                requireContext(), android.R.layout.simple_spinner_item,
                mainActivity!!.getResources().getStringArray(R.array.graph_functions)
            )
        } else if (machine.type == DAOMachine.TYPE_CARDIO) {
            adapterFunction = ArrayAdapter<String?>(
                requireContext(), android.R.layout.simple_spinner_item,
                mainActivity!!.getResources().getStringArray(R.array.graph_cardio_functions)
            )
        } else if (machine.type == DAOMachine.TYPE_STATIC) {
            adapterFunction = ArrayAdapter<String?>(
                requireContext(), android.R.layout.simple_spinner_item,
                mainActivity!!.getResources().getStringArray(R.array.graph_static_functions)
            )
        }
        adapterFunction!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        functionList!!.setAdapter(adapterFunction)
        if (functionList!!.getSelectedItemPosition() != this.functionListPositionParams) {
            if (this.functionListPositionParams <= (adapterFunction.getCount() - 1)) {
                functionList!!.setSelection(this.functionListPositionParams)
            }
        }
    }

    private fun drawGraph() {
        if (this.profil == null) return

        var lMachine: String? = null
        var lFunction: String? = null
        var lDAOFunction = 0

        mLineChart!!.clear()
        if (machineList!!.getSelectedItem() == null) {
            return
        } // Evite les problemes au cas ou il n'y aurait aucune machine d'enregistree

        if (functionList!!.getSelectedItem() == null) {
            return
        }

        lMachine = machineList!!.getSelectedItem().toString()
        lFunction = functionList!!.getSelectedItem().toString()

        val mDbExercise = DAOMachine(requireContext())
        val m = mDbExercise.getMachine(lMachine)
        if (m == null) return
        val yVals = ArrayList<Entry?>()
        val yBarVals = ArrayList<BarEntry?>()
        val desc = Description()

        if (m.type == DAOMachine.TYPE_STRENGTH) {
            if (lFunction == mainActivity!!.getResources().getString(R.string.maxRep1)) {
                lDAOFunction = DAOFonte.Companion.MAX1_FCT
            } else if (lFunction == mainActivity!!.getResources().getString(R.string.maxRep5d)) {
                lDAOFunction = DAOFonte.Companion.MAX5_FCT
            } else if (lFunction == mainActivity!!.getResources().getString(R.string.sum)) {
                lDAOFunction = DAOFonte.Companion.SUM_FCT
            }
            // Recupere les enregistrements
            var valueList: MutableList<GraphData?>? = null
            if (m.type == DAOMachine.TYPE_STRENGTH) valueList = dB!!.getBodyBuildingFunctionRecords(
                this.profil!!, lMachine, lDAOFunction
            )
            else valueList =
                mDbStatic!!.getStaticFunctionRecords(this.profil!!, lMachine, lDAOFunction)

            if (valueList.size <= 0) {
                // mLineChart.clear(); Already cleared
                return
            }

            val SP = PreferenceManager.getDefaultSharedPreferences(getActivity())
            var defaultUnit = 0
            try {
                defaultUnit = SP.getString("defaultUnit", "0")!!.toInt()
            } catch (e: NumberFormatException) {
                defaultUnit = 0
            }

            for (i in valueList.indices) {
                var value: Entry? = null
                if (defaultUnit == UnitConverter.UNIT_LBS) {
                    desc.setText(lMachine + "/" + lFunction + "(lbs)")
                    value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        UnitConverter.KgtoLbs(valueList.get(i)!!.y.toFloat())
                    ) //-minDate)/86400000));
                } else {
                    desc.setText(lMachine + "/" + lFunction + "(kg)")
                    value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        valueList.get(i)!!.y.toFloat()
                    ) //-minDate)/86400000));
                }
                yVals.add(value)
            }

            mBarGraph!!.chart!!.setVisibility(View.GONE)
            mGraphZoomSelector!!.setVisibility(View.VISIBLE)
            mDateGraph!!.chart!!.setVisibility(View.VISIBLE)
            mDateGraph!!.chart!!.setDescription(desc)
            mDateGraph!!.draw(yVals)
        } else if (m.type == DAOMachine.TYPE_CARDIO) {
            val SP = PreferenceManager.getDefaultSharedPreferences(getActivity())
            var defaultDistanceUnit = UnitConverter.UNIT_KM
            try {
                defaultDistanceUnit = SP.getString("defaultDistanceUnit", "0")!!.toInt()
            } catch (e: NumberFormatException) {
                defaultDistanceUnit = UnitConverter.UNIT_KM
            }

            if (lFunction == mainActivity!!.getResources().getString(R.string.sumDistance)) {
                lDAOFunction = DAOCardio.DISTANCE_FCT
                if (defaultDistanceUnit == UnitConverter.UNIT_KM) {
                    desc.setText(lMachine + "/" + lFunction + "(km)")
                } else {
                    desc.setText(lMachine + "/" + lFunction + "(miles)")
                }
            } else if (lFunction == mainActivity!!.getResources().getString(R.string.sumDuration)) {
                lDAOFunction = DAOCardio.DURATION_FCT
                desc.setText(lMachine + "/" + lFunction + "(min)")
            } else if (lFunction == mainActivity!!.getResources().getString(R.string.speed)) {
                lDAOFunction = DAOCardio.SPEED_FCT
                if (defaultDistanceUnit == UnitConverter.UNIT_KM) {
                    desc.setText(lMachine + "/" + lFunction + "(km/h)")
                } else {
                    desc.setText(lMachine + "/" + lFunction + "(miles/h)")
                }
            }

            // Recupere les enregistrements
            val valueList = mDbCardio!!.getFunctionRecords(
                this.profil!!, lMachine, lDAOFunction
            )

            if (valueList.size <= 0) {
                return
            }

            for (i in valueList.indices) {
                var value: Entry? = null
                if (lDAOFunction == DAOCardio.DURATION_FCT) {
                    value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        DateConverter.nbMinutes(valueList.get(i)!!.y).toFloat()
                    )
                } else if (lDAOFunction == DAOCardio.SPEED_FCT) { // Km/h
                    if (defaultDistanceUnit == UnitConverter.UNIT_MILES) value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        UnitConverter.KmToMiles(valueList.get(i)!!.y.toFloat()) * (60 * 60 * 1000)
                    )
                    else value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        valueList.get(i)!!.y.toFloat() * (60 * 60 * 1000)
                    )
                } else {
                    if (defaultDistanceUnit == UnitConverter.UNIT_MILES) value = Entry(
                        valueList.get(i)!!.x.toFloat(),
                        UnitConverter.KmToMiles(valueList.get(i)!!.y.toFloat())
                    )
                    else value =
                        Entry(valueList.get(i)!!.x.toFloat(), valueList.get(i)!!.y.toFloat())
                }
                yVals.add(value)
            }

            mBarGraph!!.chart!!.setVisibility(View.GONE)
            mGraphZoomSelector!!.setVisibility(View.VISIBLE)
            mDateGraph!!.chart!!.setVisibility(View.VISIBLE)
            mDateGraph!!.chart!!.setDescription(desc)
            mDateGraph!!.draw(yVals)
        }
        if (m.type == DAOMachine.TYPE_STATIC) {
            if (lFunction == mainActivity!!.getResources()
                    .getString(R.string.maxWeightPerDuration)
            ) {
                lDAOFunction = DAOStatic.MAX_FCT
            } else if (lFunction == mainActivity!!.getResources()
                    .getString(R.string.nbSeriesPerDate)
            ) {
                lDAOFunction = DAOStatic.NBSERIE_FCT
            }
            desc.setText(lMachine + "/" + lFunction)
            // Recupere les enregistrements
            var valueList: MutableList<GraphData?>? = null
            valueList = mDbStatic!!.getStaticFunctionRecords(this.profil!!, lMachine, lDAOFunction)

            if (valueList.size <= 0) {
                // mLineChart.clear(); Already cleared
                return
            }

            if (lDAOFunction == DAOStatic.MAX_FCT) {
                val SP = PreferenceManager.getDefaultSharedPreferences(getActivity())
                var defaultUnit = UnitConverter.UNIT_KG
                try {
                    defaultUnit = SP.getString("defaultUnit", "0")!!.toInt()
                } catch (e: NumberFormatException) {
                    defaultUnit = UnitConverter.UNIT_KG
                }

                val xAxisLabel = ArrayList<String?>()

                for (i in valueList.indices) {
                    var value: BarEntry? = null
                    if (defaultUnit == UnitConverter.UNIT_LBS) {
                        value = BarEntry(
                            i.toFloat(),
                            UnitConverter.KgtoLbs(valueList.get(i)!!.y.toFloat())
                        )
                    } else {
                        value = BarEntry(i.toFloat(), valueList.get(i)!!.y.toFloat())
                    }
                    xAxisLabel.add(valueList.get(i)!!.x.toInt().toString())
                    yBarVals.add(value)
                }
                mBarGraph!!.chart!!.setVisibility(View.VISIBLE)
                mGraphZoomSelector!!.setVisibility(View.GONE)
                mDateGraph!!.chart!!.setVisibility(View.GONE)
                mBarGraph!!.chart!!.setDescription(desc)
                mBarGraph!!.draw(yBarVals, xAxisLabel)
            } else if (lDAOFunction == DAOStatic.NBSERIE_FCT) {
                for (i in valueList.indices) {
                    val value =
                        Entry(valueList.get(i)!!.x.toFloat(), valueList.get(i)!!.y.toFloat())
                    yVals.add(value)
                }
                mBarGraph!!.chart!!.setVisibility(View.GONE)
                mGraphZoomSelector!!.setVisibility(View.VISIBLE)
                mDateGraph!!.chart!!.setVisibility(View.VISIBLE)
                mDateGraph!!.chart!!.setDescription(desc)
                mDateGraph!!.draw(yVals)
            }
        }

        val layoutParams = mLineChart!!.getLayoutParams()
        if (mLineChart!!.getHeight() > mLineChart!!.getWidth()) layoutParams.height =
            mLineChart!!.getWidth()
        mLineChart!!.setLayoutParams(layoutParams)

        val layoutParamsBar = mBarChart!!.getLayoutParams()
        if (mBarChart!!.getHeight() > mBarChart!!.getWidth()) layoutParamsBar.height =
            mBarChart!!.getWidth()
        mBarChart!!.setLayoutParams(layoutParamsBar)
    }

    val fragmentId: Int
        get() = requireArguments().getInt("id", 0)

    private fun refreshData() {
        //View fragmentView = getView();

        if (mFragmentView != null) {
            if (this.profil != null) {
                //functionList.setOnItemSelectedListener(onItemSelectedList);
                if (mAdapterMachine == null) {
                    mMachinesArray = dB!!.allMachinesStrList
                    //Data are refreshed on show
                    mAdapterMachine = ArrayAdapter<String?>(
                        requireContext(), android.R.layout.simple_spinner_item,
                        mMachinesArray
                    )
                    mAdapterMachine!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    machineList!!.setAdapter(mAdapterMachine)
                } else {
                    /* Initialisation des machines */
                    if (mMachinesArray == null) mMachinesArray = dB!!.allMachinesStrList
                    else {
                        mMachinesArray!!.clear()
                        mMachinesArray!!.addAll(dB!!.allMachinesStrList)
                        mAdapterMachine!!.notifyDataSetChanged()
                        dB!!.closeCursor()
                    }
                }

                val position = mAdapterMachine!!.getPosition(this.fontesMachine)
                if (position != -1) {
                    if (machineList!!.getSelectedItemPosition() != position) {
                        machineList!!.setSelection(position) // Refresh drawing
                    } else {
                        drawGraph()
                    }
                } else {
                    mLineChart!!.clear()
                }
            }
        }
    }

    private val adapterMachine: ArrayAdapter<String?>?
        get() {
            var a: ArrayAdapter<String?>
            mMachinesArray =
                ArrayList<String?>(0) //Data are refreshed on show //mDbFonte.getAllMachinesStrList(getProfil());
            // lMachinesArray = prepend(lMachinesArray, "All");
            mAdapterMachine = ArrayAdapter<String?>(
                requireContext(), android.R.layout.simple_spinner_item,
                mMachinesArray
            )
            mAdapterMachine!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            machineList!!.setAdapter(mAdapterMachine)
            return mAdapterMachine
        }

    private val profil: Profile?
        get() = mainActivity!!.currentProfile

    private val fontesMachine: String
        get() = this.mainActivity!!.currentMachine

    override fun onHiddenChanged(hidden: Boolean) {
        //machineList
        if (!hidden) refreshData()
    }

    fun saveSharedParams() {
        val sharedPref = this.mainActivity!!.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("FunctionListPosition", functionList!!.getSelectedItemPosition())
        editor.apply()
    }

    fun getSharedParams(paramName: String?): String {
        val sharedPref = this.mainActivity!!.getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString(paramName, "")!!
    }

    val functionListPositionParams: Int
        get() {
            val sharedPref =
                this.mainActivity!!.getPreferences(Context.MODE_PRIVATE)
            return sharedPref.getInt("FunctionListPosition", 0)
        }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): FonteGraphFragment {
            val f = FonteGraphFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
