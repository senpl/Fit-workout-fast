package com.easyfitness.licenses

import android.content.Context
import de.psdev.licensesdialog.licenses.License

/**
 * Created by senpl
 */
class CustomLicense(private var mLicenseName: String, private var mLicenseURL: String) : License() {
    override fun getName(): String {
        return mLicenseName
    }

    override fun readSummaryTextFromResources(context: Context): String {
        return ""
    }

    override fun readFullTextFromResources(context: Context): String {
        return ""
    }

    override fun getVersion(): String {
        return ""
    }

    override fun getUrl(): String {
        return mLicenseURL
    }

    companion object {
        private const val serialVersionUID = 5165684351346813168L
    }
}
