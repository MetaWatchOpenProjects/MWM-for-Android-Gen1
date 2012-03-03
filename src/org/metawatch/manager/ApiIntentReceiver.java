                                                                     
                                                                     
                                                                     
                                             
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
  * ApiIntentReceiver.java                                                    *
  * ApiIntentReceiver                                                         *
  * Intent receiver for 3rd party software                                     *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Notification.VibratePattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ApiIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		// add digital watch check
		
		if (action.equals("org.metawatch.manager.APPLICATION_UPDATE")) {
			//boolean dither = false;
			//if (intent.hasExtra("requires_dither"))
			//	dither = true;
			if (intent.hasExtra("array")) {
				int[] array = intent.getIntArrayExtra("array");			
				Application.updateAppMode(context, array);
			} else if (intent.hasExtra("buffer")) {
				byte[] buffer = intent.getByteArrayExtra("buffer");
				Application.updateAppMode(context, buffer);
			}
			return;
		}
		
		if (action.equals("org.metawatch.manager.APPLICATION_START")) {
			Application.startAppMode();
			return;
		}
		
		if (action.equals("org.metawatch.manager.APPLICATION_STOP")) {
			Application.stopAppMode(context);
			return;
		}
		
		if (action.equals("org.metawatch.manager.NOTIFICATION")) {
			
			/* Set up vibrate pattern. */
			VibratePattern vibrate = getVibratePatternFromIntent(intent);
						
			if (intent.hasExtra("oled1") || intent.hasExtra("oled1a")
					|| intent.hasExtra("oled1b") || intent.hasExtra("oled2")
					|| intent.hasExtra("oled2a") || intent.hasExtra("oled2b")) {

				byte[] line1 = Protocol.createOled1line(context, null, "");
				byte[] line2 = Protocol.createOled1line(context, null, "");
				byte[] scroll = null;
				int scrollLen = 0;
				if (intent.hasExtra("oled1")) {
					line1 = Protocol.createOled1line(context, null, intent.getStringExtra("oled1"));
				} else {
					if (intent.hasExtra("oled1a") || intent.hasExtra("oled1b")) {
						String oled1a = "";
						String oled1b = "";
						if (intent.hasExtra("oled1a")) {
							oled1a = intent.getStringExtra("oled1a");
						}
						if (intent.hasExtra("oled1b")) {
							oled1b = intent.getStringExtra("oled1b");
						}
						line1 = Protocol.createOled2lines(context, oled1a, oled1b);
					}
				}
				if (intent.hasExtra("oled2")) {
					line2 = Protocol.createOled1line(context, null, intent.getStringExtra("oled2"));
				} else {
					if (intent.hasExtra("oled2a") || intent.hasExtra("oled2b")) {
						String oled2a = "";
						String oled2b = "";
						if (intent.hasExtra("oled2a")) {
							oled2a = intent.getStringExtra("oled2a");
						}
						if (intent.hasExtra("oled2b")) {
							oled2b = intent.getStringExtra("oled2b");
						}
						scroll = new byte[800];
						scrollLen = Protocol.createOled2linesLong(context, oled2b, scroll);
						line2 = Protocol.createOled2lines(context, oled2a, oled2b);
					}
				}
				Notification.addOledNotification(context, line1, line2, scroll, scrollLen, vibrate);
				
			} else if (intent.hasExtra("text")) {
				String text = intent.getStringExtra("text");
				Notification.addTextNotification(context, text, vibrate,
						Notification.getDefaultNotificationTimeout(context));
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"ApiIntentReceiver.onReceive(): sending text notification; text='"
								+ text + "'");
			} else if (intent.hasExtra("array")) {
				int[] array = intent.getIntArrayExtra("array");
				Notification.addArrayNotification(context, array, vibrate);
			} else if (intent.hasExtra("buffer")) {
				byte[] buffer = intent.getByteArrayExtra("buffer");
				Notification.addBufferNotification(context, buffer, vibrate);
			}
			return;
		}
		
		if (action.equals("org.metawatch.manager.VIBRATE")) {			
			/* Set up vibrate pattern. */
			VibratePattern vibrate = getVibratePatternFromIntent(intent);
			
			if(vibrate.vibrate)
				Protocol.vibrate(vibrate.on, vibrate.off, vibrate.cycles);
			
			return;
		}
		
	}
	
	private VibratePattern getVibratePatternFromIntent(Intent intent){
		/* Set up vibrate pattern. */
		VibratePattern vibrate = Notification.VibratePattern.NO_VIBRATE;
		if (intent.hasExtra("vibrate_on") && intent.hasExtra("vibrate_off") && intent.hasExtra("vibrate_cycles")) {
			int vibrateOn = intent.getIntExtra("vibrate_on", 500);
			int vibrateOff = intent.getIntExtra("vibrate_off", 500);
			int vibrateCycles = intent.getIntExtra("vibrate_cycles", 3);
			vibrate = new VibratePattern(true, vibrateOn, vibrateOff, vibrateCycles);
		}
		return vibrate;
	}

}
