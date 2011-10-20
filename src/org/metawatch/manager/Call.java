                                                                     
                                                                     
                                                                     
                                             
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
  * Call.java                                                                 *
  * Call                                                                      *
  * Call watch mode                                                           *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.WatchType;
import org.metawatch.manager.Notification.VibratePattern;

import android.content.Context;
import android.graphics.Bitmap;

public class Call {
	
	public static boolean isRinging = false;
	
	public static void startCall(Context context, String number) {
		toCall();
		
		isRinging = true;
		
		Bitmap bitmap;
		String name = Utils.getContactNameFromNumber(context, number);
		
		if (name.equals(number))		
			bitmap = NotificationBuilder.smartLines(context, "phone.bmp", new String[] { number});
		else 
			bitmap = NotificationBuilder.smartLines(context, "phone.bmp", new String[] { number, name });
		
		if (MetaWatchService.watchType == WatchType.DIGITAL) {
			Protocol.sendLcdBitmap(bitmap, MetaWatchService.WatchBuffers.NOTIFICATION);		
			Protocol.updateDisplay(2);
		} else {
			Notification.addOledNotification(context, Protocol.createOled1line(context, "phone.bmp", "Call from"), Protocol.createOled1line(context, null, name), null, 0, new VibratePattern(true, 500, 500, 3));
		}
		
		Thread ringer = new Thread(new CallVibrate());
		ringer.start();		
	}
	
	public static void endCall(Context context) {
		isRinging = false;
		exitCall(context);
	}
		
	static void toCall() {		
		MetaWatchService.watchState = MetaWatchService.WatchStates.CALL;
		MetaWatchService.WatchModes.CALL = true;					
	}
	
	static void exitCall(Context context) {
				
		MetaWatchService.WatchModes.CALL = false;
				
		if (MetaWatchService.WatchModes.NOTIFICATION == true)
			Notification.toNotification(context);
		else if (MetaWatchService.WatchModes.APPLICATION == true)
			Application.toApp();
		else if (MetaWatchService.WatchModes.IDLE == true)
			Idle.toIdle(context);
	}
	
}


