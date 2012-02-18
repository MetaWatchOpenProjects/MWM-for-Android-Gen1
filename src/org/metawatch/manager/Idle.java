                                                                     
                                                                     
                                                                     
                                             
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.widgets.InternalWidget.WidgetData;
import org.metawatch.manager.widgets.WidgetManager;
import org.metawatch.manager.widgets.WidgetRow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

public class Idle {
	
	public static byte[] overridenButtons = null;
	
	final static byte IDLE_NEXT_PAGE = 60;

	static int currentPage = 0;
	
	static boolean widgetsInitialised = false;
		
	public static void NextPage() {
		
		if(currentPage==1) {
			Protocol.disableMediaButtons();
			Log.d(MetaWatch.TAG, "Leaving media mode");
			MediaControl.mediaPlayerActive = false;
		}
		
		currentPage = (currentPage+1) % numPages();
		
		if(currentPage==1) {
			Protocol.enableMediaButtons();
			Log.d(MetaWatch.TAG, "Entering media mode");
			MediaControl.mediaPlayerActive = true;
		}
	}
	
	private static int numPages() {
		int pages = 1;
		if(Preferences.idleMusicControls) {
			pages++;
		}
		return pages;
	}
	

	static synchronized Bitmap createLcdIdle(Context context) {
		return createLcdIdle(context, currentPage);
	}

	static synchronized Bitmap createLcdIdle(Context context, int page) {
		
		if(!widgetsInitialised) {
			WidgetManager.initWidgets(context, null);
			widgetsInitialised = true;
		}
		
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
		
		TextPaint paintLargeOutline = new TextPaint();
		paintLargeOutline.setColor(Color.WHITE);
		paintLargeOutline.setTextSize(FontCache.instance(context).Large.size);
		paintLargeOutline.setTypeface(FontCache.instance(context).Large.face);
		
		canvas.drawColor(Color.WHITE);	
		
		if( page == 0 ) {
		
			Protocol.configureIdleBufferSize(true);
	
			List<WidgetRow> rows = WidgetManager.getDesiredWidgetsFromPrefs();
			
			List<String> widgetsDesired = new ArrayList<String>();
			for(WidgetRow row : rows) {
				widgetsDesired.addAll(row.getIds());
			}			
			Map<String,WidgetData> widgetData = WidgetManager.refreshWidgets(widgetsDesired);
			
			int space = (64 - (rows.size()*32)) / (rows.size()+1);
			int yPos = 32 + space;
			
			for(WidgetRow row : rows) {
				row.draw(widgetData, canvas, yPos);
				yPos += 32 + space;
			}
				
		}
		else if (page == 1) {
			Protocol.configureIdleBufferSize(false);
			
			if(MediaControl.lastTrack=="") {
				canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "media_player_idle.png"), 0, 0, null);				
			}
			else {	
				canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "media_player.png"), 0, 0, null);
				
				
				TextPaint tp = null;
				if( paintLarge.measureText(MediaControl.lastTrack) < 170) {
					tp = paintLarge;
				}
				else {
					tp = paintSmall;
				}
				
				canvas.save();			
				StaticLayout layout = new StaticLayout(MediaControl.lastTrack, tp, 96, Layout.Alignment.ALIGN_CENTER, 1.2f, 0, false);
				int height = layout.getHeight();
				int textY = 26 - (height/2);
				if(textY<8) {
					textY=8;
				}
				canvas.translate(0, textY); //position the text
				canvas.clipRect(0,0,96,35);
				layout.draw(canvas);
				canvas.restore();	
				
				canvas.save();			
				layout = new StaticLayout(MediaControl.lastArtist + "\n\n" + MediaControl.lastAlbum, paintSmall, 96, Layout.Alignment.ALIGN_CENTER, 1.0f, 0, false);
				height = layout.getHeight();
				textY = 70 - (height/2);
				if(textY<54) {
					textY=54;
				}
				canvas.translate(0, textY); //position the text
				canvas.clipRect(0,0,96,35);
				layout.draw(canvas);
				canvas.restore();	
			}
		}
		
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
		
		if (numPages()>1)
			Protocol.enableButton(0, 0, IDLE_NEXT_PAGE, 0); // Right top immediate

		
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
