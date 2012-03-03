                                                                     
                                                                     
                                                                     
                                             
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
  * Test.java                                                                 *
  * Test                                                                      *
  * Activity for sending test commands                                        *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import java.io.IOException;
import java.util.Random;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WatchType;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;
import org.metawatch.manager.Notification.VibratePattern;

import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class Test extends PreferenceActivity {
	
	Context context;
	PreferenceScreen preferenceScreen;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		
		addPreferencesFromResource(R.layout.test);
		
		preferenceScreen = getPreferenceScreen();
	}
	
	@Override
	protected void onStart() {
		
		preferenceScreen.findPreference("calendar").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createCalendar(context, "Tea with the Hatter - Windmill");
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("notification").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				if (MetaWatchService.watchType == WatchType.DIGITAL) {
					//NotificationBuilder.createSmart(context, "Notification", ipsum);
					Notification.addTextNotification(context, "Notification",
							new VibratePattern(true, 500, 500, 3), Notification.getDefaultNotificationTimeout(context));
				} else {
					Notification.addOledNotification(context, Protocol
							.createOled2lines(context, "Display A, line 1",
									"Display A, line 2"), Protocol
							.createOled2lines(context, "Display B, line 1",
									"Display B, line 2"), null, 0, null);
					if (Preferences.logging) Log.d(MetaWatch.TAG, "Notification timeout is: " + Notification.getDefaultNotificationTimeout(context));
					
				}
				return true;
			}
		});
	
		preferenceScreen.findPreference("application_start").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				if (MetaWatchService.watchType == WatchType.DIGITAL)
					Application.startAppMode();
				return true;
			}
		});
	        
		preferenceScreen.findPreference("sms_start").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				startSmsTestLoop(context);
				return true;
			}
		});     
	         
		preferenceScreen.findPreference("sms_stop").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	stopSmsTestLoop();
		    	return true;
			}
		});   
	        
		preferenceScreen.findPreference("application_update").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	if (MetaWatchService.watchType == WatchType.DIGITAL)
		    		Application.updateAppMode(context);
		    	return true;
			}
		});
       
		preferenceScreen.findPreference("application_stop").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	if (MetaWatchService.watchType == WatchType.DIGITAL)
		    		Application.stopAppMode(context);
		    	return true;
			}
		});      
	        
		preferenceScreen.findPreference("sms").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	String smsText = "";
		    	for(int i=0;i<20;++i) {
		    		smsText += "SMS Line "+i+"\n";
		    	}
		    	NotificationBuilder.createSMS(context, "555-123-4567", smsText);
		    	return true;
			}
		});
    	           
		preferenceScreen.findPreference("testShortMessage").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createSMS(context, "555-123-4567", "Hi.");
		    	return true;
			}
		});
       
		preferenceScreen.findPreference("k9").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
			   	NotificationBuilder.createK9(context, "The Doctor <doctor@gallifrey.net>", "Now drop your weapons, or I'll kill him with context deadly jelly baby!", "tardis:INBOX");
			   	return true;
			}
		});
 
		preferenceScreen.findPreference("gmail_short").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createGmailBlank(context, "me@gmail.com", 513);
		    	return true;
			}
		});
		 
		preferenceScreen.findPreference("gmail_full").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createGmail(context, "bruce@wayneenterprises.com", "me@gmail.com", "Need a ride", "Alfred, would you bring the car around to the docks?");
		    	return true;
			}
		});
    	   
		preferenceScreen.findPreference("alarm").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createAlarm(context);
		    	return true;
			}
		});
    	   
		preferenceScreen.findPreference("timezone").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createTimezonechange(context);
		    	return true;
			}
		});
    	   
		preferenceScreen.findPreference("Batterylow").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createBatterylow(context);
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("music").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createMusic(context, "Park", "Who is Aliandra", "Building a Better");
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("winamp").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	NotificationBuilder.createWinamp(context, "Winamp", "It really whips the llama's...", "One Hump or Two");
		    	return true;
			}
		});
	   
		preferenceScreen.findPreference("call_start").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	Call.startCall(context, "555-123-4567");
		    	return true;
			}
		});

		preferenceScreen.findPreference("call_stop").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	Call.endCall(context);
		    	return true;
			}
		});

		preferenceScreen.findPreference("vibrate").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	Protocol.vibrate(300, 500, 3);
		    	return true;
			}
		});

		preferenceScreen.findPreference("set_rtc").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	Protocol.sendRtcNow(context);
		    	return true;
			}
		});

		preferenceScreen.findPreference("load_template").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	if (MetaWatchService.watchType == WatchType.DIGITAL)
		    		Protocol.loadTemplate(0);

		    	return true;
			}
		});
		
		preferenceScreen.findPreference("update_display").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	if (MetaWatchService.watchType == WatchType.DIGITAL)
		    		Protocol.updateDisplay(0);
		    	return true;
			}
		});
    	
		preferenceScreen.findPreference("write_buffer").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	if (MetaWatchService.watchType == WatchType.DIGITAL)
		    		Protocol.writeBuffer();
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("test").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	//Protocol.test(context);
		    	//NotificationBuilder.createSMS(context, "555-123-4567", "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20.");
		    	//Protocol.setNvalTime(true);
		    	//Protocol.configureMode();
		    	//Protocol.readButtonConfiguration();
		    	//Protocol.enableMediaButtons();
		    	//Protocol.queryNvalTime();
		    	/*
		    	if (Preferences.logging) Log.d(MetaWatch.TAG, "sending notif test");
				Protocol.loadTemplate(2);
				Protocol.sendLcdBitmap(Protocol.createTextBitmap(context, "abc"), true);
				Protocol.updateDisplay(2);
				*/

		    	//Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
		    	//KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
		    	//intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
		    	//sendOrderedBroadcast(intent, null);
		    	
		    	//Protocol.stopProtocolSender();
				
		    	return true;		    	
			}
		});
		
		preferenceScreen.findPreference("refresh_location").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				Monitors.RefreshLocation();
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("refresh_weather").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	WeatherData.timeStamp = 0;
				Monitors.updateWeatherData(context);
		    	return true;
			}
		});

		preferenceScreen.findPreference("random_location").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		       	Random rnd = new Random();
		    	LocationData.latitude = (rnd.nextDouble()*180.0)-90.0;
		    	LocationData.longitude = (rnd.nextDouble()*360.0)-180.0;
		    	LocationData.timeStamp = System.currentTimeMillis();	
		    	LocationData.received = true;
		    	WeatherData.timeStamp = 0;
		    	Monitors.updateWeatherData(context);
		    	return true;
			}
		});
	
		preferenceScreen.findPreference("dump_hprof").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	try {
					Debug.dumpHprofData("/sdcard/metawatch.hprof");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	return true;
			}
		});  

		preferenceScreen.findPreference("led_on").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
			 	Protocol.ledChange(true);
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("led_off").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				Protocol.ledChange(false);
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("time_24hr").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
				Protocol.setNvalTime(true);
			   	//NotificationBuilder.createOtherNotification(context, "", "You'll need to reset your watch for this to take effect.");
		    	return true;
			}
		});
		
 		preferenceScreen.findPreference("media_next").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	MediaControl.next(context);
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("media_previous").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	MediaControl.previous(context);
		    	return true;
			}
		});
		
		preferenceScreen.findPreference("media_togglepause").setOnPreferenceClickListener(new OnPreferenceClickListener() {	
			public boolean onPreferenceClick(Preference arg0) {
		    	MediaControl.togglePause(context);
		    	return true;
			}
		});
			
		super.onStart();
	}
	
	void startSmsTestLoop(final Context context) {
		if (MetaWatchService.testSmsLoop != null)
			MetaWatchService.testSmsLoop.stop();
		MetaWatchService.testSmsLoop = new TestSmsLoop(context);
		Thread thread = new Thread(MetaWatchService.testSmsLoop);
		thread.start();
	}
	
	void stopSmsTestLoop() {
		if (MetaWatchService.testSmsLoop != null)
			MetaWatchService.testSmsLoop.stop();
	}
	

}
