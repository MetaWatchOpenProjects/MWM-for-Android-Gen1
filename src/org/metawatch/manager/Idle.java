                                                                     
                                                                     
                                                                     
                                             
 /*****************************************************************************
  *  Copyright (c) 2011 Meta Watch Ltd.                                       *
  *  www.MetaWatch.org                                                        *
  *                                                                           *
  =============================================================================
  *                                                                           *
  *  Licensed under the Apache License, Version 2.0 (the "License");          *
  *  you may not use this file except in compliance with the License.         *
  *  You may obtain a copy of the License at                                  *
  *                                                                           *
  *    http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                           *
  *  Unless required by applicable law or agreed to in writing, software      *
  *  distributed under the License is distributed on an "AS IS" BASIS,        *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
  *  See the License for the specific language governing permissions and      *
  *  limitations under the License.                                           *
  *                                                                           *
  *****************************************************************************/

 /*****************************************************************************
  * Idle.java                                                                 *
  * Idle                                                                      *
  * Idle watch mode                                                           *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;

public class Idle {
	
	public static byte[] overridenButtons = null;

	static void drawWrappedText(String text, Canvas canvas, int x, int y, int width, TextPaint paint) {
		canvas.save();
		StaticLayout layout = new StaticLayout(text, paint, width, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
		canvas.translate(x, y); //position the text
		layout.draw(canvas);
		canvas.restore();	
	}
	
	static void drawOutlinedText(String text, Canvas canvas, int x, int y, TextPaint col, TextPaint outline) {
		canvas.drawText(text, x+1, y, outline);
		canvas.drawText(text, x-1, y, outline);
		canvas.drawText(text, x, y+1, outline);
		canvas.drawText(text, x, y-1, outline);
	
		canvas.drawText(text, x, y, col);
	}
	
	static void drawWrappedOutlinedText(String text, Canvas canvas, int x, int y, int width, TextPaint col, TextPaint outline) {
		drawWrappedText(text, canvas, x-1, y, width, outline);
		drawWrappedText(text, canvas, x+1, y, width, outline);
		drawWrappedText(text, canvas, x, y-1, width, outline);
		drawWrappedText(text, canvas, x, y+1, width, outline);
		
		drawWrappedText(text, canvas, x, y, width, col);
	}

	static Bitmap createLcdIdle(Context context) {
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		
		TextPaint paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		
		TextPaint paintSmallOutline = new TextPaint();
		paintSmallOutline.setColor(Color.WHITE);
		paintSmallOutline.setTextSize(FontCache.instance(context).Small.size);
		paintSmallOutline.setTypeface(FontCache.instance(context).Small.face);
		
		TextPaint paintLarge = new TextPaint();
		paintLarge.setColor(Color.BLACK);
		paintLarge.setTextSize(FontCache.instance(context).Large.size);
		paintLarge.setTypeface(FontCache.instance(context).Large.face);
		
		canvas.drawColor(Color.WHITE);
		
		canvas = drawLine(canvas, 32);		
		if(!Preferences.disableWeather) {
			if (WeatherData.received) {
				
				//WeatherData.icon = "weather_sunny.bmp";
				//WeatherData.locationName = "a really long place name";
				//WeatherData.condition = "cloudy with a chance of meatballs";
				
				// icon
				Bitmap image = Utils.loadBitmapFromAssets(context, WeatherData.icon);
				canvas.drawBitmap(image, 36, 37, null);
				
				// condition
				drawWrappedOutlinedText(WeatherData.condition, canvas, 1, 35, 60, paintSmall, paintSmallOutline);
				
				
				// temperatures
				if (WeatherData.celsius) {
					paintLarge.setTextAlign(Paint.Align.RIGHT);
					canvas.drawText(WeatherData.temp, 82, 46, paintLarge);
					//RM: since the degree symbol draws wrong...
					canvas.drawText("O", 82, 40, paintSmall);
					canvas.drawText("C", 95, 46, paintLarge);
				}
				else {
					paintLarge.setTextAlign(Paint.Align.RIGHT);
					canvas.drawText(WeatherData.temp, 83, 46, paintLarge);
					//RM: since the degree symbol draws wrong...
					canvas.drawText("O", 83, 40, paintSmall);
					canvas.drawText("F", 95, 46, paintLarge);
				}
				paintLarge.setTextAlign(Paint.Align.LEFT);
							
				canvas.drawText("High", 64, 54, paintSmall);
				canvas.drawText("Low", 64, 62, paintSmall);
				
				paintSmall.setTextAlign(Paint.Align.RIGHT);
				canvas.drawText(WeatherData.tempHigh, 95, 54, paintSmall);
				canvas.drawText(WeatherData.tempLow, 95, 62, paintSmall);
				paintSmall.setTextAlign(Paint.Align.LEFT);

				drawOutlinedText((String) TextUtils.ellipsize(WeatherData.locationName, paintSmall, 63, TruncateAt.END), canvas, 1, 62, paintSmall, paintSmallOutline);
							
			} else {
				paintSmall.setTextAlign(Paint.Align.CENTER);
				if (Preferences.weatherGeolocation) {
					if( !LocationData.received ) {
						canvas.drawText("Awaiting location", 48, 50, paintSmall);
					}
					else {
						canvas.drawText("Awaiting weather", 48, 50, paintSmall);
					}
				}
				else {
					canvas.drawText("No data", 48, 50, paintSmall);
				}
				paintSmall.setTextAlign(Paint.Align.LEFT);
			}
						
			// Debug current time
			//String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
			//String currentTimeString = new SimpleDateFormat("HH:mm").format(new Date());
			//canvas.drawText(currentTimeString, 0, 56, paintSmall);
			
			canvas = drawLine(canvas, 64);
		}
		
		// icons row
		//Bitmap imageI = Utils.loadBitmapFromAssets(context, "idle_icons_row.bmp");
		//canvas.drawBitmap(imageI, 0, 66, null);
				
		int rows = 3;
		/*
		if (Utils.isGmailAccessSupported(context))
			rows = 3;
		else
			rows = 2;
		*/
		int yPos = !Preferences.disableWeather ? 67 : 36;
		// icons
		for (int i = 0; i < rows; i++) {
			int slotSpace = 96/rows;
			int slotX = slotSpace/2-12;
			int iconX = slotSpace*i + slotX;
			switch (i) {
				case 0:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_call.bmp"), iconX, yPos, null);
					break;
				case 1:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_sms.bmp"), iconX, yPos, null);
					break;
				case 2:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_gmail.bmp"), iconX, yPos, null);
					break;
			}
		}
				
		// unread counters
		for (int i = 0; i < rows; i++) {
			String count = "";
			switch (i) {
				case 0:
					count = Integer.toString(Utils.getMissedCallsCount(context));					
					break;
				case 1:
					count = Integer.toString(Utils.getUnreadSmsCount(context));
					break;
				case 2:
					if(Preferences.showK9Unread) {
						Log.d(MetaWatch.TAG, "Idle: About to draw k9 count.");
						count = Integer.toString(Utils.getUnreadK9Count(context));
						Log.d(MetaWatch.TAG, "Idle: k9 count is " + count);
					}
					else {
						Log.d(MetaWatch.TAG, "Idle: About to draw Gmail count.");
						if (Utils.isGmailAccessSupported(context))
							count = Integer.toString(Utils.getUnreadGmailCount(context, Utils.getGoogleAccountName(context), "^i"));
						else 
							count = Integer.toString(Monitors.getGmailUnreadCount());
						Log.d(MetaWatch.TAG, "Idle: Gmail count is " + count);
					}
					break;				
			}
			
			int slotSpace = 96/rows;
			int slotX = (int) (slotSpace/2-paintSmall.measureText(count)/2);
			int countX = slotSpace*i + slotX;
			
			canvas.drawText(count, countX, !Preferences.disableWeather ? 92 : 62, paintSmall);
		}
		if(Preferences.disableWeather) {
			canvas = drawLine(canvas, 64);
			//Add more icons here in future.
		}
		
		/*
		FileOutputStream fos = new FileOutputStream("/sdcard/test.png");
		image.compress(Bitmap.CompressFormat.PNG, 100, fos);
		fos.close();
		Log.d("ow", "bmp ok");
		*/
		return bitmap;
	}
	
	public static Canvas drawLine(Canvas canvas, int y) {
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		
		int left = 3;
		
		for (int i = 0+left; i < 96-left; i += 3)
			canvas.drawLine(i, y, i+2, y, paint);
		
		return canvas;
	}
	
	public static synchronized void sendLcdIdle(Context context) {
		Bitmap bitmap = createLcdIdle(context);
		//Protocol.loadTemplate(0);		
		Protocol.sendLcdBitmap(bitmap, MetaWatchService.WatchBuffers.IDLE);
		//Protocol.activateBuffer();
		Protocol.updateDisplay(0);
	}
	
	public static boolean toIdle(Context context) {
		// check for parent modes
		
		MetaWatchService.WatchModes.IDLE = true;
		MetaWatchService.watchState = MetaWatchService.WatchStates.IDLE;
		
		if (MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL) {
			sendLcdIdle(context);
			//Protocol.updateDisplay(0);
		}
		
		return true;
	}
	
	public static void updateLcdIdle(Context context) {
		if (MetaWatchService.watchState == MetaWatchService.WatchStates.IDLE
				&& MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL)
			sendLcdIdle(context);
	}
	
	public static boolean isIdleButtonOverriden(byte button) {
		if (overridenButtons != null)
			for (int i = 0; i < overridenButtons.length; i++)
				if (overridenButtons[i] == button)
					return true;
		return false;
	}
	
}
