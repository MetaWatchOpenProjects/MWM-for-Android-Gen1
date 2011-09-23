                                                                     
                                                                     
                                                                     
                                             
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
  * NotificationBuilder.java                                                  *
  * NotificationBuilder                                                       *
  * Templates for different kinds of notification screens                     *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WatchType;
import org.metawatch.manager.Notification.VibratePattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

public class NotificationBuilder {
	
	public static final class FontSize {
		public static final int SMALL = 1;
		public static final int MEDIUM = 2;
		public static final int LARGE = 3;
	}

	public static void createSMS(Context context, String number, String text) {
		String name = Utils.getContactNameFromNumber(context, number);
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "message.bmp", new String[] {"SMS from", name});		
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 500, 500, 3), 4000);
			Notification.addTextNotification(context, text, new VibratePattern(false, 0, 0, 0), Notification.notificationTimeout);
		} else {
			byte[] scroll = new byte[800];
			int len = Protocol.createOled2linesLong(context, text, scroll);
			Notification.addOledNotification(context, Protocol.createOled1line(context, "message.bmp", "SMS from"), Protocol.createOled2lines(context, name, text), scroll, len, new VibratePattern(true, 500, 500, 3));
		}
	}
	
	public static void createK9(Context context, String sender, String subject) {	
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "email.bmp", new String[] {"K9 mail from", sender, subject});		
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 500, 500, 3), Notification.notificationTimeout);
		} else {
			Notification.addOledNotification(context, Protocol.createOled1line(context, "email.bmp", "K9 mail from"), Protocol.createOled2lines(context, sender, subject), null, 0, new VibratePattern(true, 500, 500, 3));
		}
	}
	
	public static void createGmail(Context context, String sender, String email, String subject, String snippet) {
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "email.bmp", new String[] {"Gmail from", sender, email, subject});		
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 500, 500, 3), Notification.notificationTimeout);	
			Notification.addTextNotification(context, snippet, new VibratePattern(false, 0, 0, 0), Notification.notificationTimeout);
		} else {
			Notification.addOledNotification(context, Protocol.createOled2lines(context, "Gmail from " + sender, email), Protocol.createOled2lines(context, subject, snippet), null, 0, new VibratePattern(true, 500, 500, 3));			
		}
	}
	
	public static void createGmailBlank(Context context, String recipient) {
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "email.bmp", new String[] {"Gmail for", recipient});		
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 500, 500, 3), Notification.notificationTimeout);
		} else {
			Notification.addOledNotification(context, Protocol.createOled1line(context, "email.bmp", "SMS for"), Protocol.createOled1line(context, null, recipient), null, 0, new VibratePattern(true, 500, 500, 3));
		}
	}
	
	public static void createAlarm(Context context) {
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "timer.bmp", new String[] {"Alarm Clock"});		
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 500, 500, 3), Notification.notificationTimeout);
		} else {
			Notification.addOledNotification(context, Protocol.createOled1line(context, "timer.bmp", "Alarm clock"), Protocol.createOled1line(context, null, "Alarm"), null, 0, new VibratePattern(true, 500, 500, 3));
		}
	}
	
	public static void createMusic(Context context, String artist, String track) {
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Bitmap bitmap = smartLines(context, "play.bmp", new String[] { track, artist});
			Notification.addBitmapNotification(context, bitmap, new VibratePattern(true, 150, 0, 1), Notification.notificationTimeout);
		} else {
			Notification.addOledNotification(context, Protocol.createOled1line(context, "play.bmp", artist), Protocol.createOled1line(context, null, track), null, 0, new VibratePattern(true, 500, 500, 3));
		}
	}
	
	
	static Bitmap smartLines(Context context, String iconPath, String[] lines) {
		
		String font = null;
		int size = 8;
		int realSize = 7;
		
		switch (Preferences.fontSize) {
			case FontSize.SMALL:
				font = "metawatch_8pt_5pxl_CAPS.ttf";
				realSize = 5;
				break;
			case FontSize.MEDIUM:
				font = "metawatch_8pt_7pxl_CAPS.ttf";
				realSize = 7;
				break;
			case FontSize.LARGE:
				font = "metawatch_16pt_11pxl.ttf";
				realSize = 11;
				size = 16;
				break;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);		
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);		
		paint.setTextSize(size);
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), font);		
		paint.setTypeface(typeface);
		canvas.drawColor(Color.WHITE);
		
		Bitmap icon = Utils.loadBitmapFromAssets(context, iconPath);
		
		int spaceForItem = 96 / (1 + lines.length);
		
		canvas.drawBitmap(icon, 96/2-icon.getWidth()/2, spaceForItem/2-icon.getHeight()/2, paint);
		
		for (int i = 0; i < lines.length; i++) {
			int x = (int)(96/2-paint.measureText(lines[i])/2);
			if (x < 0)
				x = 0;
			int y = spaceForItem * (i + 1) + spaceForItem/2 + realSize/2 ;
			canvas.drawText(lines[i], x, y, paint);
		}
		
		return bitmap;
	}
	
	
	
	
}
