package org.metawatch.manager.widgets;

import java.util.Dictionary;
import java.util.List;

import org.metawatch.manager.FontCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.TextPaint;

public class TestWidget implements InternalWidget {

	public final static String id_0 = "Test_24_32";
	final static String desc_0 = "Test (24x32)";
	
	public final static String id_1 = "Test_96_32";
	final static String desc_1 = "Test (96x32)";
	
	public final static String id_2 = "Test_64_96";
	final static String desc_2 = "Test (64x96)";
	
	public final static String id_3 = "Test_96_96";
	final static String desc_3 = "Test (96x96)";
	
	private Context context;
	private TextPaint paintSmall;
	private TextPaint paintLarge;
	
	public TestWidget(Context context) {
		this.context = context;
	}
	
	public void init(List<String> widgetIds) {

		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		paintSmall.setTextAlign(Align.CENTER);

		paintLarge = new TextPaint();
		paintLarge.setColor(Color.BLACK);
		paintLarge.setTextSize(FontCache.instance(context).Large.size);
		paintLarge.setTypeface(FontCache.instance(context).Large.face);
		paintLarge.setTextAlign(Align.CENTER);

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
			
			widget.bitmap = draw(widget.width, widget.height);
			widget.priority = 1;
			
			result.put(widget.id, widget);
		}
		
		if(widgetIds == null || widgetIds.contains(id_1)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_1;
			widget.description = desc_1;
			widget.width = 96;
			widget.height = 32;
			
			widget.bitmap = draw(widget.width, widget.height);
			widget.priority = 1;
			
			result.put(widget.id, widget);
		}
		
		if(widgetIds == null || widgetIds.contains(id_2)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_2;
			widget.description = desc_2;
			widget.width = 64;
			widget.height = 96;
			
			widget.bitmap = draw(widget.width, widget.height);
			widget.priority = 1;
			
			result.put(widget.id, widget);
		}
		
		if(widgetIds == null || widgetIds.contains(id_3)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_3;
			widget.description = desc_3;
			widget.width = 96;
			widget.height = 96;
			
			widget.bitmap = draw(widget.width, widget.height);
			widget.priority = 1;
			
			result.put(widget.id, widget);
		}
	}
	
	private Bitmap draw(int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		
		canvas.drawRect(new Rect(0,0, width-1, height-1), paint);
		
		if( width<32 )
			canvas.drawText("Test", width/2, height/2 + 2, paintSmall);
		else
			canvas.drawText("Test", width/2, height/2 + 5, paintLarge);
		
		return bitmap;
	}
}
