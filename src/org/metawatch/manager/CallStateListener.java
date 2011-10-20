                                                                     
                                                                     
                                                                     
                                             
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
  * CallStateListener.java                                                    *
  * CallStateListener                                                         *
  * Listener waiting for incoming call                                        *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


class CallStateListener extends PhoneStateListener {
	
	Context context;
	
	public CallStateListener(Context ctx) {
		super();
		context = ctx;
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {
		super.onCallStateChanged(state, incomingNumber);
		
		if (!MetaWatchService.Preferences.notifyCall)
			return;
		
		if (incomingNumber == null)
			incomingNumber = "";

		switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: 
				//String name = Utils.getContactNameFromNumber(context, incomingNumber);	
				//SendCommand.sendIncomingCallStart(incomingNumber, name, photo);
				Call.startCall(context, incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_IDLE: 
				Call.endCall(context);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: 
				Call.endCall(context);
				break;
		}

	}
}
