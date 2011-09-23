                                                                     
                                                                     
                                                                     
                                             
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
  * TestSmsLoop.java                                                          *
  * TestSmsLoop                                                               *
  * Sending test SMS in a loop                                                *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import android.content.Context;

public class TestSmsLoop implements Runnable {

	Context context;
	boolean runLoop;
	
	public TestSmsLoop(Context context) {
		this.context = context;
	}

	public void run() {
		runLoop = true;
		for (int i = 1; runLoop; i++) {
			NotificationBuilder.createSMS(context, "123-456-789", "\n  Test SMS #" + i);
			try {
				Thread.sleep(MetaWatchService.Preferences.smsLoopInterval*1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public void stop() {
		runLoop = false;
	}

}
