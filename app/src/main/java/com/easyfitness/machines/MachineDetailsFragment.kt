package com.easyfitness.machines

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Machine
import com.easyfitness.R
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.ImageUtil.OnDeleteImageListener
import com.easyfitness.utils.Keyboard
import com.easyfitness.utils.RealPathUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularimageview.CircularImageView
import java.io.File

class MachineDetailsFragment : Fragment() {
    val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: Int = 101

    // http://labs.makemachine.net/2010/03/android-multi-selection-dialogs/
    //protected CharSequence[] _muscles = {"Biceps", "Triceps", "Epaules", "Pectoraux", "Dorseaux", "Quadriceps", "Adducteurs"};
    protected var _musclesArray: MutableList<String?> = ArrayList<String?>()
    protected lateinit var _selections: BooleanArray
    var typeList: Spinner? = null /*Halteres, Machines avec Poids, Cardio*/
    var musclesList: TextView? = null
    var machineName: EditText? = null
    var machineDescription: EditText? = null
    var machinePhoto: ImageView? = null
    var machineAction: FloatingActionButton? = null
    var machinePhotoLayout: LinearLayout? = null
    var selectedType: Int = DAOMachine.TYPE_STRENGTH
    var machineNameArg: String? = null
    var machineIdArg: Long = 0
    var machineProfilIdArg: Long = 0
    var isImageFitToScreen: Boolean = false
    var pager: ExerciseDetailsPager? = null
    var selectMuscleList: ArrayList<*> = ArrayList<Any?>()
    var mDbMachine: DAOMachine? = null
    var mDbRecord: DAORecord? = null
    var mMachine: Machine? = null

    private var fragmentView: View? = null

    private var imgUtil: ImageUtil? = null
    private var isCreateMuscleDialogActive = false
    private var mCurrentPhotoPath: String? = null
    private var toBeSaved = false
    var watcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(
            s: CharSequence?, start: Int,
            before: Int, count: Int
        ) {
            requestForSave()
        }

        override fun beforeTextChanged(
            s: CharSequence?, start: Int, count: Int,
            after: Int
        ) {
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }
    private val onClickMusclesList = View.OnClickListener { v: View? -> CreateMuscleDialog() }
    private val onLongClickMachinePhoto =
        OnLongClickListener { v: View? -> CreatePhotoSourceDialog() }
    private val onClickMachinePhoto = View.OnClickListener { v: View? -> CreatePhotoSourceDialog() }
    private val onFocusMachineList = OnFocusChangeListener { arg0: View?, arg1: Boolean ->
        if (arg1) {
            CreateMuscleDialog()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.machine_details, container, false)
        fragmentView = view

        // Initialisation de l'historique
        mDbMachine = DAOMachine(view.getContext())
        mDbRecord = DAORecord(view.getContext())

        machineName = view.findViewById<EditText?>(R.id.machine_name)
        machineDescription = view.findViewById<EditText?>(R.id.machine_description)
        musclesList = view.findViewById<TextView?>(R.id.machine_muscles)
        machinePhoto = view.findViewById<ImageView?>(R.id.machine_photo)

        machinePhotoLayout = view.findViewById<LinearLayout?>(R.id.machine_photo_layout)
        machineAction = view.findViewById<FloatingActionButton?>(R.id.actionCamera)

        imgUtil = ImageUtil(machinePhoto)

        buildMusclesTable()

        val args = this.getArguments()

        machineIdArg = args!!.getLong("machineID")
        machineProfilIdArg = args.getLong("machineProfile")

        // set events
        musclesList!!.setOnClickListener(onClickMusclesList)
        musclesList!!.setOnFocusChangeListener(onFocusMachineList)
        machinePhoto!!.setOnLongClickListener(onLongClickMachinePhoto)
        machinePhoto!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (isImageFitToScreen) {
                isImageFitToScreen = false
                machinePhoto!!.setLayoutParams(
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                machinePhoto!!.setAdjustViewBounds(true)
                machinePhoto!!.setMaxHeight((requireView().getHeight() * 0.2).toInt())
                machinePhoto!!.setScaleType(ImageView.ScaleType.CENTER_CROP)
            } else {
                if (mCurrentPhotoPath != null && !mCurrentPhotoPath!!.isEmpty()) {
                    val f = File(mCurrentPhotoPath)
                    if (f.exists()) {
                        isImageFitToScreen = true

                        // Get the dimensions of the bitmap
                        val bmOptions = BitmapFactory.Options()
                        bmOptions.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
                        val photoW = bmOptions.outWidth.toFloat()
                        val photoH = bmOptions.outHeight.toFloat()

                        // Determine how much to scale down the image
                        val scaleFactor =
                            (photoW / (machinePhoto!!.getWidth())).toInt() //Math.min(photoW/targetW, photoH/targetH);machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        machinePhoto!!.setAdjustViewBounds(true)
                        machinePhoto!!.setMaxHeight((photoH / scaleFactor).toInt())
                        machinePhoto!!.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    }
                }
            }
        })
        machineAction!!.setOnClickListener(onClickMachinePhoto)

        mMachine = mDbMachine!!.getMachine(machineIdArg)
        machineNameArg = mMachine!!.name

        if (machineNameArg == "") {
            requestForSave()
        }

        machineName!!.setText(machineNameArg)
        machineDescription!!.setText(mMachine!!.description)
        musclesList!!.setText(this.getInputFromDBString(mMachine!!.bodyParts.toString()))
        mCurrentPhotoPath = mMachine!!.picture

        if (mMachine!!.type == DAOMachine.TYPE_CARDIO) {
            selectedType = mMachine!!.type
            view.findViewById<View?>(R.id.machine_muscles).setVisibility(View.GONE)
            view.findViewById<View?>(R.id.machine_muscles_textview).setVisibility(View.GONE)
        } else {
            selectedType = mMachine!!.type
            view.findViewById<View?>(R.id.machine_muscles).setVisibility(View.VISIBLE)
            view.findViewById<View?>(R.id.machine_muscles_textview).setVisibility(View.VISIBLE)
        }

        view.getViewTreeObserver()
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Ensure you call it only once :
                    fragmentView!!.getViewTreeObserver().removeOnGlobalLayoutListener(this)

                    // Here you can get the size :)
                    if (mCurrentPhotoPath != null && !mCurrentPhotoPath!!.isEmpty()) {
                        ImageUtil.setPic(machinePhoto as CircularImageView?, mCurrentPhotoPath)
                    } else {
                        if (mMachine!!.type == DAOMachine.TYPE_STRENGTH) {
                            imgUtil!!.view!!.setImageDrawable(
                                requireActivity().getResources()
                                    .getDrawable(R.drawable.ic_gym_bench_50dp)
                            )
                        } else if (mMachine!!.type == DAOMachine.TYPE_STATIC) {
                            imgUtil!!.view!!.setImageDrawable(
                                requireActivity().getResources().getDrawable(R.drawable.ic_static)
                            )
                        } else {
                            imgUtil!!.view!!.setImageDrawable(
                                requireActivity().getResources()
                                    .getDrawable(R.drawable.ic_training_white_50dp)
                            )
                        }
                        machinePhoto!!.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
                    }
                    machinePhoto!!.setMaxHeight((requireView().getHeight() * 0.2).toInt())
                }
            })

        machineName!!.addTextChangedListener(watcher)
        machineDescription!!.addTextChangedListener(watcher)
        musclesList!!.addTextChangedListener(watcher)

        imgUtil!!.setOnDeleteImageListener(OnDeleteImageListener { imgUtil: ImageUtil? ->
            if (mMachine!!.type == DAOMachine.TYPE_STRENGTH) {
                imgUtil!!.view!!.setImageDrawable(
                    requireActivity().getResources().getDrawable(R.drawable.ic_gym_bench_50dp)
                )
            } else if (mMachine!!.type == DAOMachine.TYPE_STATIC) {
                imgUtil!!.view!!.setImageDrawable(
                    requireActivity().getResources().getDrawable(R.drawable.ic_static)
                )
            } else {
                imgUtil!!.view!!.setImageDrawable(
                    requireActivity().getResources().getDrawable(R.drawable.ic_training_white_50dp)
                )
            }
            machinePhoto!!.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
            mCurrentPhotoPath = null
            requestForSave()
        })

        if (getParentFragment() is ExerciseDetailsPager) {
            pager = getParentFragment() as ExerciseDetailsPager
        }

        return view
    }

    private fun CreateMuscleDialog(): Boolean {
        if (isCreateMuscleDialogActive) return true // Si la boite de dialog est deja active, alors n'en cree pas une deuxieme.


        isCreateMuscleDialogActive = true

        Keyboard.hide(requireContext(), requireView())

        val newMuscleBuilder = AlertDialog.Builder(this.getActivity())

        newMuscleBuilder.setTitle(this.getResources().getString(R.string.selectMuscles))
        newMuscleBuilder.setMultiChoiceItems(
            _musclesArray.toTypedArray<CharSequence?>(),
            _selections,
            OnMultiChoiceClickListener { arg0: DialogInterface?, arg1: Int, arg2: Boolean ->
                if (arg2) {
                    // If user select a item then add it in selected items
//                    selectMuscleList.add(arg1)
                } else if (selectMuscleList.contains(arg1)) {
                    // if the item is already selected then remove it
//                    selectMuscleList.remove(arg1)
                }
            })

        // Set an EditText view to get user input
        newMuscleBuilder.setPositiveButton(
            getResources().getString(R.string.global_ok),
            DialogInterface.OnClickListener { dialog: DialogInterface?, whichButton: Int ->
                var msg = StringBuilder()
                var i = 0
                var firstSelection = true
                // ( selectMuscleList.size() > 0 ) { // Si on a au moins selectionne un muscle
                i = 0
                while (i < _selections.size) {
                    if (_selections[i] && firstSelection) {
                        msg = StringBuilder(_musclesArray.get(i))
                        firstSelection = false
                    } else if (_selections[i] && !firstSelection) {
                        msg.append(";").append(_musclesArray.get(i))
                    }
                    i++
                }
                //}
                setMuscleText(msg.toString())
                isCreateMuscleDialogActive = false
            })
        newMuscleBuilder.setNegativeButton(
            getResources().getString(R.string.global_cancel),
            DialogInterface.OnClickListener { dialog: DialogInterface?, whichButton: Int ->
                isCreateMuscleDialogActive = false
            })

        newMuscleBuilder.show()

        return true
    }

    private fun CreatePhotoSourceDialog(): Boolean {
        if (imgUtil == null) imgUtil = ImageUtil()

        return imgUtil!!.CreatePhotoSourceDialog(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ImageUtil.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath = imgUtil!!.filePath
//                ImageUtil.setPic(machinePhoto, mCurrentPhotoPath)
//                ImageUtil.saveThumb(mCurrentPhotoPath)
//                imgUtil!!.galleryAddPic(this, mCurrentPhotoPath)
                requestForSave()
            }

            ImageUtil.REQUEST_PICK_GALERY_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val realPath: String?
//                realPath = RealPathUtil.getRealPath(this.requireContext(), data?.getData())

//                ImageUtil.setPic(machinePhoto, realPath)
//                ImageUtil.saveThumb(realPath)
//                mCurrentPhotoPath = realPath
                requestForSave()
            }
        }
    }

    private fun setMuscleText(t: String?) {
        musclesList!!.setText(t)
    }

    val `this`: MachineDetailsFragment
        get() = this

    private fun requestForSave() {
        toBeSaved = true // setting state
        if (pager != null) pager!!.requestForSave()
    }


    private fun buildMusclesTable() {
        _musclesArray.add(requireActivity().getResources().getString(R.string.biceps))
        _musclesArray.add(requireActivity().getResources().getString(R.string.triceps))
        _musclesArray.add(requireActivity().getResources().getString(R.string.pectoraux))
        _musclesArray.add(requireActivity().getResources().getString(R.string.dorseaux))
        _musclesArray.add(requireActivity().getResources().getString(R.string.abdominaux))
        _musclesArray.add(requireActivity().getResources().getString(R.string.quadriceps))
        _musclesArray.add(requireActivity().getResources().getString(R.string.ischio_jambiers))
        _musclesArray.add(requireActivity().getResources().getString(R.string.adducteurs))
        _musclesArray.add(requireActivity().getResources().getString(R.string.mollets))
        _musclesArray.add(requireActivity().getResources().getString(R.string.deltoids))
        _musclesArray.add(requireActivity().getResources().getString(R.string.trapezius))
        _musclesArray.add(requireActivity().getResources().getString(R.string.shoulders))
        _musclesArray.add(requireActivity().getResources().getString(R.string.obliques))

        _selections = BooleanArray(_musclesArray.size)
    }

    /*
     * @return the name of the Muscle depending on the language
     */
    private fun getMuscleNameFromId(id: Int): String? {
        var ret: String? = ""
        try {
            ret = _musclesArray.get(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ret
    }

    /*
     * @return the name of the Muscle depending on the language
     */
    private fun getMuscleIdFromName(pName: String?): Int {
        for (i in _musclesArray.indices) {
            if (_musclesArray.get(i) == pName) return i
        }
        return -1
    }

    /*
     * @return the name of the Muscle depending on the language
     */
    private fun getDBStringFromInput(pInput: String): String {
        val data: Array<String?> =
            pInput.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var output = StringBuilder()

        if (pInput.isEmpty()) return ""

        var i = 0
        if (data.size > 0) {
            output = StringBuilder(getMuscleIdFromName(data[i]).toString())
            i = 1
            while (i < data.size) {
                output.append(";").append(getMuscleIdFromName(data[i]))
                i++
            }
        }

        return output.toString()
    }


    /*
     * @return the name of the Muscle depending on the language
     */
    private fun getInputFromDBString(pDBString: String): String {
        val data: Array<String?> =
            pDBString.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var output = StringBuilder()

        var i = 0

        try {
            if (data.size > 0) {
                if (data[0]!!.isEmpty()) return ""

                if (data[i] != "-1") {
                    output = StringBuilder(getMuscleNameFromId(data[i]!!.toInt()))
                    _selections[data[i]!!.toInt()] = true
                    i = 1
                    while (i < data.size) {
                        if (data[i] != "-1") {
                            output.append(";").append(getMuscleNameFromId(data[i]!!.toInt()))
                            _selections[data[i]!!.toInt()] = true
                        }
                        i++
                    }
                }
            }
        } catch (e: NumberFormatException) {
            output = StringBuilder()
            e.printStackTrace()
        }

        return output.toString()
    }

    fun getCameraPhotoOrientation(context: Context, imageUri: Uri, imagePath: String): Int {
        var rotate = 0
        try {
            context.getContentResolver().notifyChange(imageUri, null)
            val imageFile = File(imagePath)

            val exif = ExifInterface(imageFile.getAbsolutePath())
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }

            //Log.i("RotateImage", "Exif orientation: " + orientation);
            //Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }

    fun toBeSaved(): Boolean {
        return toBeSaved
    }

    fun machineSaved() {
        toBeSaved = false
    }

    val machine: Machine
        get() {
            val m = mMachine!!
            m.name=(machineName!!.getText().toString())
            m.description=(machineDescription!!.getText().toString())
            m.bodyParts=(getDBStringFromInput(this.musclesList!!.getText().toString()))
            m.picture=(mCurrentPhotoPath)
            m.favorite= false
            m.type=(selectedType)
            return m
        }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): MachineDetailsFragment {
            val f = MachineDetailsFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putLong("machineID", machineId)
            args.putLong("machineProfile", machineProfile)
            f.setArguments(args)

            return f
        }
    }
}

