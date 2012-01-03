                                                                     
                                                                     
                                                                     
                                             
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

import org.metawatch.manager.MetaWatchService.WatchType;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;
import org.metawatch.manager.Notification.VibratePattern;

import android.app.Activity;
import android.content.Context;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class Test extends Activity {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.test, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.calendar:	   
	    	NotificationBuilder.createCalendar(this, "Tea with the Hatter - Windmill");
	        return true;
		case R.id.notification:
			if (MetaWatchService.watchType == WatchType.DIGITAL) {
				//NotificationBuilder.createSmart(this, "Notification", ipsum);
				Notification.addTextNotification(this, "Notification",
						new VibratePattern(true, 500, 500, 3), Notification.getDefaultNotificationTimeout(this));
			} else {
				Notification.addOledNotification(this, Protocol
						.createOled2lines(this, "Display A, line 1",
								"Display A, line 2"), Protocol
						.createOled2lines(this, "Display B, line 1",
								"Display B, line 2"), null, 0, null);
				Log.d(MetaWatch.TAG, "Notification timeout is: " + Notification.getDefaultNotificationTimeout(this));
				
			}
			return true;
	    case R.id.application_start:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Application.startAppMode();
	        return true;
	    case R.id.sms_start:   
	    	startSmsTestLoop(this);
	        return true;
	    case R.id.sms_stop:   
	    	stopSmsTestLoop();
	        return true;
	    case R.id.application_update:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Application.updateAppMode(this);
	        return true;
	    case R.id.application_stop:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Application.stopAppMode(this);
	        return true;
	    case R.id.sms:	   
	    	String smsText = "";
	    	for(int i=0;i<20;++i) {
	    		smsText += "SMS Line "+i+"\n";
	    	}
	    	NotificationBuilder.createSMS(this, "555-123-456", smsText);
	        return true;
	    case R.id.testShortMessage:	   
	    	NotificationBuilder.createSMS(this, "555-123-456", "Hi.");
	        return true;
	    case R.id.k9:	   
	    	NotificationBuilder.createK9(this, "The Doctor <doctor@gallifrey.net>", "Now drop your weapons, or I'll kill him with this deadly jelly baby!", "tardis:INBOX");
	        return true;
	    case R.id.gmail_short:	   
	    	NotificationBuilder.createGmailBlank(this, "me@gmail.com", 513);
	        return true;
	    case R.id.gmail_full:	   
	    	NotificationBuilder.createGmail(this, "bruce@wayneenterprises.com", "me@gmail.com", "Need a ride", "Alfred, would you bring the car around to the docks?");
	        return true;
	    case R.id.alarm:	   
	    	NotificationBuilder.createAlarm(this);
	        return true;
	    case R.id.timezone:	   
	    	NotificationBuilder.createTimezonechange(this);
	        return true;	        
	    case R.id.Batterylow:	   
	    	NotificationBuilder.createBatterylow(this);
	        return true;	        
	    case R.id.music:	   
	    	NotificationBuilder.createMusic(this, "Park", "Who is Aliandra", "Building a Better");
	        return true;
	    case R.id.winamp:	   
	    	NotificationBuilder.createWinamp(this, "Winamp", "It really whips the llama's...", "One Hump or Two");
	        return true;
	    case R.id.call_start:	   
	    	Call.startCall(this, "555-123-4567");
	        return true;
	    case R.id.call_stop:	   
	    	Call.endCall(this);
	        return true;
	    case R.id.vibrate:	   
	    	Protocol.vibrate(300, 500, 3);
	        return true;
	    case R.id.set_rtc:	   
	    	Protocol.sendRtcNow(this);
	        return true;
	    case R.id.load_template:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Protocol.loadTemplate(0);
	        return true;
	    case R.id.activate_buffer:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Protocol.activateBuffer(0);
	        return true;
	    case R.id.update_display:
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Protocol.updateDisplay(0);
	        return true;
	    case R.id.write_bufer:	 
	    	if (MetaWatchService.watchType == WatchType.DIGITAL)
	    		Protocol.writeBuffer();
	        return true;
	    case R.id.test: {
	    	//Protocol.test(this);
	    	//NotificationBuilder.createSMS(this, "555-123-4567", "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20.");
	    	//Protocol.setNvalTime(true);
	    	//Protocol.configureMode();
	    	//Protocol.readButtonConfiguration();
	    	//Protocol.enableMediaButtons();
	    	//Protocol.queryNvalTime();
	    	/*
	    	Log.d(MetaWatch.TAG, "sending notif test");
			Protocol.loadTemplate(2);
			Protocol.sendLcdBitmap(Protocol.createTextBitmap(this, "abc"), true);
			Protocol.updateDisplay(2);
			*/

	    	//Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
	    	//KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
	    	//intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
	    	//sendOrderedBroadcast(intent, null);
	    	
	    	Protocol.stopProtocolSender();

	    	
	    }
	        return true;
	        
	    case R.id.refresh_location:
			Monitors.RefreshLocation();
	    	return true;
	    case R.id.refresh_weather:
	    	WeatherData.timeStamp = 0;
			Monitors.updateWeatherData(this);
	    	return true;
	    case R.id.random_location:
	       	Random rnd = new Random();
	    	LocationData.latitude = (rnd.nextDouble()*180.0)-90.0;
	    	LocationData.longitude = (rnd.nextDouble()*360.0)-180.0;
	    	LocationData.timeStamp = System.currentTimeMillis();	
	    	LocationData.received = true;
	    	WeatherData.timeStamp = 0;
	    	Monitors.updateWeatherData(this);
	    	return true;
	    
	    case R.id.dump_hprof:
	    	try {
				Debug.dumpHprofData("/sdcard/metawatch.hprof");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return true;
	    	
	    case R.id.led_on:
	    	Protocol.ledChange(true);
	    	return true;

	    case R.id.led_off:
	    	Protocol.ledChange(false);
	    	return true;
	    	
	    case R.id.time_24hr:
	    	Protocol.setNvalTime(true);
	    	NotificationBuilder.createOtherNotification(this, "", "You'll need to reset your watch for this to take effect.");
	    	return true;
	    		    	
	    default:
	        return super.onOptionsItemSelected(item);
	    }
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
