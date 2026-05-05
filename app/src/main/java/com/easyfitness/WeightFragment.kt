package com.easyfitness

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.DAOWeight
import com.easyfitness.DAO.Profile
//import com.easyfitness.DAO.Profile.gender
//import com.easyfitness.DAO.Profile.size
import com.easyfitness.DAO.bodymeasures.BodyMeasure
import com.easyfitness.DAO.bodymeasures.BodyPart
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure
import com.easyfitness.DAO.bodymeasures.DAOBodyPart
import com.easyfitness.bodymeasures.BodyPartDetailsFragment.Companion.newInstance
import com.easyfitness.graph.MiniDateGraph
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.EditableInputView.EditableInputView
import com.easyfitness.utils.EditableInputView.EditableInputView.OnTextChangedListener
import com.easyfitness.utils.EditableInputView.EditableInputViewWithDate
import com.easyfitness.utils.Gender
import com.fitworkoutfast.MainActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.onurkaganaldemir.ktoastlib.KToast
import java.lang.Long.parseLong

class WeightFragment : Fragment() {
    var mActivity: MainActivity? = null
    private var weightEdit: EditableInputViewWithDate? = null
    private var fatEdit: EditableInputView? = null
    private var musclesEdit: EditableInputView? = null
    private var waterEdit: EditableInputView? = null
    private var imcText: TextView? = null
    private var imcRank: TextView? = null
    private var ffmiText: TextView? = null
    private var ffmiRank: TextView? = null
    private var rfmText: TextView? = null
    private var rfmRank: TextView? = null

    private var mWeightLineChart: LineChart? = null
    private var mFatLineChart: LineChart? = null
    private var mMusclesLineChart: LineChart? = null
    private var mWaterLineChart: LineChart? = null


    private var mWeightDb: DAOWeight? = null
    private var mDbBodyMeasure: DAOBodyMeasure? = null
    private var mDbBodyPart: DAOBodyPart? = null
    private val mDb: DAOProfil? = null

    private val showDetailsFragment = View.OnClickListener { v: View? ->
        var bodyPartID = BodyPartExtensions.WEIGHT
        val viewId = v!!.getId()
        if (viewId == R.id.weightDetailsButton) {
            bodyPartID = BodyPartExtensions.WEIGHT
        } else if (viewId == R.id.fatDetailsButton) {
            bodyPartID = BodyPartExtensions.FAT
        } else if (viewId == R.id.musclesDetailsButton) {
            bodyPartID = BodyPartExtensions.MUSCLES
        } else if (viewId == R.id.waterDetailsButton) {
            bodyPartID = BodyPartExtensions.WATER
        }

        val fragment = newInstance(bodyPartID.toLong(), false)
        val transaction = requireActivity().getSupportFragmentManager().beginTransaction()
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS)
        transaction.addToBackStack(null)

        // Commit the transaction
        transaction.commit()
    }
    private val itemClickDeleteRecord =
        BtnClickListener { idToDelete: Long -> this.showDeleteDialog(idToDelete) }
    private val itemOnItemSelectedChange: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                refreshData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    private val itemOnTextChange = OnTextChangedListener { view: EditableInputView? ->
        val v = view as EditableInputViewWithDate?
        //save values to databases
        try {
            val viewId = requireView().getId()
            if (viewId == R.id.weightInput) {
                // push value to database
                val weightValue = v!!.getText().toFloat()
                mDbBodyMeasure!!.addBodyMeasure(
                    v.getDate(),
                    BodyPartExtensions.WEIGHT.toLong(),
                    weightValue,
                    parseLong(this.profile?.id.toString())
                )
            } else if (viewId == R.id.fatInput) {
                val fatValue = v!!.getText().toFloat()
                mDbBodyMeasure!!.addBodyMeasure(
                    v.getDate(),
                    BodyPartExtensions.FAT.toLong(),
                    fatValue,
                    this.profile!!.id
                )
            } else if (viewId == R.id.musclesInput) {
                val musclesValue = v!!.getText().toFloat()
                mDbBodyMeasure!!.addBodyMeasure(
                    v.getDate(),
                    BodyPartExtensions.MUSCLES.toLong(),
                    musclesValue,
                    this.profile!!.id
                )
            } else if (viewId == R.id.waterInput) {
                val waterValue = v!!.getText().toFloat()
                mDbBodyMeasure!!.addBodyMeasure(
                    v.getDate(),
                    BodyPartExtensions.WATER.toLong(),
                    waterValue,
                    this.profile!!.id
                )
            }
        } catch (e: NumberFormatException) {
            // Nothing to be done
        }
        // update graph and table
        refreshData()
    }
    private val showHelp = View.OnClickListener { v: View? ->
        val viewId = v!!.getId()
        if (viewId == R.id.imcHelp) {
            SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.BMI_dialog_title)
                .setContentText(getString(R.string.BMI_formula))
                .setConfirmText(getResources().getText(R.string.global_ok).toString())
                .showCancelButton(true)
                .show()
        } else if (viewId == R.id.ffmiHelp) {
            SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.FFMI_dialog_title)
                .setContentText(getString(R.string.FFMI_formula))
                .setConfirmText(getResources().getText(R.string.global_ok).toString())
                .showCancelButton(true)
                .show()
        } else if (viewId == R.id.rfmHelp) {
            SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(R.string.RFM_dialog_title)
                .setContentText(
                    getString(R.string.RFM_female_formula) +
                            getString(R.string.RFM_male_formula)
                )
                .setConfirmText(getResources().getText(R.string.global_ok).toString())
                .showCancelButton(true)
                .show()
        }
    }
    private var mWeightGraph: MiniDateGraph? = null
    private var mFatGraph: MiniDateGraph? = null
    private var mMusclesGraph: MiniDateGraph? = null
    private var mWaterGraph: MiniDateGraph? = null
    private var weightBobyPart: BodyPart? = null
    private var fatBobyPart: BodyPart? = null
    private var musclesBobyPart: BodyPart? = null
    private var waterBobyPart: BodyPart? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.tab_weight, container, false)

        /* Views Initialisation */
        weightEdit = view.findViewById<EditableInputViewWithDate?>(R.id.weightInput)
        fatEdit = view.findViewById<EditableInputView?>(R.id.fatInput)
        musclesEdit = view.findViewById<EditableInputView?>(R.id.musclesInput)
        waterEdit = view.findViewById<EditableInputView?>(R.id.waterInput)
        val weightDetailsButton = view.findViewById<Button>(R.id.weightDetailsButton)
        val fatDetailsButton = view.findViewById<Button>(R.id.fatDetailsButton)
        val musclesDetailsButton = view.findViewById<Button>(R.id.musclesDetailsButton)
        val waterDetailsButton = view.findViewById<Button>(R.id.waterDetailsButton)
        imcText = view.findViewById<TextView?>(R.id.imcValue)
        imcRank = view.findViewById<TextView?>(R.id.imcViewText)
        ffmiText = view.findViewById<TextView?>(R.id.ffmiValue)
        ffmiRank = view.findViewById<TextView?>(R.id.ffmiViewText)
        rfmText = view.findViewById<TextView?>(R.id.rfmValue)
        rfmRank = view.findViewById<TextView?>(R.id.rfmViewText)

        val ffmiHelpButton = view.findViewById<ImageButton>(R.id.ffmiHelp)
        val imcHelpButton = view.findViewById<ImageButton>(R.id.imcHelp)
        val rfmHelpButton = view.findViewById<ImageButton>(R.id.rfmHelp)

        /* Initialisation des evenements */
        weightEdit!!.setOnTextChangeListener(itemOnTextChange)
        fatEdit!!.setOnTextChangeListener(itemOnTextChange)
        musclesEdit!!.setOnTextChangeListener(itemOnTextChange)
        waterEdit!!.setOnTextChangeListener(itemOnTextChange)
        imcHelpButton.setOnClickListener(showHelp)
        ffmiHelpButton.setOnClickListener(showHelp)
        rfmHelpButton.setOnClickListener(showHelp)
        weightDetailsButton.setOnClickListener(showDetailsFragment)
        fatDetailsButton.setOnClickListener(showDetailsFragment)
        musclesDetailsButton.setOnClickListener(showDetailsFragment)
        waterDetailsButton.setOnClickListener(showDetailsFragment)

        mWeightDb = DAOWeight(view.getContext())
        mDbBodyPart = DAOBodyPart(view.getContext())
        mDbBodyMeasure = DAOBodyMeasure(view.getContext())

        mWeightLineChart = view.findViewById<LineChart>(R.id.weightGraph)
        mWeightGraph = MiniDateGraph(getContext(), mWeightLineChart, "")
        weightBobyPart =
            mDbBodyPart!!.getBodyPartfromBodyPartKey(BodyPartExtensions.WEIGHT.toLong())

        mFatLineChart = view.findViewById<LineChart>(R.id.fatGraph)
        mFatGraph = MiniDateGraph(getContext(), mFatLineChart, "")
        fatBobyPart = mDbBodyPart!!.getBodyPartfromBodyPartKey(BodyPartExtensions.FAT.toLong())

        mMusclesLineChart = view.findViewById<LineChart>(R.id.musclesGraph)
        mMusclesGraph = MiniDateGraph(getContext(), mMusclesLineChart, "")
        musclesBobyPart =
            mDbBodyPart!!.getBodyPartfromBodyPartKey(BodyPartExtensions.MUSCLES.toLong())

        mWaterLineChart = view.findViewById<LineChart>(R.id.waterGraph)
        mWaterGraph = MiniDateGraph(getContext(), mWaterLineChart, "")
        waterBobyPart = mDbBodyPart!!.getBodyPartfromBodyPartKey(BodyPartExtensions.WATER.toLong())

        return view
    }


    private fun DrawGraph() {
        if (getView() == null) return
        requireView().post(Runnable {
            if (weightBobyPart == null) return@Runnable
            val valueList = mDbBodyMeasure!!.getBodyPartMeasuresListTop4(
                weightBobyPart!!.getId(),
                this.profile
            )

            // Recupere les enregistrements
            if (valueList.size < 1) {
                mWeightLineChart!!.clear()
                return@Runnable
            }

            val yVals = ArrayList<Entry?>()
            if (valueList.size > 0) {
                for (i in valueList.indices.reversed()) {
                    val value = Entry(
                        DateConverter.nbDays(
                            valueList.get(i)!!.getDate().getTime().toDouble()
                        ).toFloat(), valueList.get(i)!!.getBodyMeasure()
                    )
                    yVals.add(value)
                    /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
        else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
            minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mWeightGraph!!.draw(yVals)
            }
        })

        requireView().post(Runnable {
            if (fatBobyPart == null) return@Runnable
            val valueList = mDbBodyMeasure!!.getBodyPartMeasuresListTop4(
                fatBobyPart!!.getId(),
                this.profile
            )

            // Recupere les enregistrements
            if (valueList.size < 1) {
                mFatLineChart!!.clear()
                return@Runnable
            }

            val yVals = ArrayList<Entry?>()
            if (valueList.size > 0) {
                for (i in valueList.indices.reversed()) {
                    val value = Entry(
                        DateConverter.nbDays(
                            valueList.get(i)!!.getDate().getTime().toDouble()
                        ).toFloat(), valueList.get(i)!!.getBodyMeasure()
                    )
                    yVals.add(value)
                    /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mFatGraph!!.draw(yVals)
            }
        })
        requireView().post(Runnable {
            if (musclesBobyPart == null) return@Runnable
            val valueList = mDbBodyMeasure!!.getBodyPartMeasuresListTop4(
                musclesBobyPart!!.getId(),
                this.profile
            )

            // Recupere les enregistrements
            if (valueList.size < 1) {
                mMusclesLineChart!!.clear()
                return@Runnable
            }

            val yVals = ArrayList<Entry?>()
            if (valueList.size > 0) {
                for (i in valueList.indices.reversed()) {
                    val value = Entry(
                        DateConverter.nbDays(
                            valueList.get(i)!!.getDate().getTime().toDouble()
                        ).toFloat(), valueList.get(i)!!.getBodyMeasure()
                    )
                    yVals.add(value)
                    /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mMusclesGraph!!.draw(yVals)
            }
        })

        requireView().post(Runnable {
            if (waterBobyPart == null) return@Runnable
            val valueList = mDbBodyMeasure!!.getBodyPartMeasuresListTop4(
                waterBobyPart!!.getId(),
                this.profile
            )

            // Recupere les enregistrements
            if (valueList.size < 1) {
                mWaterLineChart!!.clear()
                return@Runnable
            }

            val yVals = ArrayList<Entry?>()
            if (valueList.size > 0) {
                for (i in valueList.indices.reversed()) {
                    val value = Entry(
                        DateConverter.nbDays(
                            valueList.get(i)!!.getDate().getTime().toDouble()
                        ).toFloat(), valueList.get(i)!!.getBodyMeasure()
                    )
                    yVals.add(value)
                    /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mWaterGraph!!.draw(yVals)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        refreshData()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.mActivity = activity as MainActivity
    }

    val name: String?
        get() = requireArguments().getString("name")

    /**
     * @param weight in kg
     * @param size   in cm
     * @return
     */
    private fun calculateImc(weight: Float, size: Int): Float {
        var imc = 0f

        if (size == 0) return 0f

        imc = (weight / (size / 100.0 * size / 100.0)).toFloat()

        return imc
    }

    /**
     * @param imc
     * @return text associated with imc value
     */
    private fun getImcText(imc: Float): String {
        if (imc < 18.5) {
            return getString(R.string.underweight)
        } else if (imc < 25) {
            return getString(R.string.normal)
        } else if (imc < 30) {
            return getString(R.string.overweight)
        } else {
            return getString(R.string.obese)
        }
    }

    private fun calculateRfm(waistCirc: Float, sex: Int, size: Int): Float {
        val rfm = 0f

        if (waistCirc == 0f) return 0f

        return 0f
    }

    /**
     * @param rfm index
     * @return text associated with Rfm value
     */
    private fun getRfmText(rfm: Float): String {
        if (rfm < 18.5) {
            return "underweight"
        } else if (rfm < 25) {
            return "normal"
        } else if (rfm < 30) {
            return "overweight"
        } else {
            return "obese"
        }
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private fun calculateFfmi(weight: Float, size: Int, bodyFat: Float): Double {
        var ffmi = 0.0

        if (bodyFat == 0f) return 0.0

        ffmi = weight * (1 - (bodyFat / 100)) / (size / 100.0 * size / 100.0)

        return ffmi
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private fun calculateNormalizedFfmi(weight: Float, size: Int, bodyFat: Float): Double {
        var ffmi = 0.0

        if (bodyFat == 0f) return 0.0

        ffmi = weight * (1 - (bodyFat / 100)) / (size * size) + 6.1 * (1.8 - size)

        return ffmi
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally      */
    private fun getFfmiTextForMen(ffmi: Double): String {
        if (ffmi < 17) {
            return "below average"
        } else if (ffmi < 19) {
            return "average"
        } else if (ffmi < 21) {
            return "above average"
        } else if (ffmi < 23) {
            return "excellent"
        } else if (ffmi < 25) {
            return "superior"
        } else if (ffmi < 27) {
            return "suspicious"
        } else {
            return "very suspicious"
        }
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally      */
    private fun getFfmiTextForWomen(ffmi: Double): String {
        if (ffmi < 14) {
            return "below average"
        } else if (ffmi < 16) {
            return "average"
        } else if (ffmi < 18) {
            return "above average"
        } else if (ffmi < 20) {
            return "excellent"
        } else if (ffmi < 22) {
            return "superior"
        } else if (ffmi < 24) {
            return "suspicious"
        } else {
            return "very suspicious"
        }
    }

    private fun refreshData() {
        val fragmentView = getView()
        if (fragmentView != null) {
            if (this.profile != null) {
                var lastWeightValue: BodyMeasure? = null
                var lastWaterValue: BodyMeasure? = null
                var lastFatValue: BodyMeasure? = null
                var lastMusclesValue: BodyMeasure? = null

                if (this.profile != null) {
                    lastWeightValue = mDbBodyMeasure!!.getLastBodyMeasures(
                        weightBobyPart!!.getId(),
                        this.profile
                    )
                    lastWaterValue = mDbBodyMeasure!!.getLastBodyMeasures(
                        waterBobyPart!!.getId(),
                        this.profile
                    )
                    lastFatValue = mDbBodyMeasure!!.getLastBodyMeasures(
                        fatBobyPart!!.getId(),
                        this.profile
                    )
                    lastMusclesValue = mDbBodyMeasure!!.getLastBodyMeasures(
                        musclesBobyPart!!.getId(),
                        this.profile
                    )
                }

                if (lastWeightValue != null) {
                    weightEdit!!.setText(lastWeightValue.getBodyMeasure().toString())
                    // update IMC
                    val size = this.profile!!.size
                    if (size == 0) {
                        imcText!!.setText("-")
                        imcRank!!.setText(R.string.no_size_available)
                        ffmiText!!.setText("-")
                        ffmiRank!!.setText(R.string.no_size_available)
                    } else {
                        val imcValue = calculateImc(lastWeightValue.getBodyMeasure(), size)
                        imcText!!.setText(String.format("%.1f", imcValue))
                        imcRank!!.setText(getImcText(imcValue))
                        if (lastFatValue != null) {
                            val ffmiValue = calculateFfmi(
                                lastWeightValue.getBodyMeasure(),
                                size,
                                lastFatValue.getBodyMeasure()
                            )
                            ffmiText!!.setText(String.format("%.1f", ffmiValue))
                            if (this.profile!!.gender == Gender.FEMALE) ffmiRank!!.setText(
                                getFfmiTextForWomen(ffmiValue)
                            )
                            else if (this.profile!!.gender == Gender.MALE) ffmiRank!!.setText(
                                getFfmiTextForMen(ffmiValue)
                            )
                            else if (this.profile!!.gender == Gender.OTHER) ffmiRank!!.setText(
                                getFfmiTextForMen(ffmiValue)
                            )
                            else ffmiRank!!.setText("no gender defined")
                        } else {
                            ffmiText!!.setText("-")
                            ffmiRank!!.setText(R.string.no_fat_available)
                        }
                    }
                } else {
                    weightEdit!!.setText("-")
                    imcText!!.setText("-")
                    imcRank!!.setText(R.string.no_weight_available)
                    ffmiText!!.setText("-")
                    ffmiRank!!.setText(R.string.no_weight_available)
                }

                if (lastWaterValue != null) waterEdit!!.setText(
                    lastWaterValue.getBodyMeasure().toString()
                )
                else waterEdit!!.setText("-")

                if (lastFatValue != null) fatEdit!!.setText(
                    lastFatValue.getBodyMeasure().toString()
                )
                else fatEdit!!.setText("-")

                if (lastMusclesValue != null) musclesEdit!!.setText(
                    lastMusclesValue.getBodyMeasure().toString()
                )
                else musclesEdit!!.setText("-")


                DrawGraph()
            }
        }
    }

    private fun showDeleteDialog(idToDelete: Long) {
        SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                mDbBodyMeasure!!.deleteMeasure(idToDelete)
                refreshData()
                // Info
                KToast.infoToast(
                    getActivity(),
                    getResources().getText(R.string.removedid).toString(),
                    Gravity.BOTTOM,
                    KToast.LENGTH_LONG
                )
                sDialog!!.dismissWithAnimation()
            })
            .show()
    }

    private val profile: Profile?
        get() = (getActivity() as MainActivity).currentProfile

    val fragment: Fragment
        get() = this

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): WeightFragment {
            val f = WeightFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
