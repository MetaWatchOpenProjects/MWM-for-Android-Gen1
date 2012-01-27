package org.metawatch.manager.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.TextPaint;

import java.util.Dictionary;
import java.util.List;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Utils;

public class SmsWidget implements InternalWidget {

	public final static String id_0 = "unreadSms_24_32";
	final static String desc_0 = "Unread SMS (24x32)";
	
	private Context context;
	private TextPaint paintSmall;
	
	public SmsWidget(Context context) {
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
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_0;
			widget.description = desc_0;
			widget.width = 24;
			widget.height = 32;
			
			widget.bitmap = draw0();
			
			result.put(widget.id, widget);
		}
	}
	
	private Bitmap draw0() {
		Bitmap bitmap = Bitmap.createBitmap(24, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_sms.bmp"), 0, 0, null);
		
		String count = Integer.toString(Utils.getUnreadSmsCount(context));
		
		canvas.drawText(count, 12, 25, paintSmall);
		
		return bitmap;
	}
}
