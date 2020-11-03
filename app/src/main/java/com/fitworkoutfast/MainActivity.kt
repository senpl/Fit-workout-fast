package com.fitworkoutfast

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment.getExternalStorageDirectory
import androidx.preference.PreferenceManager
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.easyfitness.*
import com.easyfitness.DAO.*
import com.easyfitness.DAO.cardio.DAOOldCardio
import com.easyfitness.bodymeasures.BodyPartListFragment
import com.easyfitness.fonte.FontesOldPagerFragment
import com.easyfitness.fonte.FontesPagerFragment
import com.easyfitness.intro.MainIntroActivity
import com.easyfitness.machines.MachineFragment
import com.easyfitness.programs.ProgramsPagerFragment
import com.easyfitness.programs.ProgramsPagerFragment.Companion.newInstance
import com.easyfitness.utils.*
import com.mikhaellopez.circularimageview.CircularImageView
import com.onurkaganaldemir.ktoastlib.KToast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_INTRO = 111
    private val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    var mDrawerAdapter: CustomDrawerAdapter? = null
    lateinit var dataList: MutableList<DrawerItem>//? = null
    private var mpFontesPagerFrag: FontesPagerFragment? = null
    private var mpFontesOldPagerFrag: FontesOldPagerFragment? = null
    private var mpProgramPagerFrag: ProgramsPagerFragment? = null
    private var mpWeightFrag: WeightFragment? = null
    private var mpProfileFrag: ProfileFragment? = null
    private var mpMachineFrag: MachineFragment? = null
    private var mpSettingFrag: SettingsFragment? = null
    private var mpAboutFrag: AboutFragment? = null
    private var mpBodyPartListFrag: BodyPartListFragment? = null
    private var currentFragmentName = ""
    private var mDbProfils: DAOProfil? = null
    var currentProfile: Profile? = null
        private set
    private var mCurrentProfilID: Long = -1
    private var m_importCVSchosenDir = ""
    lateinit var activityToolbar: Toolbar
        private set

    /* Navigation Drawer */
    private var mDrawerLayout: DrawerLayout? = null
    private lateinit var mDrawerList: ListView
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private val musicController = MusicController(this)
    private var roundProfile: CircularImageView? = null

    //    private void savePhotoProfile(String path) {
    //        mCurrentProfile.setPhoto(path);// Enregistrer sur le profile le path de la photo.
    //        mDbProfils.updateProfile(mCurrentProfile);
    //    }
    var currentMachine = ""
    private var mIntro014Launched = false
    private var mMigrationBD15done = false
    private val onMenuItemClick = PopupMenu.OnMenuItemClickListener { item: MenuItem ->
        when (item.itemId) {
            R.id.create_newprofil -> {
                activity.createNewProfil()
                return@OnMenuItemClickListener true
            }
            R.id.photo_profil -> {
                val optionListArray = arrayOfNulls<String>(2)
                optionListArray[0] = activity.resources.getString(R.string.camera)
                optionListArray[1] = activity.resources.getString(R.string.gallery)
                //profilListArray[2] = "Remove Image";

                //requestPermissionForWriting(pF);
                val itemActionbuilder = AlertDialog.Builder(activity)
                itemActionbuilder.setTitle("").setItems(optionListArray) { _: DialogInterface?, which: Int ->
                    when (which) {
                        1 -> {
                            val photoPickerIntent = Intent(Intent.ACTION_PICK)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(photoPickerIntent, ImageUtil.REQUEST_PICK_GALERY_PHOTO)
                        }
                        0 ->                             //dispatchTakePictureIntent(mF);
                            // start picker to get image for cropping and then use the image in cropping activity
                            CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(activity)
                        2 -> {
                        }
                        else -> {
                        }
                    }
                }
                itemActionbuilder.show()
                return@OnMenuItemClickListener true
            }
            R.id.change_profil -> {
                val profilListArray = activity.mDbProfils!!.allProfil
                val changeProfilbuilder = AlertDialog.Builder(activity)
                changeProfilbuilder.setTitle(activity.resources.getText(R.string.profil_select_profil))
                    .setItems(profilListArray) { dialog: DialogInterface, which: Int ->
                        val lv = (dialog as AlertDialog).listView
                        val checkedItem = lv.adapter.getItem(which)
                        setCurrentProfil(checkedItem.toString())
                        KToast.infoToast(activity, activity.resources.getText(R.string.profileSelected).toString() + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                    }
                changeProfilbuilder.show()
                return@OnMenuItemClickListener true
            }
            R.id.delete_profil -> {
                val profildeleteListArray = activity.mDbProfils!!.allProfil
                val deleteProfilbuilder = AlertDialog.Builder(activity)
                deleteProfilbuilder.setTitle(activity.resources.getText(R.string.profil_select_profil_to_delete))
                    .setItems(profildeleteListArray) { dialog: DialogInterface, which: Int ->
                        val lv = (dialog as AlertDialog).listView
                        val checkedItem = lv.adapter.getItem(which)
                        if (currentProfile!!.name == checkedItem.toString()) {
                            KToast.errorToast(activity, activity.resources.getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                        } else {
                            val profileToDelete = mDbProfils!!.getProfil(checkedItem.toString())
                            mDbProfils!!.deleteProfil(profileToDelete)
                            KToast.infoToast(activity, getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                        }
                    }
                deleteProfilbuilder.show()
                return@OnMenuItemClickListener true
            }
            R.id.rename_profil -> {
                activity.renameProfil()
                return@OnMenuItemClickListener true
            }
            R.id.param_profil -> {
                showFragment(PROFILE)
                return@OnMenuItemClickListener true
            }
            else -> return@OnMenuItemClickListener false
        }
    }
    private var mBackPressed: Long = 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {

            /* creation de l'arborescence de l'application */
            var folder = File(getExternalStorageDirectory().toString() + "/FitWorkoutFast")
            var success = true
            if (!folder.exists()) {
                success = folder.mkdir()
            }
            if (success) {
                folder = File(getExternalStorageDirectory().toString() + "/FitWorkoutFast/crashreport")
                success = folder.mkdir()
                if (!success) {
                    Toast.makeText(baseContext, "Folder creation failed", Toast.LENGTH_LONG).show()
                }
            }
            if (folder.exists()) {
                if (Thread.getDefaultUncaughtExceptionHandler() !is CustomExceptionHandler) {
                    Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler(getExternalStorageDirectory().toString() + "/FitWorkoutFast/crashreport"))
                }
            }
        }
        val SP = PreferenceManager.getDefaultSharedPreferences(baseContext) //getSharedPreferences(baseContext, MODE_PRIVATE)//???
        val dayNightAuto = SP.getString("dayNightAuto", resources.getInteger(R.integer.dark_mode_value).toString())
        val dayNightAutoValue: Int
        dayNightAutoValue = try {
            dayNightAuto!!.toInt()
        } catch (e: NumberFormatException) {
            resources.getInteger(R.integer.dark_mode_value)
        }
        if (dayNightAutoValue == resources.getInteger(R.integer.dark_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SweetAlertDialog.DARK_STYLE = true
        } else if (dayNightAutoValue == resources.getInteger(R.integer.light_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            SweetAlertDialog.DARK_STYLE = false
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> SweetAlertDialog.DARK_STYLE = false
                Configuration.UI_MODE_NIGHT_YES -> SweetAlertDialog.DARK_STYLE = true
                else -> SweetAlertDialog.DARK_STYLE = false
            }
        }
        setContentView(R.layout.activity_main)
        activityToolbar = findViewById(R.id.actionToolbar)
        setSupportActionBar(activityToolbar)
        activityToolbar.title = resources.getText(R.string.app_name)
        if (savedInstanceState == null) {
            if (mpFontesPagerFrag == null) mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6)
            if (mpFontesOldPagerFrag == null) mpFontesOldPagerFrag = FontesOldPagerFragment.newInstance(FONTESPAGER + "OLD", 6)
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5)
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10)
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8)
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6)
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7)
            if (mpBodyPartListFrag == null) mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9)
        } else {
            mpFontesPagerFrag = supportFragmentManager.getFragment(savedInstanceState, FONTESPAGER) as FontesPagerFragment?
            mpFontesOldPagerFrag = supportFragmentManager.getFragment(savedInstanceState, FONTESPAGER + "OLD") as FontesOldPagerFragment?
            mpWeightFrag = supportFragmentManager.getFragment(savedInstanceState, WEIGHT) as WeightFragment?
            mpProfileFrag = supportFragmentManager.getFragment(savedInstanceState, PROFILE) as ProfileFragment?
            mpSettingFrag = supportFragmentManager.getFragment(savedInstanceState, SETTINGS) as SettingsFragment?
            mpAboutFrag = supportFragmentManager.getFragment(savedInstanceState, ABOUT) as AboutFragment?
            mpMachineFrag = supportFragmentManager.getFragment(savedInstanceState, MACHINES) as MachineFragment?
            mpBodyPartListFrag = supportFragmentManager.getFragment(savedInstanceState, BODYTRACKING) as BodyPartListFragment?
        }
        loadPreferences()
        DatabaseHelper.renameOldDatabase(this)
        if (DatabaseHelper.DATABASE_VERSION >= 15 && !mMigrationBD15done) {
            val mDbOldCardio = DAOOldCardio(this)
            val lDAOMachine = DAOMachine(this)
            if (mDbOldCardio.tableExists()) {
                val mDbCardio = DAOCardio(this)
                val mList = mDbOldCardio.allRecords
                for (record in mList) {
                    val m = lDAOMachine.getMachine(record.exercice)
                    var exerciseName = record.exercice
                    if (m != null) { // if a machine exists
                        if (m.type == DAOMachine.TYPE_FONTE) { // if it is not a Cardio type
                            exerciseName = "$exerciseName-Cardio" // add a suffix to
                        }
                    }
                    mDbCardio.addCardioRecord(record.date, "00:00:00", exerciseName, record.distance, record.duration, record.profil, UnitConverter.UNIT_KM)
                }
                mDbOldCardio.dropTable()
                val mDbFonte = DAOFonte(this)
                val mFonteList = mDbFonte.allBodyBuildingRecords
                for (record in mFonteList) {
                    mDbFonte.updateRecord(record) // Automatically update record Type
                }
                val machineList = lDAOMachine.allMachinesArray
                for (record in machineList) {
                    lDAOMachine.updateMachine(record) // Reset all the fields on machines.
                }
            }
            mMigrationBD15done = true
            savePreferences()
        }
        if (savedInstanceState == null) {
            showFragment(FONTESPAGER, false) // Create fragment, do not add to backstack
            currentFragmentName = FONTESPAGER
        }
        dataList = ArrayList()
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerList = findViewById(R.id.left_drawer)
        val drawerTitleItem = DrawerItem("TITLE", R.drawable.ic_person_black_24dp, true)
        dataList.add(drawerTitleItem)
        dataList.add(DrawerItem(this.resources.getString(R.string.menu_Workout), R.drawable.ic_fitness_center_white_24dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.manu_programs), R.drawable.outline_assignment_white_24, true))
        //dataList.add(new DrawerItem(this.getResources().getString(R.string.CardioMenuLabel), R.drawable.ic_running, true));
        dataList.add(DrawerItem(this.resources.getString(R.string.MachinesLabel), R.drawable.ic_gym_bench_50dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.weightMenuLabel), R.drawable.ic_bathroom_scale_white_50dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.bodytracking), R.drawable.ic_ruler_white_50dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.SettingLabel), R.drawable.ic_settings_white_24dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.single_exercise_and_results), R.drawable.sharp_history_edu_white_24dp, true))
        dataList.add(DrawerItem(this.resources.getString(R.string.AboutLabel), R.drawable.ic_info_outline_white_24dp, true))
        mDrawerAdapter = CustomDrawerAdapter(this, R.layout.custom_drawer_item,
            dataList)
        mDrawerList.adapter = mDrawerAdapter
        roundProfile = activityToolbar.findViewById(R.id.imageProfile)
        mDrawerToggle = ActionBarDrawerToggle(
            this,  /* host Activity */
            mDrawerLayout,  /* DrawerLayout object */
            activityToolbar,  /* nav drawer icon to replace 'Up' caret */
            R.string.drawer_open, R.string.drawer_close
        )

        // Set the list's click listener
        mDrawerList.onItemClickListener = DrawerItemClickListener()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        musicController.initView()

        // Lance l'intro
        // Tester si l'intro a déjà été lancé
        if (!mIntro014Launched) {
            val intent = Intent(this, MainIntroActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_INTRO)
        }
    }

    override fun onStart() {
        super.onStart() // Always call the superclass method first
        if (mIntro014Launched) {
            initActivity()
            initDEBUGdata()
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val bShowMP3 = sharedPreferences.getBoolean("prefShowMP3", false)
        showMP3Toolbar(bShowMP3)
    }

    private fun initDEBUGdata() {
        if (BuildConfig.DEBUG) {
            // do something for a debug build
            val lDbFonte = DAOFonte(this)
            if (lDbFonte.count == 0) {
                lDbFonte.addBodyBuildingRecord(DateConverter.dateToDate(2019, 7, 1), "Exercise 1", 1, 10, 40f, currentProfile, 0, "", "12:34:56")
                lDbFonte.addBodyBuildingRecord(DateConverter.dateToDate(2019, 6, 30), "Exercise 2", 1, 10, 50f, currentProfile, 0, "", "12:34:56")
            }
            val lDbCardio = DAOCardio(this)
            if (lDbCardio.count == 0) {
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 7, 1), "01:02:03", "Course", 1000f, 10000, currentProfile, UnitConverter.UNIT_KM)
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 7, 31), "01:02:03", "Rameur", 5000f, 20000, currentProfile, UnitConverter.UNIT_MILES)
            }
            val lDbStatic = DAOStatic(this)
            if (lDbStatic.count == 0) {
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 7, 1), "Exercise ISO 1", 1, 50, 40f, currentProfile, 0, "", "12:34:56")
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 7, 31), "Exercise ISO 2", 1, 60, 40f, currentProfile, 0, "", "12:34:56")
            }
        }
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //Save the fragment's instance
        if (fontesPagerFragment!!.isAdded) supportFragmentManager.putFragment(outState, FONTESPAGER, mpFontesPagerFrag!!)
        if (programsFragment.isAdded) supportFragmentManager.putFragment(outState, PROGRAMS, mpProgramPagerFrag!!)
        if (fontesOldPagerFragment!!.isAdded) supportFragmentManager.putFragment(outState, FONTESPAGER + "OLD", mpFontesOldPagerFrag!!)
        if (weightFragment!!.isAdded) supportFragmentManager.putFragment(outState, WEIGHT, mpWeightFrag!!)
        if (profileFragment!!.isAdded) supportFragmentManager.putFragment(outState, PROFILE, mpProfileFrag!!)
        if (machineFragment!!.isAdded) supportFragmentManager.putFragment(outState, MACHINES, mpMachineFrag!!)
        if (aboutFragment!!.isAdded) supportFragmentManager.putFragment(outState, ABOUT, mpAboutFrag!!)
        if (settingsFragment.isAdded) supportFragmentManager.putFragment(outState, SETTINGS, mpSettingFrag!!)
        if (bodyPartFragment!!.isAdded) supportFragmentManager.putFragment(outState, BODYTRACKING, mpBodyPartListFrag!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu items for use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_actions, menu)

        // restore the profile picture in case it was overwritten during the menu inflate
        if (currentProfile != null) setPhotoProfile(currentProfile!!.photo)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        val alertMenuItem = menu.findItem(R.id.action_profil)
        roundProfile!!.setOnClickListener { v: View? ->
            val popup = PopupMenu(activity, v)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.profile_actions, popup.menu)
            popup.setOnMenuItemClickListener(onMenuItemClick)
            popup.show()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun exportDatabase() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            // Afficher une boite de dialogue pour confirmer
            val exportDbBuilder = AlertDialog.Builder(this)
            exportDbBuilder.setTitle(activity.resources.getText(R.string.export_database))
            exportDbBuilder.setMessage(activity.resources.getText(R.string.export_question).toString() + " " + currentProfile!!.name + "?")

            // Si oui, supprimer la base de donnee et refaire un Start.
            exportDbBuilder.setPositiveButton(activity.resources.getText(R.string.global_yes)) { dialog: DialogInterface, _: Int ->
                val cvsMan = CVSManager(activity.baseContext)
                if (cvsMan.exportDatabase(currentProfile)) {
                    KToast.successToast(activity, currentProfile!!.name + ": " + activity.resources.getText(R.string.export_success), Gravity.BOTTOM, KToast.LENGTH_LONG)
                } else {
                    KToast.errorToast(activity, currentProfile!!.name + ": " + activity.resources.getText(R.string.export_failed), Gravity.BOTTOM, KToast.LENGTH_LONG)
                }

                // Do nothing but close the dialog
                dialog.dismiss()
            }
            exportDbBuilder.setNegativeButton(activity.resources.getText(R.string.global_no)) { dialog: DialogInterface, _: Int ->
                // Do nothing
                dialog.dismiss()
            }
            val exportDbDialog = exportDbBuilder.create()
            exportDbDialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else when (item.itemId) {
            R.id.export_database -> {
                exportDatabase()
                true
            }
            R.id.import_database -> {
                // Create DirectoryChooserDialog and register a callback
                val fileChooserDialog = FileChooserDialog(this) { chosenDir: String ->
                    m_importCVSchosenDir = chosenDir
                    //Toast.makeText(getActivity().getBaseContext(), "Chosen directory: " +
                    //    chosenDir, Toast.LENGTH_LONG).show();
                    SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(this.resources.getString(R.string.global_confirm_question))
                        .setContentText(this.resources.getString(R.string.import_new_exercise_first))
                        .setConfirmText(this.resources.getString(R.string.global_yes))
                        .setConfirmClickListener { sDialog: SweetAlertDialog ->
                            sDialog.dismissWithAnimation()
                            val cvsMan = CVSManager(activity.baseContext)
                            if (cvsMan.importDatabase(m_importCVSchosenDir, currentProfile)) {
                                KToast.successToast(activity, m_importCVSchosenDir + " " + activity.resources.getString(R.string.imported_successfully), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                            } else {
                                KToast.errorToast(activity, m_importCVSchosenDir + " " + activity.resources.getString(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                            }
                            setCurrentProfil(currentProfile) // Refresh profile
                        }
                        .setCancelText(this.resources.getString(R.string.global_no))
                        .show()
                }
                fileChooserDialog.fileFilter = "csv"
                fileChooserDialog.chooseDirectory(getExternalStorageDirectory().toString() + "/FastnFitness/export")
                true
            }
            R.id.action_deleteDB -> {
                // Afficher une boite de dialogue pour confirmer
                val deleteDbBuilder = AlertDialog.Builder(this)
                deleteDbBuilder.setTitle(activity.resources.getText(R.string.global_confirm))
                deleteDbBuilder.setMessage(activity.resources.getText(R.string.deleteDB_warning))

                // Si oui, supprimer la base de donnee et refaire un Start.
                deleteDbBuilder.setPositiveButton(activity.resources.getText(R.string.global_yes)) { dialog: DialogInterface, _: Int ->
                    // recupere le premier ID de la liste.
                    val lList = mDbProfils!!.allProfils
                    run {
                        var i = 0
                        while (i < lList.size) {
                            val mTempProfile = lList[i]
                            mDbProfils!!.deleteProfil(mTempProfile.id)
                            i++
                        }
                    }
                    val mDbMachines = DAOMachine(activity)
                    // recupere le premier ID de la liste.
                    val lList2: List<Machine> = mDbMachines.allMachinesArray
                    var i = 0
                    while (i < lList2.size) {
                        val mTemp = lList2[i]
                        mDbMachines.delete(mTemp.id)
                        i++
                    }

                    // redisplay the intro
                    mIntro014Launched = false

                    // Do nothing but close the dialog
                    dialog.dismiss()
                    finish()
                }
                deleteDbBuilder.setNegativeButton(activity.resources.getText(R.string.global_no)) { dialog: DialogInterface, _: Int ->
                    // Do nothing
                    dialog.dismiss()
                }
                val deleteDbDialog = deleteDbBuilder.create()
                deleteDbDialog.show()
                true
            }
            R.id.action_apropos -> {
                // Display the fragment as the main content.
                showFragment(ABOUT)
                //getAboutFragment().setHasOptionsMenu(true);
                true
            }
            R.id.action_chrono -> {
                val cdd = ChronoDialogbox(this@MainActivity)
                cdd.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If request is cancelled, the result arrays are empty.
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                exportDatabase()
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            }
        }
    }

    fun createNewProfil(): Boolean {
        val newProfilBuilder = AlertDialog.Builder(this)
        newProfilBuilder.setTitle(activity.resources.getText(R.string.createProfilTitle))
        newProfilBuilder.setMessage(activity.resources.getText(R.string.createProfilQuestion))

        // Set an EditText view to get user input
        val input = EditText(this)
        input.setText(R.string.anonymous)
        newProfilBuilder.setView(input)
        newProfilBuilder.setPositiveButton(activity.resources.getText(R.string.global_ok)) { _: DialogInterface?, _: Int ->
            val value = input.text.toString()
            if (value.isEmpty()) {
                createNewProfil()
            } else {
                // Create the new profil
                mDbProfils!!.addProfil(value)
                // Make it the current.
                setCurrentProfil(value)
            }
        }
        newProfilBuilder.setNegativeButton(activity.resources.getText(R.string.global_cancel)) { _: DialogInterface?, _: Int ->
            if (currentProfile == null) {
                createNewProfil()
            }
        }
        newProfilBuilder.show()
        return true
    }

    private fun renameProfil(): Boolean {
        val newBuilder = AlertDialog.Builder(this)
        newBuilder.setTitle(activity.resources.getText(R.string.renameProfilTitle))
        newBuilder.setMessage(activity.resources.getText(R.string.renameProfilQuestion))

        // Set an EditText view to get user input
        val input = EditText(this)
        input.setText(currentProfile!!.name)
        newBuilder.setView(input)
        newBuilder.setPositiveButton(activity.resources.getText(R.string.global_ok)) { _: DialogInterface?, _: Int ->
            val value = input.text.toString()
            if (!value.isEmpty()) {
                // Get current profil
                val temp = currentProfile
                // Rename it
                temp!!.name = value
                // Commit it
                mDbProfils!!.updateProfile(temp)
                // Make it the current.
                setCurrentProfil(value)
            }
        }
        newBuilder.setNegativeButton(activity.resources.getText(R.string.global_cancel)) { _: DialogInterface?, _: Int -> }
        newBuilder.show()
        return true
    }

    private fun setDrawerTitle(pProfilName: String) {
        Objects.requireNonNull(mDrawerAdapter!!.getItem(0))?.title  = pProfilName
        mDrawerAdapter!!.notifyDataSetChanged()
        mDrawerLayout!!.invalidate()
    }

    /**
     * Swaps fragments in the main content view
     */
    private fun selectItem(position: Int) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList!!.setItemChecked(position, true)
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout!!.closeDrawer(mDrawerList!!)
    }

    override fun setTitle(title: CharSequence) {
        supportActionBar!!.title = title
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    private fun showFragment(pFragmentName: String, addToBackStack: Boolean = true) {
        if (currentFragmentName == pFragmentName) return  // If this is already the current fragment, do no replace.
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()

        // Then show the fragments
        if (pFragmentName == FONTESPAGER) {
            ft.replace(R.id.fragment_container, fontesPagerFragment!!, FONTESPAGER)
        } else if (pFragmentName == PROGRAMS) {
            ft.replace(R.id.fragment_container, programsFragment, PROGRAMS)
        } else if (pFragmentName == FONTESPAGER + "OLD") {
            ft.replace(R.id.fragment_container, fontesOldPagerFragment!!, FONTESPAGER + "OLD")
        } else if (pFragmentName == WEIGHT) {
            ft.replace(R.id.fragment_container, weightFragment!!, WEIGHT)
        } else if (pFragmentName == SETTINGS) {
            ft.replace(R.id.fragment_container, settingsFragment, SETTINGS)
        } else if (pFragmentName == MACHINES) {
            ft.replace(R.id.fragment_container, machineFragment!!, MACHINES)
        } else if (pFragmentName == ABOUT) {
            ft.replace(R.id.fragment_container, aboutFragment!!, ABOUT)
        } else if (pFragmentName == BODYTRACKING) {
            ft.replace(R.id.fragment_container, bodyPartFragment!!, BODYTRACKING)
        } else if (pFragmentName == PROFILE) {
            ft.replace(R.id.fragment_container, profileFragment!!, PROFILE)
        }
        currentFragmentName = pFragmentName
        //if (addToBackStack) ft.addToBackStack(null);
        ft.commit()
    }

    override fun onStop() {
        super.onStop()
        savePreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //@SuppressLint("RestrictedApi")
    private fun setCurrentProfil(newProfilName: String?) {
        val newProfil = mDbProfils!!.getProfil(newProfilName)
        setCurrentProfil(newProfil)
    }

    fun setCurrentProfil(newProfil: Profile?) {
        if (newProfil != null) if (currentProfile == null || mCurrentProfilID != newProfil.id || !currentProfile!!.equals(newProfil)) {
            currentProfile = newProfil
            mCurrentProfilID = currentProfile!!.id

            // rafraichit le fragment courant
            val fragmentManager = supportFragmentManager
            //FragmentTransaction ft=fragmentManager.beginTransaction();
            //showFragment(WEIGHT);

            // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
            // Il faut donc que cela soit pris en compte dans le refresh des fragments.
            for (i in fragmentManager.fragments.indices) {
                if (fragmentManager.fragments[i] != null) fragmentManager.fragments[i].onHiddenChanged(false)
            }
            setDrawerTitle(currentProfile!!.name)
            setPhotoProfile(currentProfile!!.photo)
            savePreferences()
        }
    }

    //    public long getCurrentProfilID() {
    //        return mCurrentProfile.getId();
    //    }
    private fun setPhotoProfile(path: String) {
        val imgUtil = ImageUtil()

        // Check if path is pointing to a thumb else create it and use it.
        val thumbPath = imgUtil.getThumbPath(path)
        if (thumbPath != null) {
            ImageUtil.setPic(roundProfile, thumbPath)
            mDrawerAdapter!!.getItem(0)!!.img = thumbPath
            mDrawerAdapter!!.notifyDataSetChanged()
            mDrawerLayout!!.invalidate()
        } else {
            roundProfile!!.setImageDrawable(getDrawable(R.drawable.ic_person_black_24dp))
            mDrawerAdapter!!.getItem(0)!!.imgResID = R.drawable.ic_person_black_24dp
            mDrawerAdapter!!.getItem(0)!!.img = null // Img has priority over Resource
            mDrawerAdapter!!.notifyDataSetChanged()
            mDrawerLayout!!.invalidate()
        }
    }

    val activity: MainActivity
        get() = this

    private fun loadPreferences() {
        // Restore preferences
        val settings = getSharedPreferences(PREFS_NAME, 0)
        mCurrentProfilID = settings.getLong("currentProfil", -1) // return -1 if it doesn't exist
        mIntro014Launched = settings.getBoolean("intro014Launched", false)
        mMigrationBD15done = settings.getBoolean("migrationBD15done", false)
    }

    private fun savePreferences() {
        // Restore preferences
        val settings = getSharedPreferences(PREFS_NAME, 0)
        val editor :SharedPreferences.Editor = settings.edit()
        if (currentProfile != null) {
            editor.putLong("currentProfil", currentProfile!!.id).apply()
        }
        editor.putBoolean("intro014Launched", mIntro014Launched)
        editor.putBoolean("migrationBD15done", mMigrationBD15done)
        editor.apply()
    }

    private val fontesPagerFragment: FontesPagerFragment?
        get() {
            if (mpFontesPagerFrag == null) mpFontesPagerFrag = supportFragmentManager.findFragmentByTag(FONTESPAGER) as FontesPagerFragment?
            if (mpFontesPagerFrag == null) mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6)
            return mpFontesPagerFrag
        }
    private val fontesOldPagerFragment: FontesOldPagerFragment?
        get() {
            if (mpFontesOldPagerFrag == null) mpFontesOldPagerFrag = supportFragmentManager.findFragmentByTag(FONTESPAGER + "OLD") as FontesOldPagerFragment?
            if (mpFontesOldPagerFrag == null) mpFontesOldPagerFrag = FontesOldPagerFragment.newInstance(FONTESPAGER + "OLD", 6)
            return mpFontesOldPagerFrag
        }
    private val weightFragment: WeightFragment?
        get() {
            if (mpWeightFrag == null) mpWeightFrag = supportFragmentManager.findFragmentByTag(WEIGHT) as WeightFragment?
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5)
            return mpWeightFrag
        }
    private val programsFragment: ProgramsPagerFragment
        get() {
            if (mpProgramPagerFrag == null) mpProgramPagerFrag = supportFragmentManager.findFragmentByTag(PROGRAMS) as ProgramsPagerFragment?
            if (mpProgramPagerFrag == null) mpProgramPagerFrag = newInstance(PROGRAMS, Integer.valueOf(11))
            return mpProgramPagerFrag!!
        }
    private val profileFragment: ProfileFragment?
        get() {
            if (mpProfileFrag == null) mpProfileFrag = supportFragmentManager.findFragmentByTag(PROFILE) as ProfileFragment?
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10)
            return mpProfileFrag
        }
    private val machineFragment: MachineFragment?
        get() {
            if (mpMachineFrag == null) mpMachineFrag = supportFragmentManager.findFragmentByTag(MACHINES) as MachineFragment?
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7)
            return mpMachineFrag
        }
    private val aboutFragment: AboutFragment?
        get() {
            if (mpAboutFrag == null) mpAboutFrag = supportFragmentManager.findFragmentByTag(ABOUT) as AboutFragment?
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6)
            return mpAboutFrag
        }
    private val bodyPartFragment: BodyPartListFragment?
        get() {
            if (mpBodyPartListFrag == null) mpBodyPartListFrag = supportFragmentManager.findFragmentByTag(BODYTRACKING) as BodyPartListFragment?
            if (mpBodyPartListFrag == null) mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9)
            return mpBodyPartListFrag
        }
    private val settingsFragment: SettingsFragment
        get() {
            if (mpSettingFrag == null) mpSettingFrag = supportFragmentManager.findFragmentByTag(SETTINGS) as SettingsFragment?
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8)
            return mpSettingFrag!!
        }

    fun restoreToolbar() {
        if (activityToolbar != null) setSupportActionBar(activityToolbar)
    }

    fun showMP3Toolbar(show: Boolean) {
        val mp3toolbar = findViewById<Toolbar>(R.id.musicToolbar)
        if (!show) {
            mp3toolbar.visibility = View.GONE
        } else {
            mp3toolbar.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                initActivity()
                mIntro014Launched = true
                initDEBUGdata()
                savePreferences()
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish()
            }
        }
    }

    override fun onBackPressed() {
        val index = activity.supportFragmentManager.backStackEntryCount - 1
        if (index >= 0) { // Si on est dans une sous activité
            val backEntry = supportFragmentManager.getBackStackEntryAt(index)
            val tag = backEntry.name
            supportFragmentManager.findFragmentByTag(tag)
            super.onBackPressed()
            activity.supportActionBar?.show()
        } else { // Si on est la racine, avec il faut cliquer deux fois
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed()
                return
            } else {
                Toast.makeText(baseContext, R.string.pressBackAgain, Toast.LENGTH_SHORT).show()
            }
            mBackPressed = System.currentTimeMillis()
        }
    }

    fun initActivity() {
        // Initialisation des objets DB
        mDbProfils = DAOProfil(this.applicationContext)

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        /*if (mDbProfils.getCount() == 0 || mCurrentProfilID == -1) {
            // Ouvre la fenetre de creation de profil
            this.CreateNewProfil();
        } else {*/currentProfile = mDbProfils!!.getProfil(mCurrentProfilID)
        if (currentProfile == null) { // au cas ou il y aurait un probleme de synchro
            try {
                val lList = mDbProfils!!.allProfils
                currentProfile = lList[0]
            } catch (e: IndexOutOfBoundsException) {
                createNewProfil()
            }
        }
        if (currentProfile != null) setCurrentProfil(currentProfile!!.name)
    }

    private inner class DrawerItemClickListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            selectItem(position)
            title = when (position) {
                0 -> {
                    showFragment(PROFILE)
                    getString(R.string.ProfileLabel)
                }
                1 -> {
                    showFragment(FONTESPAGER)
                    resources.getText(R.string.menu_Workout)
                }
                2 -> {
                    showFragment(PROGRAMS)
                    resources.getText(R.string.fitness_programs)
                }
                3 -> {
                    showFragment(MACHINES)
                    resources.getText(R.string.MachinesLabel)
                }
                4 -> {
                    showFragment(WEIGHT)
                    resources.getText(R.string.weightMenuLabel)
                }
                5 -> {
                    showFragment(BODYTRACKING)
                    resources.getText(R.string.bodytracking)
                }
                6 -> {
                    showFragment(SETTINGS)
                    resources.getText(R.string.SettingLabel)
                }
                7 -> {
                    showFragment(FONTESPAGER + "OLD")
                    resources.getText(R.string.revision_history_label)
                }
                8 -> {
                    showFragment(ABOUT)
                    resources.getText(R.string.AboutLabel)
                }
                else -> {
                    showFragment(FONTESPAGER)
                    resources.getText(R.string.FonteLabel)
                }
            }
        }
    }

    companion object {
        private const val TIME_INTERVAL = 2000 // # milliseconds, desired time passed between two back presses.
        var FONTESPAGER = "FontePager"
        var FONTES = "Fonte"
        var HISTORY = "History"
        var GRAPHIC = "Graphics"
        var CARDIO = "Cardio"
        var WEIGHT = "Weight"
        var PROFILE = "Profile"
        var PROGRAMS = "Programs"
        var BODYTRACKING = "BodyTracking"
        @JvmField
        var BODYTRACKINGDETAILS = "BodyTrackingDetail"
        var ABOUT = "About"
        var SETTINGS = "Settings"
        var MACHINES = "Machines"
        @JvmField
        var MACHINESDETAILS = "MachinesDetails"
        var PREFS_NAME = "prefsfile"
    }
}
