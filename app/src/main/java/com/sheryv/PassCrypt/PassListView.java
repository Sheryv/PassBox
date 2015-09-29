package com.sheryv.PassCrypt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

/**
 * Created by Sheryv on 20.09.2015.
 */
public class PassListView extends ExpandableListView
{

        boolean expanded = true;

        public PassListView(Context context) {
            super(context);
        }

        public PassListView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public PassListView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public boolean isExpanded()
        {
            return expanded;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (isExpanded())
            {
                // Calculate entire height by providing a very large height hint.
                // View.MEASURED_SIZE_MASK represents the largest height possible.
                int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
                super.onMeasure(widthMeasureSpec, expandSpec);

                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = getMeasuredHeight();
            }else{
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        public void setExpanded(boolean expanded)
        {
            this.expanded = expanded;
        }

    }