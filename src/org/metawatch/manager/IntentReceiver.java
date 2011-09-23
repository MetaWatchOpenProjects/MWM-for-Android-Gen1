                                                                     
                                                                     
                                                                     
                                             
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
  * IntentReceiver.java                                                       *
  * IntentReceiver                                                            *
  * Notifications receiver                                                    *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();		
		//Log.d(MetaWatch.TAG, action);
		
		/*
		Bundle b = intent.getExtras();
		for (String key : b.keySet()) {
			Log.d(MetaWatch.TAG, "extra: " + key);
        }		
		Log.d(MetaWatch.TAG, intent.getDataString());
		*/
		
		if (action.equals("android.intent.action.PROVIDER_CHANGED")) {
			
			if (!MetaWatchService.Preferences.notifyGmail)
				return;
			
			if (!Utils.isGmailAccessSupported(context)) {
				String recipient = "You";
				Bundle bundle = intent.getExtras();
				
				if (bundle.containsKey("account"))
					recipient = bundle.getString("account");
				Log.d(MetaWatch.TAG, "count for " + recipient + ": " + bundle.getInt("count"));
				int count = bundle.getInt("count");				
				Monitors.updateGmailUnreadCount(recipient, count);
				
				if (count > 0)
					NotificationBuilder.createGmailBlank(context, recipient);
				else
					Idle.updateLcdIdle(context);
				return;
			}
		}
		
		if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {		
			
			if (!MetaWatchService.Preferences.notifySMS)
				return;
			
			Bundle bundle = intent.getExtras();
			if (bundle.containsKey("pdus")) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] smsMessage = new SmsMessage[pdus.length];
				for (int i = 0; i < smsMessage.length; i++) {
					smsMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					String number = smsMessage[i].getOriginatingAddress();
					String body = smsMessage[i].getDisplayMessageBody();
					
					NotificationBuilder.createSMS(context, number, body);
				}
			}
			return;
		}
		
		if (action.equals("com.fsck.k9.intent.action.EMAIL_RECEIVED")) {
			
			if (!MetaWatchService.Preferences.notifyK9)
				return;
			
			Bundle bundle = intent.getExtras();				
			String subject = bundle.getString("com.fsck.k9.intent.extra.SUBJECT");
			String sender = bundle.getString("com.fsck.k9.intent.extra.FROM");
			NotificationBuilder.createK9(context, sender, subject);
			return;
		}
				
		if (action.equals("com.android.alarmclock.ALARM_ALERT") || action.equals("com.htc.android.worldclock.ALARM_ALERT") || action.equals("com.android.deskclock.ALARM_ALERT") || action.equals("com.sonyericsson.alarm.ALARM_ALERT") ) {
			
			if (!MetaWatchService.Preferences.notifyAlarm)
				return;
			
			NotificationBuilder.createAlarm(context);
			return;
		}
		
		if (intent.getAction().equals("com.android.music.metachanged") || intent.getAction().equals("com.htc.music.metachanged"))
		//if (intent.getAction().equals("com.android.music.metachanged") || intent.getAction().equals("com.htc.music.metachanged") || intent.getAction().equals("com.android.music.playstatechanged") || intent.getAction().equals("com.htc.music.playstatechanged"))  
		{	
			if (!MetaWatchService.Preferences.notifyMusic)
				return;
			
			String artist = "";
			String track = "";
			
			if (intent.hasExtra("artist"))
				artist = intent.getStringExtra("artist");
			if (intent.hasExtra("track"))
				track = intent.getStringExtra("track");
			
			NotificationBuilder.createMusic(context, artist, track);
			return;
		}
		
	}

}
