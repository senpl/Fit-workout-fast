package com.easyfitness

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.easyfitness.utils.ImageUtil
import com.mikhaellopez.circularimageview.CircularImageView

class CustomDrawerAdapter(
    context: Context, var layoutResID: Int,
    var drawerItemList: MutableList<DrawerItem>
) : ArrayAdapter<DrawerItem?>(
    context,
    layoutResID,
    drawerItemList
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val drawerHolder: DrawerItemHolder
        var view = convertView

        if (view == null) {
            val inflater = (context as Activity).getLayoutInflater()
            drawerHolder = DrawerItemHolder()

            view = inflater.inflate(layoutResID, parent, false)
            drawerHolder.ItemName = view.findViewById<TextView>(R.id.drawer_itemName)
            drawerHolder.icon = view.findViewById<ImageView>(R.id.drawer_icon)

            drawerHolder.spinner = view.findViewById<Spinner>(R.id.drawerSpinner)

            drawerHolder.title = view.findViewById<TextView>(R.id.drawerTitle)

            drawerHolder.headerLayout = view.findViewById<RelativeLayout>(R.id.headerLayout)
            drawerHolder.itemLayout = view.findViewById<RelativeLayout>(R.id.itemLayout)
            drawerHolder.spinnerLayout = view.findViewById<LinearLayout>(R.id.spinnerLayout)
            drawerHolder.roundProfile = view.findViewById<CircularImageView>(R.id.header_icon)

            view.setTag(drawerHolder)
        } else {
            drawerHolder = view.getTag() as DrawerItemHolder
        }

        val dItem = this.drawerItemList.get(position)
        if (dItem.isSpinner) {
            drawerHolder.headerLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.itemLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.spinnerLayout!!.setVisibility(LinearLayout.VISIBLE)

            val userList: MutableList<SpinnerItem?> = ArrayList<SpinnerItem?>()

            userList.add(
                SpinnerItem(
                    R.drawable.ic_person_black_24dp, "bloop",
                    "bloop@gmail.com"
                )
            )
            userList.add(
                SpinnerItem(
                    R.drawable.ic_person_black_24dp, "blip",
                    "blip@gmail.com"
                )
            )

            val adapter = CustomSpinnerAdapter(context, R.layout.custom_spinner_item,
                userList as MutableList<SpinnerItem>
            )

            drawerHolder.spinner!!.setAdapter(adapter)

            drawerHolder.spinner!!.setOnItemSelectedListener(object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    arg0: AdapterView<*>?,
                    arg1: View?, arg2: Int, arg3: Long
                ) {
                    Toast.makeText(
                        context, context.getResources().getString(R.string.userChanged),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {
                }
            })
        } else if (dItem.title != null) {
            drawerHolder.headerLayout!!.setVisibility(LinearLayout.VISIBLE)
            drawerHolder.itemLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.spinnerLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.title!!.setText(dItem.title)

            //drawerHolder.icon = view.findViewById(R.id.header_icon);

            //drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
            val imgUtil = ImageUtil()
            // Check if path is pointing to a thumb else create it and use it.
//            val thumbPath = imgUtil.getThumbPath(dItem.getImg())
//            if (thumbPath != null) ImageUtil.setPic(drawerHolder.roundProfile, thumbPath)
//            else drawerHolder.roundProfile!!.setImageDrawable(
//                view.getResources().getDrawable(dItem.imgResID)
//            )
        } else {
            drawerHolder.headerLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.spinnerLayout!!.setVisibility(LinearLayout.GONE)
            drawerHolder.itemLayout!!.setVisibility(LinearLayout.VISIBLE)

            drawerHolder.icon!!.setImageDrawable(context.getDrawable(dItem.imgResID))

            if (!dItem.isActive) {
                drawerHolder.ItemName!!.setAlpha(0.5.toFloat())
                drawerHolder.ItemName!!.setText(dItem.itemName + "(soon)")
            } else {
                drawerHolder.ItemName!!.setText(dItem.itemName)
            }

            //Log.d("Getview", "Passed5");
        }
        return view
    }

    private class DrawerItemHolder {
        var ItemName: TextView? = null
        var title: TextView? = null
        var icon: ImageView? = null
        var roundProfile: CircularImageView? = null
        var headerLayout: RelativeLayout? = null
        var itemLayout: RelativeLayout? = null
        var spinnerLayout: LinearLayout? = null
        var spinner: Spinner? = null
    }
}
