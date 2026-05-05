package com.easyfitness

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DatabaseHelper
import com.easyfitness.licenses.CustomLicense
import com.fitworkoutfast.MainActivity
import de.psdev.licensesdialog.LicensesDialog
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21
import de.psdev.licensesdialog.licenses.License
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice

class AboutFragment : Fragment() {
    private val name: String? = null
    private val id = 0
    var mainActivity: MainActivity? = null
        private set

    private val clickLicense = View.OnClickListener { v: View? ->
        var name: String? = null
        var url: String? = null
        var copyright: String? = null
        var license: License? = null

        val id = v!!.getId()
        if (id == R.id.MPAndroidChart) {
            name = "MPAndroidChart"
            url = "https://github.com/PhilJay/MPAndroidChart"
            copyright = "Copyright 2019 Philipp Jahoda"
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.javaCSV) {
            name = "JavaCSV"
            url = "https://sourceforge.net/projects/javacsv/"
            copyright = ""
            license = GnuLesserGeneralPublicLicense21()
        } else if (id == R.id.antoniomChronometer) {
            name = "Millisecond-Chronometer"
            url = "https://github.com/antoniom/Millisecond-Chronometer"
            copyright = ""
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.LicensesDialog) {
            name = "LicensesDialog"
            url = "https://github.com/PSDev/LicensesDialog"
            copyright = "Copyright 2013 Philip Schiffer"
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.PagerSlidingTabStrip) {
            name = "PagerSlidingTabStrip"
            url = "https://github.com/astuetz/PagerSlidingTabStrip"
            copyright = "Andreas Stuetz - andreas.stuetz@gmail.com"
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.SmartTabLayout) {
            name = "SmartTabLayout"
            url = "https://github.com/ogaclejapan/SmartTabLayout"
            copyright = "Copyright (C) 2015 ogaclejapan"
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.flaticonCredits) {
            name = "Flaticon"
            url = "https://www.flaticon.com"
            copyright = "Copyright © 2013-2019 Freepik Company S.L."
            license = CustomLicense(
                "Free License (with attribution)",
                "https://profile.flaticon.com/license/free"
            )
        } else if (id == R.id.freepikCredits) {
            name = "Freepik"
            url = "https://www.freepik.com"
            copyright = "Copyright © 2010-2019 Freepik Company S.L."
            license = CustomLicense(
                "Free License (with attribution)",
                "https://profile.freepik.com/license/free"
            )
        } else if (id == R.id.CircleProgress) {
            name = "CircleProgress"
            url = "https://github.com/lzyzsd/CircleProgress"
            copyright = "Copyright (C) 2014 Bruce Lee <bruceinpeking#gmail.com>"
            license = CustomLicense("WTFPL License", "http://www.wtfpl.net/txt/copying/")
        } else if (id == R.id.CircularImageView) {
            name = "CircularImageView"
            url = "https://github.com/lopspower/CircularImageView"
            copyright = "Lopez Mikhael"
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.ktoast) {
            name = "KToast"
            url = "https://github.com/onurkagan/KToast"
            copyright = ""
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.SweetAlertDialog) {
            name = "SweetAlertDialog"
            url = "https://github.com/F0RIS/sweet-alert-dialog"
            copyright = "Pedant (http://pedant.cn)"
            license = MITLicense()
        } else if (id == R.id.AndroidImageCropper) {
            name = "Android-Image-Cropper"
            url = "https://github.com/ArthurHub/Android-Image-Cropper"
            copyright = "Copyright 2016, Arthur Teplitzki, 2013, Edmodo, Inc."
            license = ApacheSoftwareLicense20()
        } else if (id == R.id.MaterialFavoriteButton) {
            name = "Material Favorite Button"
            url = "https://github.com/IvBaranov/MaterialFavoriteButton"
            copyright = "Copyright 2015 Ivan Baranov"
            license = ApacheSoftwareLicense20()
        }

        val notice = Notice(name, url, copyright, license)
        LicensesDialog.Builder(this.mainActivity)
            .setNotices(notice)
            .build()
            .show()
    }


    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.tab_about, container, false)

        //TextView pAppVersion = view.findViewById(R.id.app_version_textview);
        //pAppVersion.setText(); TODO get code version from Manifest
        val mpDBVersionTextView = view.findViewById<TextView>(R.id.database_version)
        mpDBVersionTextView.setText(DatabaseHelper.DATABASE_VERSION.toString())

        val mpMPAndroidChartTextView = view.findViewById<TextView>(R.id.MPAndroidChart)
        val mpJavaCVSTextView = view.findViewById<TextView>(R.id.javaCSV)
        val mpLicenseDialogTextView = view.findViewById<TextView>(R.id.LicensesDialog)
        val mpChronometerTextView = view.findViewById<TextView>(R.id.antoniomChronometer)
        val mpPagerSlidingTabStripTextView = view.findViewById<TextView>(R.id.PagerSlidingTabStrip)

        val mpSmartTabLayoutTextView = view.findViewById<TextView>(R.id.SmartTabLayout)
        val mpFlaticonTextView = view.findViewById<TextView>(R.id.flaticonCredits)
        val mpFreepikView = view.findViewById<TextView>(R.id.freepikCredits)
        val mpCircleProgressView = view.findViewById<TextView>(R.id.CircleProgress)
        val mpCircularImageView = view.findViewById<TextView>(R.id.CircularImageView)
        val mpkToast = view.findViewById<TextView>(R.id.ktoast)
        val mpSweetAlertDialog = view.findViewById<TextView>(R.id.SweetAlertDialog)
        val mpAndroidImageCropper = view.findViewById<TextView>(R.id.AndroidImageCropper)
        val mpMaterialFavoriteButton = view.findViewById<TextView>(R.id.MaterialFavoriteButton)


        mpMPAndroidChartTextView.setOnClickListener(clickLicense)
        mpJavaCVSTextView.setOnClickListener(clickLicense)
        mpLicenseDialogTextView.setOnClickListener(clickLicense)
        mpChronometerTextView.setOnClickListener(clickLicense)
        mpPagerSlidingTabStripTextView.setOnClickListener(clickLicense)
        mpSmartTabLayoutTextView.setOnClickListener(clickLicense)
        mpFlaticonTextView.setOnClickListener(clickLicense)
        mpFreepikView.setOnClickListener(clickLicense)
        mpCircleProgressView.setOnClickListener(clickLicense)
        mpCircularImageView.setOnClickListener(clickLicense)
        mpkToast.setOnClickListener(clickLicense)
        mpSweetAlertDialog.setOnClickListener(clickLicense)
        mpAndroidImageCropper.setOnClickListener(clickLicense)
        mpMaterialFavoriteButton.setOnClickListener(clickLicense)

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): AboutFragment {
            val f = AboutFragment()

            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.setArguments(args)

            return f
        }
    }
}
