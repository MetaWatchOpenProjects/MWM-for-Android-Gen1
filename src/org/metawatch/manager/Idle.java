                                                                     
                                                                     
                                                                     
                                             
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
import android.preference.Preference;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

public class Idle {
	
	final static byte IDLE_NEXT_PAGE = 60;

	static int currentPage = 0;
	
	static boolean widgetsInitialised = false;
		
	static int mediaPlayerPage = -1;
	
	public static void nextPage() {
		toPage(currentPage+1);
	}
	
	public static void toPage(int page) {
		
		if(currentPage==mediaPlayerPage) {
			Protocol.disableMediaButtons();
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Leaving media mode");
			MediaControl.mediaPlayerActive = false;
		}
		
		currentPage = (page) % numPages();
		
		if(currentPage==mediaPlayerPage) {
			Protocol.enableMediaButtons();
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Entering media mode");
			MediaControl.mediaPlayerActive = true;
		}
	}
	
	public static int numPages() {	
		int pages = (widgetScreens==null || widgetScreens.size()==0) ? 1 : widgetScreens.size();
		if(Preferences.idleMusicControls) {
			mediaPlayerPage = pages;
			pages++;
		}
		return pages;
	}
	
	private static ArrayList<ArrayList<WidgetRow>> widgetScreens = null;
	private static Map<String,WidgetData> widgetData = null;
	
	public static synchronized void updateWidgetPages(Context context)
	{
		if(!widgetsInitialised) {
			WidgetManager.initWidgets(context, null);
			widgetsInitialised = true;
		}
		
		List<WidgetRow> rows = WidgetManager.getDesiredWidgetsFromPrefs();
		
		ArrayList<CharSequence> widgetsDesired = new ArrayList<CharSequence>();
		for(WidgetRow row : rows) {
			widgetsDesired.addAll(row.getIds());
		}			
		widgetData = WidgetManager.refreshWidgets(widgetsDesired);
		
		for(WidgetRow row : rows) { 
			row.doLayout(widgetData);
		}
		
		// Bucket rows into screens
		ArrayList<ArrayList<WidgetRow>> screens = new ArrayList<ArrayList<WidgetRow>>();
	
		int screenSize = 32; // Initial screen has top part used by the fw clock
		ArrayList<WidgetRow> screen = new ArrayList<WidgetRow>();
		for(WidgetRow row : rows) { 
			if(screenSize+row.getHeight() > 96) {
				screens.add(screen);
				screen = new ArrayList<WidgetRow>();
				screenSize = 0;
			}
			screen.add(row);
			screenSize += row.getHeight();
		}
		screens.add(screen);
		
		widgetScreens = screens;
	}

	static synchronized Bitmap createLcdIdle(Context context) {
		return createLcdIdle(context, false, currentPage);
	}

	static synchronized Bitmap createLcdIdle(Context context, boolean preview, int page) {
		
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
		
		if( page != mediaPlayerPage ) {
		
			if(preview && page==0) {
				canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "dummy_clock.png"), 0, 0, null);
			} 
	
			if(widgetScreens.size() > page)
			{
				ArrayList<WidgetRow> rowsToDraw = widgetScreens.get(page);
				
				int totalHeight = 0;
				for(WidgetRow row : rowsToDraw) {
					totalHeight += row.getHeight();
				}
							
				int space = (((page==0 ? 64:96) - totalHeight) / (rowsToDraw.size()+1));
				int yPos = (page==0 ? 32:0) + space;
				
				for(WidgetRow row : rowsToDraw) {
					row.draw(widgetData, canvas, yPos);
					yPos += row.getHeight() + space;
				}

				if (Preferences.displayWidgetRowSeparator) {
					int i = (page==0 ? -1:0);
					yPos = 0 + space;
					for(WidgetRow row : rowsToDraw) {
						yPos += row.getHeight() + space;
						i++;
						if (i!=rowsToDraw.size())
							drawLine(canvas, yPos);
					}
				}

			}

		}
		else {
			
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
	
	private static synchronized void sendLcdIdle(Context context) {
		if(MetaWatchService.watchState != MetaWatchService.WatchStates.IDLE) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Ignoring sendLcdIdle as not in idle");
			return;
		}
		
		updateWidgetPages(context);
		Bitmap bitmap = createLcdIdle(context);
		int mode = currentPage==mediaPlayerPage ? MetaWatchService.WatchBuffers.APPLICATION : MetaWatchService.WatchBuffers.IDLE;
		
		Protocol.sendLcdBitmap(bitmap, mode);
		Protocol.configureIdleBufferSize(currentPage==0);
		Protocol.updateDisplay(mode);
	}
	
	public static boolean toIdle(Context context) {
		
		MetaWatchService.WatchModes.IDLE = true;
		MetaWatchService.watchState = MetaWatchService.WatchStates.IDLE;
		
		if (MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL) {
			sendLcdIdle(context);
				
			if (numPages()>1) {
				Protocol.enableButton(0, 0, IDLE_NEXT_PAGE, 0); // Right top immediate
				Protocol.enableButton(0, 0, IDLE_NEXT_PAGE, 1); // Right top immediate
			}
		
		}

		return true;
	}
	
	public static void updateLcdIdle(Context context) {
		if (MetaWatchService.watchState == MetaWatchService.WatchStates.IDLE
				&& MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL)
			sendLcdIdle(context);
	}
	
}
