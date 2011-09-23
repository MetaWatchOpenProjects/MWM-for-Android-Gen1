                                                                     
                                                                     
                                                                     
                                             
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
  * Application.java                                                          *
  * Application                                                               *
  * Application watch mode                                                    *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import android.content.Context;
import android.graphics.Bitmap;

public class Application {
	
	public static void startAppMode() {
		MetaWatchService.WatchModes.APPLICATION = true;		
	}
	
	public static void stopAppMode(Context context) {
		exitApp(context);
	}
	
	public static void updateAppMode(Context context) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				//Bitmap bitmap = createLcdApp(context);
				Bitmap bitmap = Protocol.createTextBitmap(context, "Starting application mode ...");
				Protocol.sendLcdBitmap(bitmap, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void updateAppMode(Context context, Bitmap bitmap) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdBitmap(bitmap, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void updateAppMode(Context context, int[] array) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdArray(array, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void updateAppMode(Context context, byte[] buffer) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdBuffer(buffer, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	
	
	public static void toApp() {
		MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
		// update screen with cached buffer
		Protocol.updateDisplay(MetaWatchService.WatchBuffers.APPLICATION);
	}
	
	public static void exitApp(Context context) {
		MetaWatchService.WatchModes.APPLICATION = false;
		
		if (MetaWatchService.WatchModes.IDLE == true) {
			Idle.toIdle(context);
		}
	}
	
	

	
}
