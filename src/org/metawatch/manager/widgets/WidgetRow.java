package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.Map;

import org.metawatch.manager.widgets.InternalWidget.WidgetData;

import android.graphics.Canvas;

public class WidgetRow {
	
	ArrayList<CharSequence> widgetIDs = new ArrayList<CharSequence>();
	
	ArrayList<WidgetData> widgets = null;
	int totalWidth = 0;
	int totalHeight = 0;
	
	public void add(String id) {
		widgetIDs.add(id);
	}
	
	public ArrayList<CharSequence> getIds() {
		return widgetIDs;
	}
	
	public void doLayout(Map<String,WidgetData> widgetData) {
		widgets = new ArrayList<WidgetData>();
		
		totalWidth = 0;
		for( CharSequence id : widgetIDs ) {
			WidgetData widget = widgetData.get(id);
			if(widget!=null && widget.bitmap!=null && widget.priority>-1) {
				widgets.add(widget);
				totalWidth += widget.width;
			}
		}
		
		// Cull widgets to fit
		while(totalWidth>96) {
			int lowestPri = Integer.MAX_VALUE;
			int cull = -1;
			for(int i=0; i<widgets.size(); ++i) {
				int pri = widgets.get(i).priority;
				if(pri <= lowestPri) {
					cull = i;
					lowestPri = pri;
				}
			}
			if(cull>-1)
			{
				totalWidth-=widgets.get(cull).width;
				widgets.remove(cull);
			}
			else
			{
				widgets = null;
				return;
			}
		}
		
		totalHeight = 0;
		for(WidgetData widget : widgets)
			totalHeight = Math.max(totalHeight, widget.height);
	}
	
	public int getWidth() {
		if(widgets==null)
			return 0;
		return totalWidth;
	}
	
	public int getHeight() {
		if(widgets==null)
			return 0;
		return totalHeight;
	}
	
	public void draw(Map<String,WidgetData> widgetData, Canvas canvas, int y)
	{
		if (widgets==null)
			return;
		
		int space = (96-totalWidth)/(widgets.size()+1);		
		int x=space;
		for(WidgetData widget : widgets) {
			canvas.drawBitmap(widget.bitmap, x, y, null);
			x += (space+widget.width);
		}	
	}
}
