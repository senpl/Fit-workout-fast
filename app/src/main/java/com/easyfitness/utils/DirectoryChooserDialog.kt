package com.easyfitness.utils

import android.R.id
import android.R.layout
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Environment
import android.view.Gravity
import android.view.KeyEvent
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

//DirectoryChooserDialog.java

class DirectoryChooserDialog(
    private val m_context: Context,
    chosenDirectoryListener: ChosenDirectoryListener?
) {
    /**//////////////////////////////////////////////////////////////////// */ // setNewFolderEnabled() - enable/disable new folder button
    /**//////////////////////////////////////////////////////////////////// */
    var newFolderEnabled: Boolean = true
    private var m_sdcardDirectory = ""
    private var m_titleView: TextView? = null

    private var m_dir: String? = ""
    private var m_subdirs: MutableList<String?>? = null
    private var m_chosenDirectoryListener: ChosenDirectoryListener? = null
    private var m_listAdapter: ArrayAdapter<String?>? = null

    init {
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()
        m_chosenDirectoryListener = chosenDirectoryListener

        try {
            m_sdcardDirectory = File(m_sdcardDirectory).getCanonicalPath()
        } catch (ignored: IOException) {
        }
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
                // Navigate into the sub-directory
                m_dir += "/" + (dialog as AlertDialog).getListView().getAdapter().getItem(item)
                updateDirectory()
            }
        }

        val dialogBuilder =
            createDirectoryChooserDialog(dir, m_subdirs!!, DirectoryOnClickListener())

        dialogBuilder.setPositiveButton(
            "OK",
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // Current directory chosen
                if (m_chosenDirectoryListener != null) {
                    // Call registered listener supplied with the chosen directory
                    m_chosenDirectoryListener!!.onChosenDir(m_dir)
                }
            }).setNegativeButton("Cancel", null)

        val dirsDialog = dialogBuilder.create()

        dirsDialog.setOnKeyListener(DialogInterface.OnKeyListener { dialog: DialogInterface?, keyCode: Int, event: KeyEvent? ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event!!.getAction() == KeyEvent.ACTION_DOWN) {
                // Back button pressed
                if (m_dir == m_sdcardDirectory) {
                    // The very top level directory, do nothing
                    return@OnKeyListener false
                } else {
                    // Navigate back to an upper directory
                    m_dir = File(m_dir).getParent()
                    updateDirectory()
                }

                return@OnKeyListener true
            } else {
                return@OnKeyListener false
            }
        })

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

    private fun getDirectories(dir: String): MutableList<String?> {
        val dirs: MutableList<String?> = ArrayList<String?>()

        try {
            val dirFile = File(dir)
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs
            }

            for (file in dirFile.listFiles()) {
                if (file.isDirectory()) {
                    dirs.add(file.getName())
                }
            }
        } catch (ignored: Exception) {
        }

        Collections.sort<String?>(
            dirs,
            Comparator { obj: String?, anotherString: String? -> obj!!.compareTo(anotherString!!) })

        return dirs
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
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        m_titleView!!.setTextAppearance(m_context, android.R.style.TextAppearance_Large)
        m_titleView!!.setTextColor(m_context.getResources().getColor(android.R.color.white))
//        m_titleView!!.setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL)
        m_titleView!!.setText(title)

        val newDirButton = Button(m_context)
        newDirButton.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        newDirButton.setText(m_context.getString(R.string.new_folder))
        newDirButton.setOnClickListener(View.OnClickListener { v: View? ->
            val input = EditText(m_context)
            // Show new folder name input dialog
//            AlertDialog.Builder(m_context).setTitle("New folder name").setView
////            (input).setPosi
//                m_context.getString(R.string.global_ok),
//                DialogInterface.OnClickListener { dialog: DialogInterface?, whichButton: Int ->
//                    val newDir = input.getText()
//                    val newDirName = newDir.toString()
//                    // Create new directory
//                    if (createSubDir(m_dir + "/" + newDirName)) {
//                        // Navigate into the new directory
//                        m_dir += "/" + newDirName
//                        updateDirectory()
//                    } else {
//                        Toast.makeText(
//                            m_context, "Failed to create '" + newDirName +
//                                    "' folder", Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }).setNegativeButton(m_context.getString(R.string.global_cancel), null).show()
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
    }

    private fun createListAdapter(items: MutableList<String?>): ArrayAdapter<String?> {
        return object : ArrayAdapter<String?>(
            m_context,
            layout.select_dialog_item, id.text1, items
        ) {
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
                }
                return v
            }
        }
    }

    /**/////////////////////////////////////////////////// */ // Callback interface for selected directory
    /**/////////////////////////////////////////////////// */
    interface ChosenDirectoryListener {
        fun onChosenDir(chosenDir: String?)
    }
}

