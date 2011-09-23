                                                                     
                                                                     
                                                                     
                                             
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

import org.metawatch.manager.Notification.VibratePattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ApiIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		
		// add digital watch check
		
		if (action.equals("org.metawatch.manager.APPLICATION_UPDATE")) {
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
			Application.startAppMode();
			return;
		}
		
		if (action.equals("org.metawatch.manager.IDLE_BUTTONS_OVERRIDE")) {
			Bundle bundle = intent.getExtras();
			if (bundle.containsKey("buttons")) {
					Idle.overridenButtons = bundle.getByteArray("buttons");
			} else {
				Idle.overridenButtons = null;
			}
			return;
		}
		
		if (action.equals("org.metawatch.manager.NOTIFICATION")) {
			if (intent.hasExtra("array")) {
				int[] array = intent.getIntArrayExtra("array");
				VibratePattern vibrate = null;				
				if (intent.hasExtra("vibrate_on") && intent.hasExtra("vibrate_off") && intent.hasExtra("vibrate_cycles")) {
					int vibrateOn = intent.getIntExtra("vibrate_on", 500);
					int vibrateOff = intent.getIntExtra("vibrate_off", 500);
					int vibrateCycles = intent.getIntExtra("vibrate_cycles", 3);
					vibrate = new VibratePattern(true, vibrateOn, vibrateOff, vibrateCycles);
				}				
				Notification.addArrayNotification(context, array, vibrate);
			} else if (intent.hasExtra("buffer")) {
				byte[] buffer = intent.getByteArrayExtra("buffer");
				VibratePattern vibrate = null;				
				if (intent.hasExtra("vibrate_on") && intent.hasExtra("vibrate_off") && intent.hasExtra("vibrate_cycles")) {
					int vibrateOn = intent.getIntExtra("vibrate_on", 500);
					int vibrateOff = intent.getIntExtra("vibrate_off", 500);
					int vibrateCycles = intent.getIntExtra("vibrate_cycles", 3);
					vibrate = new VibratePattern(true, vibrateOn, vibrateOff, vibrateCycles);
				}	
				Notification.addBufferNotification(context, buffer, vibrate);
			}
			return;
		}
		
	}

}
