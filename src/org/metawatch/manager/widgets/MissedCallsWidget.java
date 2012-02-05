package org.metawatch.manager.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.TextPaint;

import java.util.Dictionary;
import java.util.List;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Utils;

public class MissedCallsWidget implements InternalWidget {

	public final static String id_0 = "missedCalls_24_32";
	final static String desc_0 = "Missed Calls (24x32)";
	
	private Context context;
	private TextPaint paintSmall;
	
	public MissedCallsWidget(Context context) {
		this.context = context;
	}
	
	public void init(List<String> widgetIds) {

		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		paintSmall.setTextAlign(Align.CENTER);

	}

	public void shutdown() {
		paintSmall = null;
	}

	public void refresh(List<String> widgetIds) {
	}

	public void get(List<String> widgetIds, Dictionary<String,WidgetData> result) {

		if(widgetIds == null || widgetIds.contains(id_0)) {
			result.put(id_0, GenWidget(id_0));
		}
	}
	
	private InternalWidget.WidgetData GenWidget(String widget_id) {
		InternalWidget.WidgetData widget = new InternalWidget.WidgetData();

		widget.id = id_0;
		widget.description = desc_0;
		widget.width = 24;
		widget.height = 32;
		
		Bitmap icon = Utils.loadBitmapFromAssets(context, "idle_call.bmp");

		int count = Utils.getMissedCallsCount(context);

		widget.priority = count;		
		widget.bitmap = Utils.DrawIconCountWidget(context, widget.width, widget.height, icon, count, paintSmall);
		
		return widget;
	}

}
