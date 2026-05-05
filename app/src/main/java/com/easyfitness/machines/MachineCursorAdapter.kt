package com.easyfitness.machines

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.R
import com.easyfitness.utils.ImageUtil
import com.github.ivbaranov.mfb.MaterialFavoriteButton

class MachineCursorAdapter(context: Context, c: Cursor?, flags: Int, pDbMachine: DAOMachine?) :
    CursorAdapter(context, c, flags), Filterable {
    var mDbMachine: DAOMachine? = null
    var iFav: MaterialFavoriteButton? = null
    private val mInflater: LayoutInflater

    init {
        mDbMachine = pDbMachine
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val t0 = view.findViewById<TextView>(R.id.LIST_MACHINE_ID)
        t0.setText(cursor.getString(cursor.getColumnIndex(DAOMachine.KEY)))

        val t1 = view.findViewById<TextView>(R.id.LIST_MACHINE_NAME)
        t1.setText(cursor.getString(cursor.getColumnIndex(DAOMachine.NAME)))

        val t2 = view.findViewById<TextView>(R.id.LIST_MACHINE_SHORT_DESCRIPTION)
        t2.setText(cursor.getString(cursor.getColumnIndex(DAOMachine.DESCRIPTION)))

        val i0 = view.findViewById<ImageView>(R.id.LIST_MACHINE_PHOTO)
        val lPath = cursor.getString(cursor.getColumnIndex(DAOMachine.PICTURE))

        val lType = cursor.getInt(cursor.getColumnIndex(DAOMachine.TYPE))

        if (lPath != null && !lPath.isEmpty()) {
            try {
                val imgUtil = ImageUtil()
                val lThumbPath = imgUtil.getThumbPath(lPath)
                ImageUtil.setThumb(i0, lThumbPath)
            } catch (e: Exception) {
                if (lType == DAOMachine.TYPE_STRENGTH) {
                    i0.setImageResource(R.drawable.ic_gym_bench_50dp)
                } else if (lType == DAOMachine.TYPE_STATIC) {
                    i0.setImageResource(R.drawable.ic_static)
                } else {
                    i0.setImageResource(R.drawable.ic_training_white_50dp)
                    i0.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
                }
                e.printStackTrace()
            }
        } else {
            if (lType == DAOMachine.TYPE_STRENGTH) {
                i0.setImageResource(R.drawable.ic_gym_bench_50dp)
            } else if (lType == DAOMachine.TYPE_STATIC) {
                i0.setImageResource(R.drawable.ic_static)
            } else {
                i0.setImageResource(R.drawable.ic_training_white_50dp)
            }

            i0.setScaleType(ImageView.ScaleType.CENTER_INSIDE)
        }

        iFav = view.findViewById<MaterialFavoriteButton?>(R.id.LIST_MACHINE_FAVORITE)
        val bFav = cursor.getInt(6) == 1
        iFav!!.setFavorite(bFav)
        iFav!!.setRotationDuration(500)
        iFav!!.setAnimateFavorite(true)
        iFav!!.setTag(cursor.getLong(0))

        iFav!!.setOnClickListener(View.OnClickListener { v: View? ->
            val mFav = v as MaterialFavoriteButton
            val t = mFav.isFavorite()
            mFav.setFavoriteAnimated(!t)
            if (mDbMachine != null) {
                val m = mDbMachine!!.getMachine(mFav.getTag() as Long)
                m?.favorite =!t
                mDbMachine!!.updateMachine(m)
            }
        })
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View? {
        return mInflater.inflate(R.layout.machinelist_row, parent, false)
    }
}
