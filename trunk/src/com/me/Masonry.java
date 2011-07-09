package com.me;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class Masonry extends ViewGroup {
	
	private int margin;
	
	private int measuredHeight;
	
	private int measuredWidth;
	
	public Masonry(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Masonry(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Masonry);
		margin = a.getDimensionPixelSize(R.styleable.Masonry_margin, 0);
		a.recycle();
	}

	public Masonry(Context context) {
		super(context);
	}
	
	public void setMargin(int margin){
		this.margin = margin;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSpec;
		if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED){
			widthSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
		}else{
			widthSpec = widthMeasureSpec;
		}
		int heightSpec;
		if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED){
			heightSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);
		}else{
			heightSpec = heightMeasureSpec;
		}
		super.onMeasure(widthSpec, heightSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		measuredWidth = getWidth();
		Offset currentOffset = new Offset();
		List<Layout> layouts = new ArrayList<Layout>();
		for(int i=0; i<getChildCount(); i++){
			View child = getChildAt(i);
			LayoutParams size = child.getLayoutParams();
			if(i > 0){
				Layout lastLayout = layouts.get(i - 1);
				int leftOffset = lastLayout.right  + margin;
				int childWidth = size.width; 
				if(leftOffset + childWidth <= measuredWidth){
					currentOffset.left = leftOffset;
				}else{
					Set<Integer> bottoms = new HashSet<Integer>();
					for(Layout layout : layouts){
						bottoms.add(layout.bottom);
					}
					Integer maxBottom = Collections.max(bottoms);
					boolean floated = false;
					for(int j=0; j<i;j++){
						int backIdx = i - 1 -j;
						Layout backLayout = layouts.get(backIdx);
						int backLeft = backLayout.left;
						if(backLayout.bottom == maxBottom){
							break;
						}else if(backLeft + margin + childWidth <= measuredWidth){
							if(currentOffset.left >= backLeft){
								currentOffset.left = backLeft;
								currentOffset.top = backLayout.bottom + margin;
								floated = true;
							}
						}
					}
					if(!floated){
						currentOffset.left = 0;
						currentOffset.top = maxBottom + margin;
					}
				}
			}
			Layout layout = new Layout();
			layout.left = currentOffset.left;
			layout.top =  currentOffset.top;
			layout.right = layout.left + size.width;
			layout.bottom = layout.top + size.height;
			layouts.add(layout);
			child.layout(layout.left, layout.top, layout.right, layout.bottom);
		}
		Set<Integer> bottoms = new HashSet<Integer>();
		for (Layout layout : layouts) {
			bottoms.add(layout.bottom);
		}
		measuredHeight = Collections.max(bottoms);
	}
	
	private static class Offset {
		int left;
		int top;
	}
	
	private static class Layout extends Offset {
		int right;
		int bottom;
	}

}
