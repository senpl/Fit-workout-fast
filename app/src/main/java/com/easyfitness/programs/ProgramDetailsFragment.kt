package com.easyfitness.programs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Program
import com.easyfitness.R

class ProgramDetailsFragment : Fragment() {
    private lateinit var machineName: EditText

    private var machineNameArg: String = ""
    private var machineIdArg: Long = 0
    private var machineProfilIdArg: Long = 0

    private var pager: ProgramDetailsPager? = null
    private var mDbMachine: DAOProgram? = null
    private var mDbRecord: DAORecord? = null
    private var mMachine: Program? = null
    private var fragmentView: View? = null

    var toBeSaved = false
    private var watcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            requestForSave()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.program_details, container, false)
        fragmentView = view

        // Initialisation de l'historique
        mDbMachine = DAOProgram(requireContext())
        mDbRecord = DAORecord(context)
        machineName = view.findViewById(R.id.programName)
        val args = this.arguments
        machineIdArg = args!!.getLong("programID")
        machineProfilIdArg = args.getLong("programProfile")
        mMachine = mDbMachine!!.getRecord(machineIdArg)
        if(mMachine!=null)
        machineNameArg = mMachine!!.programName!!

        if (machineNameArg == "") {
            requestForSave()
        }
        machineName.setText(machineNameArg)
//        exerciseTypeSelectorLayout!!.visibility = View.GONE
//        if (mMachine.type == DAOMachine.TYPE_CARDIO) {
//            cardioSelector!!.setBackgroundColor(getColor(requireContext().resources, R.color.record_background_odd, requireContext().theme))
//            bodybuildingSelector!!.visibility = View.GONE
//            bodybuildingSelector!!.setBackgroundColor(getColor(requireContext().resources, R.color.background, requireContext().theme))
//            selectedType = mMachine.type
//            view.findViewById<View>(R.id.machine_muscles).visibility = View.GONE
//            view.findViewById<View>(R.id.machine_muscles_textview).visibility = View.GONE
//        } else {
//            cardioSelector!!.setBackgroundColor(getColor(requireContext().resources, R.color.background, requireContext().theme))
//            cardioSelector!!.visibility = View.GONE
//            bodybuildingSelector!!.setBackgroundColor(getColor(requireContext().resources, R.color.record_background_odd, requireContext().theme))
//            selectedType = mMachine.type
//        }
//        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                // Ensure you call it only once :
//                fragmentView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                // Here you can get the size :)
//                if (mCurrentPhotoPath != null && mCurrentPhotoPath!!.isNotEmpty()) {
//                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath)
//                } else {
//                    if (mMachine.type == DAOMachine.TYPE_FONTE) {
//                        imgUtil!!.view.setImageDrawable(activity!!.getDrawable(R.drawable.ic_gym_bench_50dp))
//                    } else if( mMachine.type == DAOMachine.TYPE_STATIC) {
//                        imgUtil!!.view.setImageDrawable(activity!!.getDrawable(R.drawable.ic_static))
//                    } else {
//                        imgUtil!!.view.setImageDrawable(activity!!.getDrawable(R.drawable.ic_training_white_50dp))
//                    }
//                    machinePhoto!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
//                }
//                machinePhoto!!.maxHeight = (requireView().height * 0.2).toInt() // Taille initiale
//            }
//        })
        machineName.addTextChangedListener(watcher)
//        if (parentFragment is ProgramDetailsPager) {
//            pager = parentFragment as ProgramDetailsPager?
//        }
        return view
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            ImageUtil.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
//                mCurrentPhotoPath = imgUtil!!.filePath
//                ImageUtil.setPic(machinePhoto, mCurrentPhotoPath)
//                ImageUtil.saveThumb(mCurrentPhotoPath)
//                imgUtil!!.galleryAddPic(this, mCurrentPhotoPath)
//                requestForSave()
//            }
//            ImageUtil.REQUEST_PICK_GALERY_PHOTO -> if (resultCode == Activity.RESULT_OK) {
//                val realPath: String = RealPathUtil.getRealPath(this.context, data!!.data)
//                ImageUtil.setPic(machinePhoto, realPath)
//                ImageUtil.saveThumb(realPath)
//                mCurrentPhotoPath = realPath
//                requestForSave()
//            }
////            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
////                val result = CropImage.getActivityResult(data)
////                if (resultCode == Activity.RESULT_OK) {
////                    val resultUri = result.uri
////                    var realPath: String?
////                    realPath = RealPathUtil.getRealPath(this.context, resultUri)
////                    val sourceFile = File(realPath)
////                    val storageDir: File?
////                    val state = Environment.getExternalStorageState()
////                    if (Environment.MEDIA_MOUNTED != state) {
////                        return
////                    } else {
////                        //We use the FastNFitness directory for saving our .csv file.
////                        storageDir = getExternalStoragePublicDirectory("/FastnFitness/Camera/")
////                        if (!storageDir.exists()) {
////                            storageDir.mkdirs()
////                        }
////                    }
////                    val destinationFile: File?
////                    try {
////                        destinationFile = imgUtil!!.moveFile(sourceFile, storageDir)
////                        Timber.tag("Moving").v("Moving file successful.")
////                        realPath = destinationFile.path
////                    } catch (e: IOException) {
////                        e.printStackTrace()
////                        Timber.tag("Moving").v("Moving file failed.")
////                    }
////                    ImageUtil.setPic(machinePhoto, realPath)
////                    ImageUtil.saveThumb(realPath)
////                    mCurrentPhotoPath = realPath
////                    requestForSave()
////                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
////                    result.error
////                }
////            }
//        }
//    }

    val `this`: ProgramDetailsFragment
        get() = this

    private fun requestForSave() {
        toBeSaved = true // setting state
        if (pager != null) pager!!.requestForSave()
    }

    fun programSaved() {
        toBeSaved = false
    }

    val machine: Program?
        get() {
            val m = mMachine
            if (m != null) {
                m.programName = machineName.text.toString()
            }
            //        m.setProfil(selectedType);
            return m
        }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): ProgramDetailsFragment {
            val f = ProgramDetailsFragment()
            val args = Bundle()
            args.putLong("programID", machineId)
            args.putLong("programProfile", machineProfile)
            f.arguments = args
            return f
        }
    }
}
