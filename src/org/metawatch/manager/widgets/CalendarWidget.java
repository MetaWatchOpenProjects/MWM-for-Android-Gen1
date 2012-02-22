package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class CalendarWidget implements InternalWidget {

	public final static String id_0 = "Calendar_24_32";
	final static String desc_0 = "Next Calendar Appointment (24x32)";
	
	public final static String id_1 = "Calendar_96_32";
	final static String desc_1 = "Next Calendar Appointment (96x32)";
	
	private Context context;
	private TextPaint paintSmall;
	private TextPaint paintNumerals;

	private String meetingTime = "None";
	
	public void init(Context context, ArrayList<CharSequence> widgetIds) {
		this.context = context;
		
		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		paintSmall.setTextAlign(Align.CENTER);
		
		paintNumerals = new TextPaint();
		paintNumerals.setColor(Color.BLACK);
		paintNumerals.setTextSize(FontCache.instance(context).Numerals.size);
		paintNumerals.setTypeface(FontCache.instance(context).Numerals.face);
		paintNumerals.setTextAlign(Align.CENTER);
	}

	public void shutdown() {
		paintSmall = null;
	}

	public void refresh(ArrayList<CharSequence> widgetIds) {
		meetingTime = Utils.readCalendar(context, 0);
	}

	public void get(ArrayList<CharSequence> widgetIds, Map<String,WidgetData> result) {

		if(widgetIds == null || widgetIds.contains(id_0)) {		
			result.put(id_0, GenWidget(id_0));
		}
		
		if(widgetIds == null || widgetIds.contains(id_1)) {		
			result.put(id_1, GenWidget(id_1));
		}
	}
	
	private InternalWidget.WidgetData GenWidget(String widget_id) {
		InternalWidget.WidgetData widget = new InternalWidget.WidgetData();

		widget.priority = meetingTime.equals("None") ? 0 : 1;	
		
		if (widget_id.equals(id_0)) {
			widget.id = id_0;
			widget.description = desc_0;
			widget.width = 24;
			widget.height = 32;
		}
		else if (widget_id.equals(id_1)) {
			widget.id = id_1;
			widget.description = desc_1;
			widget.width = 96;
			widget.height = 32;
		}
							
		Bitmap icon = Utils.loadBitmapFromAssets(context, "idle_calendar.bmp");
		
		widget.bitmap = Bitmap.createBitmap(widget.width, widget.height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(widget.bitmap);
		canvas.drawColor(Color.WHITE);
		
		canvas.drawBitmap(icon, 0, 3, null);
		Calendar c = Calendar.getInstance(); 
		String day = ""+c.get(Calendar.DAY_OF_MONTH);
		canvas.drawText(day, 12, 16, paintNumerals);
		canvas.drawText(meetingTime, 12, 29, paintSmall);
			
		if (widget_id.equals(id_1)) {
			paintSmall.setTextAlign(Align.LEFT);
			
			String text = Utils.Meeting_Title;
			if (Utils.Meeting_Location.length()>0)
				text += " - " + Utils.Meeting_Location;
			
			canvas.save();			
			StaticLayout layout = new StaticLayout(text, paintSmall, 70, Layout.Alignment.ALIGN_CENTER, 1.2f, 0, false);
			int height = layout.getHeight();
			int textY = 16 - (height/2);
			if(textY<0) {
				textY=0;
			}
			canvas.translate(25, textY); //position the text
			layout.draw(canvas);
			canvas.restore();	
			
			paintSmall.setTextAlign(Align.CENTER);
		}
		
		return widget;
	}
	


}
