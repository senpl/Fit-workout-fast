package com.easyfitness.graph

import android.content.Context
import androidx.core.content.ContextCompat
import com.easyfitness.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.EntryXComparator
import com.github.mikephil.charting.utils.Utils
import java.util.Collections

class DateGraph(context: Context?, chart: LineChart?, name: String?) {
    var chart: LineChart? = null
        private set
    private var mChartName: String? = null
    private var mContext: Context? = null

    init {
        this.chart = chart
        mChartName = name
        chart!!.setDoubleTapToZoomEnabled(true)
        chart.setHorizontalScrollBarEnabled(true)
        chart.setVerticalScrollBarEnabled(true)
        chart.setAutoScaleMinMaxEnabled(true)
        chart.setDrawBorders(true)

        val marker: IMarker = DateGraphMarkerView(
            chart.getContext(), R.layout.graph_markerview,
            this.chart
        )
        chart.setMarker(marker)

        mContext = context
        // get the legend (only possible after setting data)
        val l = chart.getLegend()
        l.isEnabled = false

        val xAxis = chart.getXAxis()
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setTextColor(ColorTemplate.getHoloBlue())
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(true)
        xAxis.setCenterAxisLabels(false)
        xAxis.setGranularity(1f) // 1 jour

        //        xAxis.setValueFormatter(new IAxisValueFormatter() {
//
//            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM"); // HH:mm:ss
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                //long millis = TimeUnit.HOURS.toMillis((long) value);
//                mFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Date tmpDate = new Date((long) DateConverter.nbMilliseconds(value)); // Convert days in milliseconds
//                return mFormat.format(tmpDate);
//            }
//        });
        val leftAxis = chart.getAxisLeft()
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.setTextColor(ColorTemplate.getHoloBlue())
        leftAxis.setDrawGridLines(true)
        leftAxis.setGranularityEnabled(true)
        leftAxis.setGranularity(0.5.toFloat())
        leftAxis.resetAxisMinimum()

        chart.getAxisRight().isEnabled = false
    }

    fun draw(entries: ArrayList<Entry?>) {
        chart!!.clear()
        if (entries.isEmpty()) {
            return
        }

        Collections.sort<Entry?>(entries, EntryXComparator())

        //Log.d("DEBUG", arrayToString(entries));
        val set1 = LineDataSet(entries, mChartName)
        set1.setLineWidth(3f)
        set1.setCircleRadius(4f)
        set1.setDrawFilled(true)
        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(mContext!!, R.drawable.fade_blue)
            set1.setFillDrawable(drawable)
        } else {
            set1.setFillColor(ColorTemplate.getHoloBlue())
        }
        set1.setFillAlpha(100)
        set1.setColor(mContext!!.getResources().getColor(R.color.toolbar_background))
        set1.setCircleColor(mContext!!.getResources().getColor(R.color.toolbar_background))

        // Create a data object with the datasets
        val data = LineData(set1)

        //        data.setValueFormatter(new IValueFormatter() {
//            private DecimalFormat mFormat = new DecimalFormat("#.##");
//
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                return mFormat.format(value);
//            }
//        });

        // Set data
        chart!!.setData(data)

        chart!!.invalidate()

        //mChart.animateY(500, Easing.EasingOption.EaseInBack);    //refresh graph
    }

    private fun arrayToString(entries: ArrayList<Entry?>): String {
        val output = StringBuilder()
        val delimiter = "\n" // Can be new line \n tab \t etc...
        for (i in entries.indices) {
            output.append(entries.get(i)!!.y).append(" / ").append(entries.get(i)!!.x)
                .append(delimiter)
        }

        return output.toString()
    }

    fun setZoom(z: zoomType) {
        when (z) {
            zoomType.ZOOM_ALL -> chart!!.fitScreen()
            zoomType.ZOOM_WEEK -> {
                chart!!.fitScreen()
                if (chart!!.getData() != null) {
                    chart!!.setVisibleXRangeMaximum(7f) // allow 20 values to be displayed at once on the x-axis, not more
                    chart!!.moveViewToX(
                        chart!!.getData()!!.getXMax() + (1 - 7)
                    ) // set the left edge of the chart to x-index 10
                }
            }

            zoomType.ZOOM_MONTH -> {
                chart!!.fitScreen()
                if (chart!!.getData() != null) {
                    chart!!.setVisibleXRangeMaximum(30f) // allow 30 values to be displayed at once on the x-axis, not more
                    chart!!.moveViewToX(
                        chart!!.getData()!!.getXMax() + (1 - 30).toFloat()
                    ) // set the left edge of the chart to x-index 10
                }
            }

            zoomType.ZOOM_YEAR -> {
                chart!!.fitScreen()
                if (chart!!.getData() != null) {
                    chart!!.setVisibleXRangeMaximum(365f) // allow 365 values to be displayed at once on the x-axis, not more
                    chart!!.moveViewToX(
                        chart!!.getData()!!.getXMax() + (1 - 365).toFloat()
                    ) // set the left edge of the chart to x-index 10
                }
            }
        }

        // refresh
        chart!!.invalidate()
    }

    enum class zoomType {
        ZOOM_ALL, ZOOM_YEAR, ZOOM_MONTH, ZOOM_WEEK
    }
}
