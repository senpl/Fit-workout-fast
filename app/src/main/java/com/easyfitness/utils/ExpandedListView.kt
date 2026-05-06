package com.easyfitness.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

class ExpandedListView : ListView {
    //    private ViewGroup.LayoutParams params;
    //    private int oldCount = 0;
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec

        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec =
                MeasureSpec.makeMeasureSpec(Int.Companion.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        }

        super.onMeasure(widthMeasureSpec, heightSpec)
    } /*
    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != oldCount && getCount() != 0) {
            int height = getChildAt(0).getHeight() + 1;
            oldCount = getCount();
            params = getLayoutParams();
            params.height = getCount() * height;
            setLayoutParams(params);
            if (getCount() != oldCount) {
                params = getLayoutParams();
                oldCount = getCount();
                int totalHeight = 0;
                for (int i = 0; i < getCount(); i++) {
                    this.measure(0, 0);
                    totalHeight += getMeasuredHeight();
                }

                params = getLayoutParams();
                params.height = totalHeight + (getDividerHeight() * (getCount() - 1));
                setLayoutParams(params);
            }

            super.onDraw(canvas);
        }

        super.onDraw(canvas);
    }
*/
}
