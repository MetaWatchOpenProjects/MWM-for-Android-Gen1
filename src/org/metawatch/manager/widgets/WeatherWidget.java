package org.metawatch.manager.widgets;

import java.util.List;
import java.util.Map;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;
import org.metawatch.manager.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;

public class WeatherWidget implements InternalWidget {
	public final static String id_0 = "weather_24_32";
	final static String desc_0 = "Current Weather (24x32)";
	
	public final static String id_1 = "weather_96_32";
	final static String desc_1 = "Current Weather (96x32)";
	
	public final static String id_2 = "weather_fc_96_32";
	final static String desc_2 = "Weather Forecast (96x32)";
	
	private Context context;
	private TextPaint paintSmall;
	private TextPaint paintSmallOutline;
	private TextPaint paintLarge;
	private TextPaint paintLargeOutline;
	
	public void init(Context context, List<String> widgetIds) {
		this.context = context;

		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		
		paintSmallOutline = new TextPaint();
		paintSmallOutline.setColor(Color.WHITE);
		paintSmallOutline.setTextSize(FontCache.instance(context).Small.size);
		paintSmallOutline.setTypeface(FontCache.instance(context).Small.face);
		
		paintLarge = new TextPaint();
		paintLarge.setColor(Color.BLACK);
		paintLarge.setTextSize(FontCache.instance(context).Large.size);
		paintLarge.setTypeface(FontCache.instance(context).Large.face);
		
		paintLargeOutline = new TextPaint();
		paintLargeOutline.setColor(Color.WHITE);
		paintLargeOutline.setTextSize(FontCache.instance(context).Large.size);
		paintLargeOutline.setTypeface(FontCache.instance(context).Large.face);
	}

	public void shutdown() {
		paintSmall = null;
	}

	public void refresh(List<String> widgetIds) {
	}

	public void get(List<String> widgetIds, Map<String,WidgetData> result) {
		
		if(widgetIds == null || widgetIds.contains(id_0)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_0;
			widget.description = desc_0;
			widget.width = 24;
			widget.height = 32;
			
			widget.bitmap = draw0();
			widget.priority = WeatherData.received ? 1 : 0;
			
			result.put(widget.id, widget);
		}
		
		if(widgetIds == null || widgetIds.contains(id_1)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_1;
			widget.description = desc_1;
			widget.width = 96;
			widget.height = 32;
			
			widget.bitmap = draw1();
			widget.priority = WeatherData.received ? 1 : 0;
			
			result.put(widget.id, widget);
		}
		
		if(widgetIds == null || widgetIds.contains(id_2)) {
			InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
			
			widget.id = id_2;
			widget.description = desc_2;
			widget.width = 96;
			widget.height = 32;
			
			widget.bitmap = draw2();
			widget.priority = WeatherData.received ? 1 : 0;
			
			result.put(widget.id, widget);
		}
	}
		
	private Bitmap draw0() {
		Bitmap bitmap = Bitmap.createBitmap(24, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		if (WeatherData.received) {
			
			// icon
			Bitmap image = Utils.loadBitmapFromAssets(context, WeatherData.icon);
			canvas.drawBitmap(image, 0, 4, null);
								
			// temperatures
			if (WeatherData.celsius) {
				Utils.drawOutlinedText(WeatherData.temp+"°C", canvas, 0, 7, paintSmall, paintSmallOutline);
			}
			else {
				Utils.drawOutlinedText(WeatherData.temp+"°F", canvas, 0, 7, paintSmall, paintSmallOutline);
			}
			paintLarge.setTextAlign(Paint.Align.LEFT);
						
			Utils.drawOutlinedText("H "+WeatherData.forecast[0].tempHigh, canvas, 0, 25, paintSmall, paintSmallOutline);
			Utils.drawOutlinedText("L "+WeatherData.forecast[0].tempLow, canvas, 0, 31, paintSmall, paintSmallOutline);
									
		} else {
			paintSmall.setTextAlign(Paint.Align.CENTER);

			canvas.drawText("Wait", 12, 16, paintSmall);

			paintSmall.setTextAlign(Paint.Align.LEFT);
		}
		
		return bitmap;
	}
	
	private Bitmap draw1() {
		Bitmap bitmap = Bitmap.createBitmap(96, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		if (WeatherData.received) {
			
			// icon
			Bitmap image = Utils.loadBitmapFromAssets(context, WeatherData.icon);
			canvas.drawBitmap(image, 36, 5, null);
			
			// condition
			Utils.drawWrappedOutlinedText(WeatherData.condition, canvas, 1, 1, 60, paintSmall, paintSmallOutline, Layout.Alignment.ALIGN_NORMAL);
								
			// temperatures
			paintLarge.setTextAlign(Paint.Align.RIGHT);
			paintLargeOutline.setTextAlign(Paint.Align.RIGHT);
			Utils.drawOutlinedText(WeatherData.temp, canvas, 82, 12, paintLarge, paintLargeOutline);
			if (WeatherData.celsius) {
				//RM: since the degree symbol draws wrong...
				canvas.drawText("O", 82, 6, paintSmall);
				canvas.drawText("C", 95, 12, paintLarge);
			}
			else {
				//RM: since the degree symbol draws wrong...
				canvas.drawText("O", 83, 6, paintSmall);
				canvas.drawText("F", 95, 12, paintLarge);
			}
			paintLarge.setTextAlign(Paint.Align.LEFT);
						
			canvas.drawText("High", 64, 23, paintSmall);
			canvas.drawText("Low", 64, 31, paintSmall);
			
			paintSmall.setTextAlign(Paint.Align.RIGHT);
			canvas.drawText(WeatherData.forecast[0].tempHigh, 95, 23, paintSmall);
			canvas.drawText(WeatherData.forecast[0].tempLow, 95, 31, paintSmall);
			paintSmall.setTextAlign(Paint.Align.LEFT);

			Utils.drawOutlinedText((String) TextUtils.ellipsize(WeatherData.locationName, paintSmall, 63, TruncateAt.END), canvas, 1, 31, paintSmall, paintSmallOutline);
						
		} else {
			paintSmall.setTextAlign(Paint.Align.CENTER);
			//if (Preferences.weatherGeolocation) {
				if( !LocationData.received ) {
					canvas.drawText("Awaiting location", 48, 18, paintSmall);
				}
				else {
					canvas.drawText("Awaiting weather", 48, 18, paintSmall);
				}
			//}
			//else {
			//	canvas.drawText("No data", 48, 18, paintSmall);
			//}
			paintSmall.setTextAlign(Paint.Align.LEFT);
		}
		
		return bitmap;
	}

	private Bitmap draw2() {
		Bitmap bitmap = Bitmap.createBitmap(96, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		paintSmall.setTextAlign(Align.LEFT);
		paintSmallOutline.setTextAlign(Align.LEFT);
		
		if (WeatherData.received && WeatherData.forecast.length>=6) {
			for (int i=0;i<4;++i) {
				int x = i*24;
				Bitmap image = Utils.loadBitmapFromAssets(context, WeatherData.forecast[i+1].icon);
				canvas.drawBitmap(image, x, 4, null);
				Utils.drawOutlinedText(WeatherData.forecast[i+1].day, canvas, x, 6, paintSmall, paintSmallOutline);
				
				Utils.drawOutlinedText("H "+WeatherData.forecast[i+1].tempHigh, canvas, x, 25, paintSmall, paintSmallOutline);
				Utils.drawOutlinedText("L "+WeatherData.forecast[i+1].tempLow, canvas, x, 31, paintSmall, paintSmallOutline);
			}
		} else {
			paintSmall.setTextAlign(Paint.Align.CENTER);
			//if (Preferences.weatherGeolocation) {
				if( !LocationData.received ) {
					canvas.drawText("Awaiting location", 48, 18, paintSmall);
				}
				else {
					canvas.drawText("Awaiting weather", 48, 18, paintSmall);
				}
			//}
			//else {
			//	canvas.drawText("No data", 48, 18, paintSmall);
			//}
			paintSmall.setTextAlign(Paint.Align.LEFT);
		}
		
		return bitmap;
	}


}
