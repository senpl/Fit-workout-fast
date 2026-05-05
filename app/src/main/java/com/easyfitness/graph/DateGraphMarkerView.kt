package com.easyfitness.graph

import android.content.Context
import android.graphics.Canvas
import android.widget.TextView
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.DecimalFormat
import java.util.Date
import java.util.TimeZone

class DateGraphMarkerView(context: Context?, layoutResource: Int, chart: Chart<*>?) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView
    private val tvDate: TextView
    private val mFormat = DecimalFormat("#.##")
    private var lineChart: Chart<*>? = null

    /**
     * Screen width in pixels.
     */
    private val uiScreenWidth: Int
    private var mOffset: MPPointF? = null

    init {
        // find your layout components
        tvContent = findViewById<TextView>(R.id.tvContent)
        tvDate = findViewById<TextView>(R.id.tvDate)
        uiScreenWidth = getResources().getDisplayMetrics().widthPixels
        lineChart = chart
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        val dateFormat3 =
            android.text.format.DateFormat.getDateFormat(getContext().getApplicationContext())
        dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"))
        tvDate.setText(
            dateFormat3.format(
                Date(
                    DateConverter.nbMilliseconds(e.x.toDouble()).toLong()
                )
            )
        )
        tvContent.setText(mFormat.format(e.y.toDouble()))

        // this will perform necessary layouting
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF(-(getWidth() / 2).toFloat(), -getHeight().toFloat())
        }

        return mOffset!!
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        // take offsets into consideration
        var posX = posX
        var posY = posY
        var lineChartWidth = 0
        var lineChartHeight = 0
        var offsetX = getOffset().getX()
        val offsetY = getOffset().getY()

        val width = getWidth().toFloat()
        val height = getHeight().toFloat()

        if (lineChart != null) {
            lineChartWidth = lineChart!!.getWidth()
            lineChartHeight = lineChart!!.getHeight()
        }

        //Si ca deborde sur les cotés
        if (posX + offsetX < 0) {
            offsetX = -posX
        } else if (posX + width + offsetX > lineChartWidth) {
            offsetX = lineChartWidth - posX - width
        }
        posX += offsetX

        // Si ca deborde en haut ou en bas
        if (posY + offsetY < 0) {
            posY = posY + 20
        } else if (posY + height + offsetY > lineChartHeight) {
            posY += lineChartHeight - posY - height
        } else {
            posY += offsetY
        }

        // translate to the correct position and draw
        canvas.translate(posX, posY)
        draw(canvas)
        canvas.translate(-posX, -posY)
    }
}
