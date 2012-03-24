                                                                     
                                                                     
                                                                     
                                             
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

import org.metawatch.manager.Monitors.WeatherData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;

public class Idle {
	
	public static byte[] overridenButtons = null;

	static Bitmap createLcdIdle(Context context) {
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		
		Paint paintSmall = new Paint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(8);
		Typeface typefaceSmall = Typeface.createFromAsset(context.getAssets(), "metawatch_8pt_5pxl_CAPS.ttf");
		paintSmall.setTypeface(typefaceSmall);
		
		Paint paintLarge = new Paint();
		paintLarge.setColor(Color.BLACK);
		paintLarge.setTextSize(16);
		Typeface typefaceLarge = Typeface.createFromAsset(context.getAssets(), "metawatch_16pt_11pxl.ttf");
		paintLarge.setTypeface(typefaceLarge);
		
		canvas.drawColor(Color.WHITE);
		
		canvas = drawLine(canvas, 32);		
		
		if (WeatherData.received) {
			// condition
			canvas.save();
			TextPaint paint = new TextPaint(paintSmall);
			StaticLayout layout = new StaticLayout(WeatherData.condition, paint, 36, android.text.Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
			canvas.translate(3, 40); //position the text
			layout.draw(canvas);
			
			canvas.restore();						
			
			// icon
			Bitmap image = Utils.loadBitmapFromAssets(context, WeatherData.icon);
			canvas.drawBitmap(image, 37, 35, null);
			
			// temperatures
			canvas.drawText(WeatherData.temp, 64, 46, paintLarge);				
			canvas.drawText(WeatherData.tempHigh, 64, 54, paintSmall);
			canvas.drawText(WeatherData.tempLow, 64, 62, paintSmall);
			canvas.drawText(WeatherData.city, 3, 62, paintSmall);
			
		} else {
			canvas.drawText("no data", 34, 50, paintSmall);
		}
		
		canvas = drawLine(canvas, 64);
		
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
				
		// icons
		for (int i = 0; i < rows; i++) {
			int slotSpace = 96/rows;
			int slotX = slotSpace/2-12;
			int iconX = slotSpace*i + slotX;
			switch (i) {
				case 0:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_call.bmp"), iconX, 67, null);
					break;
				case 1:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_sms.bmp"), iconX, 67, null);
					break;
				case 2:
					canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "idle_gmail.bmp"), iconX, 67, null);
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
					if (Utils.isGmailAccessSupported(context))
						count = Integer.toString(Utils.getUnreadGmailCount(context, Utils.getGoogleAccountName(context), "^i"));
					else 
						count = Integer.toString(Monitors.getGmailUnreadCount());
					break;				
			}
			
			int slotSpace = 96/rows;
			int slotX = (int) (slotSpace/2-paintSmall.measureText(count)/2);
			int countX = slotSpace*i + slotX;
			
			canvas.drawText(count, countX, 92, paintSmall);
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
	
	public static void sendLcdIdle(Context context) {
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
		
		sendLcdIdle(context);
		//Protocol.updateDisplay(0);
		
		return true;
	}
	
	public static void updateLcdIdle(Context context) {
		if (MetaWatchService.watchState == MetaWatchService.WatchStates.IDLE)
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
