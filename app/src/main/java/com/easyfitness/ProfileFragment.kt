package com.easyfitness

import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener
import com.easyfitness.DAO.DAOProfil
import com.easyfitness.DAO.Profile
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.EditableInputView.EditableInputView
import com.easyfitness.utils.EditableInputView.EditableInputView.CustomerDialogBuilder
import com.easyfitness.utils.EditableInputView.EditableInputView.OnTextChangedListener
import com.easyfitness.utils.Gender
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.ImageUtil.OnDeleteImageListener
import com.easyfitness.utils.RealPathUtil
import com.fitworkoutfast.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikhaellopez.circularimageview.CircularImageView
import com.onurkaganaldemir.ktoastlib.KToast

//import com.theartofdev.edmodo.cropper.CropImage;
class ProfileFragment : Fragment() {
    var sizeEdit: EditableInputView? = null
    var birthdayEdit: EditableInputView? = null
    var nameEdit: EditableInputView? = null
    var genderEdit: EditableInputView? = null
    var roundProfile: CircularImageView? = null
    var photoButton: FloatingActionButton? = null
    var mCurrentPhotoPath: String? = null

    var mActivity: MainActivity? = null
    private var mDb: DAOProfil? = null
    private var mProfile: Profile? = null
    private var imgUtil: ImageUtil? = null
    private val itemOnTextChange =
        OnTextChangedListener { view: EditableInputView? -> this.requestForSave(requireView()) }
    private val onClickMachinePhoto = View.OnClickListener { v: View? -> CreatePhotoSourceDialog() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.profile, container, false)

        sizeEdit = view.findViewById<EditableInputView?>(R.id.size)
        birthdayEdit = view.findViewById<EditableInputView?>(R.id.birthday)
        nameEdit = view.findViewById<EditableInputView?>(R.id.name)
        genderEdit = view.findViewById<EditableInputView?>(R.id.gender)
        roundProfile = view.findViewById<CircularImageView?>(R.id.photo)
        photoButton = view.findViewById<FloatingActionButton?>(R.id.actionCamera)

        sizeEdit!!.setTextSuffix(" cm")

        mDb = DAOProfil(view.getContext())
        mProfile = this.profil

        /* Initialisation des valeurs */
        imgUtil = ImageUtil(roundProfile)

        // ImageView must be set in OnStart. Not in OnCreateView

        /* Initialisation des boutons */
        genderEdit!!.setCustomDialogBuilder(CustomerDialogBuilder { view1: EditableInputView? ->
            val dlg = SweetAlertDialog(view1!!.getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(requireContext().getString(R.string.edit_value))
                .setNeutralText(getString(R.string.maleGender))
                .setCancelText(getString(R.string.femaleGender))
                .setConfirmText(getString(R.string.otherGender))
                .setNeutralClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                    val oldValue = genderEdit!!.getText()
                    if (oldValue != getString(R.string.maleGender)) {
                        genderEdit!!.setText(getString(R.string.maleGender))
                        requestForSave(genderEdit!!)
                    }
                    sDialog!!.dismissWithAnimation()
                })
                .setCancelClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                    val oldValue = genderEdit!!.getText()
                    if (oldValue != getString(R.string.femaleGender)) {
                        genderEdit!!.setText(getString(R.string.femaleGender))
                        requestForSave(genderEdit!!)
                    }
                    sDialog!!.dismissWithAnimation()
                })
                .setConfirmClickListener(OnSweetClickListener { sDialog: SweetAlertDialog? ->
                    val oldValue = genderEdit!!.getText()
                    if (oldValue != getString(R.string.otherGender)) {
                        genderEdit!!.setText(getString(R.string.otherGender))
                        requestForSave(genderEdit!!)
                    }
                    sDialog!!.dismissWithAnimation()
                })
            dlg.setOnShowListener(OnShowListener { sDialog: DialogInterface? ->
                val sweetDlg = sDialog as SweetAlertDialog
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM)
                    .setBackgroundResource(R.color.record_background_odd)
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0)
                //LayoutParams params = (LayoutParams)sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).getLayoutParams();
                //params.setMargins(0, 0, 0, 0);
                //dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM)
                        .setAutoSizeTextTypeUniformWithConfiguration(
                            8,
                            12,
                            1,
                            TypedValue.COMPLEX_UNIT_SP
                        )
                }
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL)
                    .setBackgroundResource(R.color.record_background_odd)
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0)

                //dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL)
                        .setAutoSizeTextTypeUniformWithConfiguration(
                            8,
                            12,
                            1,
                            TypedValue.COMPLEX_UNIT_SP
                        )
                }
                sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL)
                    .setBackgroundResource(R.color.record_background_odd)
                sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0)

                //dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL)
                        .setAutoSizeTextTypeUniformWithConfiguration(
                            8,
                            12,
                            1,
                            TypedValue.COMPLEX_UNIT_SP
                        )
                }
            })
            dlg
        })

        photoButton!!.setOnClickListener(onClickMachinePhoto)

        imgUtil!!.setOnDeleteImageListener(OnDeleteImageListener { imgUtil: ImageUtil? ->
            imgUtil!!.getView().setImageDrawable(
                requireActivity().getResources().getDrawable(R.drawable.ic_person_black_24dp)
            )
            mCurrentPhotoPath = null
            requestForSave(imgUtil.getView())
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        roundProfile!!.post(Runnable {
            refreshData()
            sizeEdit!!.setOnTextChangeListener(itemOnTextChange)
            birthdayEdit!!.setOnTextChangeListener(itemOnTextChange)
            nameEdit!!.setOnTextChangeListener(itemOnTextChange)
            genderEdit!!.setOnTextChangeListener(itemOnTextChange)
        })
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.mActivity = activity as MainActivity
    }

    val name: String?
        get() = requireArguments().getString("name")

    private fun refreshData() {
        mProfile = this.profil

        /* Initialisation des valeurs */
        if (mProfile!!.size == 0) {
            sizeEdit!!.setText("")
            sizeEdit!!.setHint(getString(R.string.profileEnterYourSize))
        } else {
            sizeEdit!!.setText(mProfile!!.size.toString())
        }

        when (mProfile!!.gender) {
            Gender.MALE -> genderEdit!!.setText(getString(R.string.maleGender))
            Gender.FEMALE -> genderEdit!!.setText(getString(R.string.femaleGender))
            Gender.OTHER -> genderEdit!!.setText(getString(R.string.otherGender))
            else -> {
                genderEdit!!.setText("")
                genderEdit!!.setHint(getString(R.string.enter_gender_here))
            }
        }

        if (mProfile!!.birthday!!.getTime() == 0L) {
            birthdayEdit!!.setText("")
            birthdayEdit!!.setHint(getString(R.string.profileEnterYourBirthday))
        } else {
            birthdayEdit!!.setText(
                DateConverter.dateToLocalDateStr(
                    mProfile!!.birthday,
                    getContext()
                )
            )
            //sizeEdit.setNormalColor();
        }

        nameEdit!!.setText(mProfile!!.name)

        if (mProfile!!.photo != null) {
            ImageUtil.setPic(roundProfile, mProfile!!.photo)
            roundProfile!!.invalidate()
        } else roundProfile!!.setImageDrawable(
            requireActivity().getResources().getDrawable(R.drawable.profile)
        )
    }

    private fun requestForSave(view: View) {
        var profileToUpdate = false
        val viewId = view.getId()

        // Save all the fields in the Profile
        if (viewId == R.id.name) {
            mProfile!!.name = nameEdit!!.getText()
            profileToUpdate = true
        } else if (viewId == R.id.size) {
            try {
                mProfile!!.size = sizeEdit!!.getText().toFloat().toInt()
            } catch (e: NumberFormatException) {
                mProfile!!.size = 0
            }
            profileToUpdate = true
        } else if (viewId == R.id.birthday) {
            mProfile!!.birthday =
                DateConverter.localDateStrToDate(birthdayEdit!!.getText(), getContext())
            profileToUpdate = true
        } else if (viewId == R.id.photo) {
            mProfile!!.photo = mCurrentPhotoPath!!
            profileToUpdate = true
        } else if (viewId == R.id.gender) {
            var lGender = Gender.UNKNOWN
            if (genderEdit!!.getText() == getString(R.string.maleGender)) {
                lGender = Gender.MALE
            } else if (genderEdit!!.getText() == getString(R.string.femaleGender)) {
                lGender = Gender.FEMALE
            } else if (genderEdit!!.getText() == getString(R.string.otherGender)) {
                lGender = Gender.OTHER
            }
            mProfile!!.gender = lGender
            profileToUpdate = true
        }

        if (profileToUpdate) {
            mDb!!.updateProfile(mProfile)
            KToast.infoToast(
                getActivity(),
                mProfile!!.name + " updated",
                Gravity.BOTTOM,
                KToast.LENGTH_SHORT
            )
            mActivity!!.setCurrentProfil(mProfile)
        }
    }

    private val profil: Profile?
        get() = (getActivity() as MainActivity).currentProfile

    val fragment: Fragment
        get() = this

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private fun CreatePhotoSourceDialog(): Boolean {
        if (imgUtil == null) imgUtil = ImageUtil()

        return imgUtil!!.CreatePhotoSourceDialog(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ImageUtil.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath = imgUtil!!.getFilePath()
                ImageUtil.setPic(roundProfile, mCurrentPhotoPath)
                ImageUtil.saveThumb(mCurrentPhotoPath)
                imgUtil!!.galleryAddPic(this, mCurrentPhotoPath)
                requestForSave(roundProfile!!)
            }

            ImageUtil.REQUEST_PICK_GALERY_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val realPath: String?
                realPath = RealPathUtil.getRealPath(this.getContext(), data?.getData())

                ImageUtil.setPic(roundProfile, realPath)
                ImageUtil.saveThumb(realPath)
                mCurrentPhotoPath = realPath
                requestForSave(roundProfile!!)
            }
        }
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): ProfileFragment {
            val f = ProfileFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
