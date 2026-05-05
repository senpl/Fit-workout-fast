package com.easyfitness.graph

import android.content.Context
import com.easyfitness.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.Collections

class BarGraph(context: Context?, chart: BarChart?, name: String?) {
    var chart: BarChart? = null
        private set
    private var mChartName: String? = null
    private var mContext: Context? = null

    init {
        this.chart = chart
        mChartName = name
        //mChart.setDoubleTapToZoomEnabled(true);
        chart!!.setHorizontalScrollBarEnabled(true)
        chart.setVerticalScrollBarEnabled(true)
        //mChart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawBorders(true)

        //IMarker marker = new BarGraphMarkerView(mChart.getContext(), R.layout.graph_markerview, mChart);
        //mChart.setMarker(marker);
        mContext = context
        // get the legend (only possible after setting data)
        val l = chart.getLegend()
        l.isEnabled = false

        val xAxis = chart.getXAxis()
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setTextColor(ColorTemplate.getHoloBlue())
        xAxis.setDrawAxisLine(false)
        //xAxis.setDrawGridLines(true);
        //xAxis.setCenterAxisLabels(false);
        xAxis.setGranularityEnabled(true)
        xAxis.setGranularity(1f)

        /*xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM"); // HH:mm:ss

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //long millis = TimeUnit.HOURS.toMillis((long) value);
                mFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = new Date((long) DateConverter.nbMilliseconds(value)); // Convert days in milliseconds
                return mFormat.format(tmpDate);
            }
        });*/
        val leftAxis = chart.getAxisLeft()
        leftAxis.setAxisMinimum(0f)
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.setTextColor(ColorTemplate.getHoloBlue())
        leftAxis.setGranularityEnabled(true)
        leftAxis.setGranularity(1f)

        chart.setFitBars(true)
        leftAxis.setAxisMinimum(0f)

        chart.getAxisRight().isEnabled = false
    }

    fun draw(entries: MutableList<BarEntry?>, xAxisLabel: ArrayList<String?>?) {
        chart!!.clear()
        if (entries.isEmpty()) {
            return
        }

        val xAxis = this.chart!!.getXAxis()
//        xAxis.setValueFormatter(IndexAxisValueFormatter(xAxisLabel))
//        xAxis.valueFormatter(IndexAxisValueFormatter(xAxisLabel))

        Collections.sort<BarEntry?>(entries, EntryXComparator())

        //Log.d("DEBUG", arrayToString(entries));
        val set1 = BarDataSet(entries, mChartName)
        set1.setColor(mContext!!.getResources().getColor(R.color.toolbar_background))

        // Create a data object with the datasets
        val data = BarData(set1)

        data.setValueTextSize(12f)

        //        data.setValueFormatter(new IValueFormatter() {
//            private DecimalFormat mFormat = new DecimalFormat("#.## kg");
//
//            @Override
//            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//                return mFormat.format(value);
//            }
//        });

        // Set data
        chart!!.setData(data)

        chart!!.getAxisLeft().setAxisMinimum(0f)

        chart!!.invalidate()

        //mChart.animateY(500, Easing.EasingOption.EaseInBack);    //refresh graph
    }
}
