package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.Map;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Monitors;
import org.metawatch.manager.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.TextPaint;

public class PhoneStatusWidget implements InternalWidget {

	public final static String id_0 = "phoneStatus_24_32";
	final static String desc_0 = "Phone Status (24x32)";
	
	private Context context;
	private TextPaint paintSmall;
		
	public void init(Context context, ArrayList<CharSequence> widgetIds) {
		this.context = context;
		
		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		paintSmall.setTextAlign(Align.CENTER);

	}

	public void shutdown() {
		paintSmall = null;
	}

	public void refresh(ArrayList<CharSequence> widgetIds) {
	}

	public void get(ArrayList<CharSequence> widgetIds, Map<String,WidgetData> result) {

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
		
		Bitmap icon = Utils.loadBitmapFromAssets(context, "idle_phone_status.bmp");

		int level = Monitors.BatteryData.level;
		String count = level==-1 ? "-" : level+"%";

		widget.priority = level==-1 ? 0 : 1;		
		widget.bitmap = Bitmap.createBitmap(widget.width, widget.height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(widget.bitmap);
		canvas.drawColor(Color.WHITE);
		
		canvas.drawBitmap(icon, 0, 3, null);
		canvas.drawText(count, 12, 29,  paintSmall);
	
		if(level>95)
			level=100;
		if(level>-1)
			canvas.drawRect(13, 8 + ((100-level)/10), 19, 18, paintSmall);
		
		return widget;
	}
}
	