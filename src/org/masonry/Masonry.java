package org.masonry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class Masonry extends ViewGroup {
	
	private int containerHeight;
	
	private int containerWidth;

	private int columnWidth;
	
	private int gutter;
	
	private int cols;
	
	private List<Integer> colYs;
	
	public Masonry(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Masonry);
		gutter = a.getDimensionPixelSize(R.styleable.Masonry_gutter, 0);
		columnWidth = a.getDimensionPixelSize(R.styleable.Masonry_columnWidth, 0);
		a.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpec;
		if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED){
			widthSpec = MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY);
		}else{
			widthSpec = widthMeasureSpec;
		}
		int heightSpec;
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED){
			heightSpec = MeasureSpec.makeMeasureSpec(containerHeight, MeasureSpec.EXACTLY);
		}else{
			heightSpec = heightMeasureSpec;
		}
		super.onMeasure(widthSpec, heightSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		containerWidth = getWidth();
		measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		List<View> bricks = new ArrayList<View>();
		for(int i=0; i<getChildCount(); i++){
			bricks.add(getChildAt(i));
		}
		if(columnWidth == 0){
			columnWidth = (bricks.isEmpty()?containerWidth:bricks.get(0).getMeasuredWidth()) + gutter;
		}
		cols = Math.max(Double.valueOf(Math.floor((double)(containerWidth + gutter)
			/columnWidth)).intValue(), 1);
		colYs = new ArrayList<Integer>();
		for (int i=0; i<cols; i++) {
			colYs.add(0);
		}
		List<Style> styleQueue = new ArrayList<Style>();
		for(View brick : bricks){
			int colSpan = Math.min(Double.valueOf(Math.ceil((double)brick.getMeasuredWidth()
				/ columnWidth)).intValue(), cols);
			if(colSpan == 1){
				placeBrick(brick, colYs, styleQueue);
			}else{
				int groupCount = cols - colSpan + 1;
				List<Integer> groupY = new ArrayList<Integer>();
				for (int i=0; i < groupCount; i++) {
		            groupY.add(Collections.max(colYs.subList(i, i + colSpan)));
				}
				placeBrick(brick, groupY, styleQueue);
			}
		}
		containerHeight = Collections.max(colYs);
		for (Style s : styleQueue) {
			Position position = s.position;
			int left = position.left;
			int top = position.top;
			View brick = s.brick;
			brick.layout(left, top, left + brick.getMeasuredWidth(), top + brick.getMeasuredHeight());
		}
	}
	
	private void placeBrick(View brick, List<Integer> y, List<Style> styleQueue){
		int minimumY = Collections.min(y);
	    int shortCol = 0;
	    int len = y.size();
	    for(int i=0; i<len; i++){
	    	if(y.get(i) == minimumY){
	    		shortCol = i;
	    		break;
	        }
	    }
	    Position position = new Position();
	    position.top = minimumY;
	    position.left = columnWidth * shortCol;
	    Style style = new Style();
	    style.brick = brick;
	    style.position = position; 
	    styleQueue.add(style);
	    int height = minimumY + gutter + brick.getLayoutParams().height;
        int span = cols + 1 - len;
		for(int i=0; i<span; i++){
			colYs.set(shortCol + i, height);
		}
	}
	
	private static class Style {
		View brick;
		Position position;
	}
	
	private static class Position {
		int top;
		int left;
	}
	
}
