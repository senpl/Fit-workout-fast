package com.easyfitness.utils

import android.R.id
import android.R.layout
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.easyfitness.R
import java.io.File
import java.io.IOException
import java.util.Collections
import kotlin.math.max

//DirectoryChooserDialog.java

class FileChooserDialog(
    private val m_context: Context,
    chosenDirectoryListener: ChosenFileListener?
) {
    /*
     * setNewFolderEnabled() - enable/disable new folder button
     */
    var newFolderEnabled: Boolean = false

    /*
     * setDisplayFolderOnly() - display Folder only
     */
    var displayFolderOnly: Boolean = false

    /*
     * setFileFilter() - allow to filter file without specified extensions
     */
    var fileFilter: String = "*"
    private var m_sdcardDirectory = ""
    private var m_titleView: TextView? = null

    private var m_dir: String? = ""
    private var m_subdirs: MutableList<String?>? = null
    private var m_chosenFileListener: ChosenFileListener? = null
    private var m_listAdapter: ArrayAdapter<String?>? = null

    init {
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()
        m_chosenFileListener = chosenDirectoryListener

        try {
            m_sdcardDirectory = File(m_sdcardDirectory).getCanonicalPath()
        } catch (ignored: IOException) {
        }
    }

    fun resetFileFilter(fileFilter: String) {
        this.fileFilter = fileFilter
    }

    /**//////////////////////////////////////////////////////////////////// */ // chooseDirectory() - load directory chooser dialog for initial
    // default sdcard directory
    /**//////////////////////////////////////////////////////////////////// */
    @JvmOverloads
    fun chooseDirectory(dir: String = m_sdcardDirectory) {
        var dir = dir
        val dirFile = File(dir)
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = m_sdcardDirectory
        }

        try {
            dir = File(dir).getCanonicalPath()
        } catch (ioe: IOException) {
            return
        }

        m_dir = dir
        m_subdirs = getDirectories(dir)

        class DirectoryOnClickListener : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item: Int) {
                if ((dialog as AlertDialog).getListView().getAdapter().getItem(item).toString()
                        .substring(0, 1) == "/"
                ) {
                    // Navigate into the sub-directory
                    m_dir += dialog.getListView().getAdapter().getItem(item)
                    dialog.getListView()
                        .smoothScrollToPositionFromTop(0, 0, 0) // Back on top of the ListView
                    updateDirectory()
                } else if (dialog.getListView().getAdapter().getItem(item).toString() == "..") {
                    // Navigate back to an upper directory
                    m_dir = File(m_dir).getParent()
                    dialog.getListView().smoothScrollToPositionFromTop(
                        0,
                        0,
                        0
                    ) //.scrollTo(0, 0);//.smoothScrollToPosition(0);
                    updateDirectory()
                } else {
                    // Current directory chosen
                    if (m_chosenFileListener != null) {
                        // Call registered listener supplied with the chosen directory
                        m_chosenFileListener!!.onChosenFile(
                            m_dir + "/" + dialog.getListView().getAdapter().getItem(item).toString()
                        )
                        dialog.dismiss()
                    }
                }
            }
        }

        val dialogBuilder =
            createDirectoryChooserDialog(dir, m_subdirs!!, DirectoryOnClickListener())

        /*
        dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            // Current directory chosen
            if (m_chosenFileListener != null) {
                // Call registered listener supplied with the chosen directory
                m_chosenFileListener.onChosenFile(m_dir);
            }
        });
*/
        dialogBuilder.setNegativeButton("Cancel", null)

        val dirsDialog = dialogBuilder.create()

        // Show directory chooser dialog
        dirsDialog.show()
    }

    /**///////////////////////////////////////////////////////////////////////////// */ // chooseDirectory(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    /**///////////////////////////////////////////////////////////////////////////// */
    private fun createSubDir(newDir: String): Boolean {
        val newDirFile = File(newDir)
        if (!newDirFile.exists()) {
            return newDirFile.mkdir()
        }

        return false
    }

    fun getDirectories(dir: String): MutableList<String?> {
        val dirs: MutableList<String?> = ArrayList<String?>()

        try {
            val dirFile = File(dir)
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs
            }

            if (dir.length > 1) dirs.add("..")

            for (file in dirFile.listFiles()) {
                if (file.isDirectory())  // Get Directories
                {
                    dirs.add("/" + file.getName())
                } else  // Get files
                {
                    if (!this.displayFolderOnly) if (isInFilter(file.getName())) {
                        dirs.add(file.getName())
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        Collections.sort<String?>(
            dirs,
            Comparator { obj: String?, anotherString: String? -> obj!!.compareTo(anotherString!!) })

        return dirs
    }

    /*
     * return only files with full path of a specific folder
     */
    fun getFiles(dir: String): MutableList<String?> {
        val dirs: MutableList<String?> = ArrayList<String?>()

        try {
            val dirFile = File(dir)
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs
            }

            for (file in dirFile.listFiles()) {
                if (!file.isDirectory())  // Don't get Directories
                {
                    if (!this.displayFolderOnly) if (isInFilter(file.getName())) {
                        dirs.add(file.getName())
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        Collections.sort<String?>(
            dirs,
            Comparator { obj: String?, anotherString: String? -> obj!!.compareTo(anotherString!!) })

        return dirs
    }

    /*
     * return true if file is allowed
     */
    private fun isInFilter(fileName: String): Boolean {
        val ret = false
        var extension = ""

        // recupere l'extension du fichier
        extension = getExtension(fileName)

        // verifie si l'extension est prise en compte
        if (this.fileFilter.contains("*")) return true
        if (this.fileFilter.contains(extension)) return true

        return ret
    }

    private fun getExtension(fileName: String): String {
        var extension = ""

        val i = fileName.lastIndexOf('.')
        val p = max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'))

        if (i > p) {
            extension = fileName.substring(i + 1)
        }
        return extension
    }

    private fun createDirectoryChooserDialog(
        title: String?, listItems: MutableList<String?>,
        onClickListener: DialogInterface.OnClickListener?
    ): AlertDialog.Builder {
        val dialogBuilder = AlertDialog.Builder(m_context)

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        val titleLayout = LinearLayout(m_context)
        titleLayout.setOrientation(LinearLayout.VERTICAL)

        m_titleView = TextView(m_context)
        m_titleView!!.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        m_titleView!!.setTextAppearance(
            m_context,
            android.R.style.TextAppearance_DeviceDefault_Medium
        )
        m_titleView!!.setTextColor(m_context.getResources().getColor(android.R.color.black))
//        m_titleView!!.setGravity(Gravity.CENTER_VERTICAL or Gravity.START)
        m_titleView!!.setText(title)

        val newDirButton = Button(m_context)
        newDirButton.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        newDirButton.setText("New folder")
        newDirButton.setOnClickListener(View.OnClickListener { v: View? ->
            val input = EditText(m_context)
            input.setHint("Folder name")
            // Show new folder name input dialog
            AlertDialog.Builder(m_context)
                .setTitle("New folder name")
                .setView(input)
                .setPositiveButton(
                m_context.getResources().getText(R.string.global_ok),
                DialogInterface.OnClickListener { dialog: DialogInterface?, whichButton: Int ->
                    val newDir = input.getText()
                    val newDirName = newDir.toString()
                    // Create new directory
                    if (createSubDir(m_dir + "/" + newDirName)) {
                        // Navigate into the new directory
                        m_dir += "/" + newDirName
                        updateDirectory()
                    } else {
                        Toast.makeText(
                            m_context,
                            m_context.getResources().getText(R.string.failedtocreatefolder)
                                .toString() + " " + newDirName,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }).setNegativeButton(m_context.getResources().getText(R.string.global_cancel), null)
                .show()
        })

        if (!this.newFolderEnabled) {
            newDirButton.setVisibility(View.GONE)
        }

        titleLayout.addView(m_titleView)
        titleLayout.addView(newDirButton)

        dialogBuilder.setCustomTitle(titleLayout)

        m_listAdapter = createListAdapter(listItems)

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener)
        dialogBuilder.setCancelable(false)

        return dialogBuilder
    }

    private fun updateDirectory() {
        m_subdirs!!.clear()
        m_subdirs!!.addAll(getDirectories(m_dir!!))
        m_titleView!!.setText(m_dir)
        m_listAdapter!!.notifyDataSetChanged()

        //m_titleView.getContext().layout.select_dialog_item;
    }

    private fun createListAdapter(items: MutableList<String?>): ArrayAdapter<String?> {
        return object : ArrayAdapter<String?>(
            m_context,
            layout.simple_list_item_1, id.text1, items
        ) //.select_dialog_item
        {
            override fun getView(
                position: Int, convertView: View?,
                parent: ViewGroup
            ): View {
                val v = super.getView(position, convertView, parent)

                if (v is TextView) {
                    // Enable list item (directory) text wrapping
                    val tv = v
                    tv.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT
                    tv.setEllipsize(null)
                    tv.setTextAppearance(
                        m_context,
                        android.R.style.TextAppearance_DeviceDefault_Small
                    )
                }
                return v
            }
        }
    }

    /**/////////////////////////////////////////////////// */ // Callback interface for selected directory
    /**/////////////////////////////////////////////////// */
    interface ChosenFileListener {
        fun onChosenFile(chosenDir: String?)
    }
}
