                                                                     
                                                                     
                                                                     
                                             
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
  * CallVibrate.java                                                          *
  * CallVibrate                                                               *
  * While in incoming call mode                                               *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.WatchType;

public class CallVibrate implements Runnable {

	public void run() {
		
		while (Call.isRinging) {
			Protocol.vibrate(1000, 0, 1);
			if (MetaWatchService.watchType == WatchType.DIGITAL)
				Protocol.updateDisplay(2);
			else
				Protocol.updateOledsNotification();
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
		
	}

}
