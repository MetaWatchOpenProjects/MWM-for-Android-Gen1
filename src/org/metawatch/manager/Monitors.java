                                                                     
                                                                     
                                                                     
                                             
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
  * Monitors.java                                                             *
  * Monitors                                                                  *
  * Starting notifications and updates                                        *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.anddev.android.weatherforecast.weather.GoogleWeatherHandler;
import org.anddev.android.weatherforecast.weather.WeatherCurrentCondition;
import org.anddev.android.weatherforecast.weather.WeatherForecastCondition;
import org.anddev.android.weatherforecast.weather.WeatherSet;
import org.anddev.android.weatherforecast.weather.WeatherUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


public class Monitors {
	
	public static AlarmManager alarmManager;
	public static Intent intent;
	public static PendingIntent sender;
	
	static GmailMonitor gmailMonitor;
	
	private static ContentObserverMessages contentObserverMessages;
	static ContentResolver contentResolverMessages;
	
	private static ContentObserverCalls contentObserverCalls;
	static ContentResolver contentResolverCalls;
	
	//public static int gmailCount = 0;
	static Hashtable<String, Integer> gmailUnreadCounts = new Hashtable<String, Integer>();
	
	public static LocationManager locationManager;
	public static String locationProvider;
	
	private static NetworkLocationListener networkLocationListener;
	
	public static Boolean useGeolocation = true;
	
	public static class WeatherData {
		public static boolean updating = false;
		public static boolean received = false;
		public static String icon;
		public static String tempHigh;
		public static String tempLow;
		public static String temp;
		public static String condition;
		public static String locationName;
	}
	
	public static class LocationData {
		public static boolean received = false;
	    public static double latitude;
	    public static double longitude;
	}
	
	public static void updateGmailUnreadCount(String account, int count) {
		Log.d(MetaWatch.TAG, "Monitors.updateGmailUnreadCount(): account='"
				+ account + "' count='" + count + "'");
		gmailUnreadCounts.put(account, count);
		Log.d(MetaWatch.TAG,
				"Monitors.updateGmailUnreadCount(): new unread count is: "
						+ gmailUnreadCounts.get(account));
	}
	
	public static int getGmailUnreadCount() {
		Log.d(MetaWatch.TAG, "Monitors.getGmailUnreadCount()");
		int totalCount = 0;
		for (String key : gmailUnreadCounts.keySet()) {
			Integer accountCount = gmailUnreadCounts.get(key);
			totalCount += accountCount.intValue();
			Log.d(MetaWatch.TAG, "Monitors.getGmailUnreadCount(): account='"
					+ key + "' accountCount='" + accountCount
					+ "' totalCount='" + totalCount + "'");
		}
		return totalCount;
	}
	
	public static int getGmailUnreadCount(String account) {
		int count = gmailUnreadCounts.get(account);
		Log.d(MetaWatch.TAG, "Monitors.getGmailUnreadCount('"+account+"') returning " + count);
		return count;
	}
	
	public static void start(Context context, TelephonyManager telephonyManager) {
		// start weather updater
		
		Log.d(MetaWatch.TAG,
				"Monitors.start()");
				
		if (useGeolocation) {
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			locationProvider = LocationManager.NETWORK_PROVIDER;
			
			networkLocationListener = new NetworkLocationListener(context);
			
			locationManager.requestLocationUpdates(locationProvider, 30 * 1000, 0, networkLocationListener);
		}
		
		
		CallStateListener phoneListener = new CallStateListener(context);
		
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int phoneEvents = PhoneStateListener.LISTEN_CALL_STATE;
		telephonyManager.listen(phoneListener, phoneEvents);
		
		if (Utils.isGmailAccessSupported(context)) {
			gmailMonitor = new GmailMonitor(context);
			gmailMonitor.startMonitor();
		}
		
		try {
			contentObserverMessages = new ContentObserverMessages(context);
			Uri uri = Uri.parse("content://mms-sms/conversations/");
			contentResolverMessages = context.getContentResolver();
			contentResolverMessages.registerContentObserver(uri, true, contentObserverMessages);
		} catch (Exception x) {
		}
		
		try {
			contentObserverCalls = new ContentObserverCalls(context);
			//Uri uri = Uri.parse("content://mms-sms/conversations/");
			contentResolverCalls = context.getContentResolver();
			contentResolverCalls.registerContentObserver(android.provider.CallLog.Calls.CONTENT_URI, true, contentObserverCalls);
		} catch (Exception x) {
		}
		
		// temporary one time update
		updateWeatherData(context);
		
		startAlarmTicker(context);
	}
	
	public static void stop() {
		
		Log.d(MetaWatch.TAG,
				"Monitors.stop()");
		
		contentResolverMessages.unregisterContentObserver(contentObserverMessages);
		if (useGeolocation) {
			locationManager.removeUpdates(networkLocationListener);
		}
		stopAlarmTicker();		
	}
	
	private static synchronized void updateWeatherDataGoogle(Context context) {
		try {

			Log.d(MetaWatch.TAG,
					"Monitors.updateWeatherData(): start");
			
			URL url;
			String queryString = "http://www.google.com/ig/api?weather="
					+ Preferences.weatherCity;
			url = new URL(queryString.replace(" ", "%20"));

			WeatherData.locationName = Preferences.weatherCity; 
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			GoogleWeatherHandler gwh = new GoogleWeatherHandler();
			xr.setContentHandler(gwh);
			xr.parse(new InputSource(url.openStream()));
			WeatherSet ws = gwh.getWeatherSet();
			WeatherCurrentCondition wcc = ws
					.getWeatherCurrentCondition();

			// IndexOutOfBoundsException: Invalid index 0, size is 0
			WeatherForecastCondition wfc = ws
					.getWeatherForecastConditions().get(0);

			String cond = wcc.getCondition();
			String temp;
			if (Preferences.weatherCelsius) {
				WeatherData.tempHigh = 
						  Integer.toString(wfc.getTempMaxCelsius());
				WeatherData.tempLow = 
						  Integer.toString(wfc.getTempMinCelsius());
				temp = Integer.toString(wcc.getTempCelcius());
			} else {
				WeatherData.tempHigh = 
						  Integer.toString(WeatherUtils
								.celsiusToFahrenheit(wfc
										.getTempMaxCelsius()));
				WeatherData.tempLow = 
						  Integer.toString(WeatherUtils
								.celsiusToFahrenheit(wfc
										.getTempMinCelsius()));
				temp = Integer.toString(wcc.getTempFahrenheit());
			}
			// String place = gwh.city
			
			WeatherData.condition = cond;
			WeatherData.temp = temp;

			cond = cond.toLowerCase();

			if (cond.equals("clear") || cond.equals("mostly sunny")
					|| cond.equals("partly sunny")
					|| cond.equals("sunny"))
				WeatherData.icon = "weather_sunny.bmp";
			else if (cond.equals("cloudy")
					|| cond.equals("mostly cloudy")
					|| cond.equals("overcast")
					|| cond.equals("partly cloudy"))
				WeatherData.icon = "weather_cloudy.bmp";
			else if (cond.equals("light rain") || cond.equals("rain")
					|| cond.equals("rain showers")
					|| cond.equals("showers")
					|| cond.equals("chance of showers")
					|| cond.equals("scattered showers")
					|| cond.equals("freezing rain")
					|| cond.equals("freezing drizzle")
					|| cond.equals("rain and snow"))
				WeatherData.icon = "weather_rain.bmp";
			else if (cond.equals("thunderstorm")
					|| cond.equals("chance of storm")
					|| cond.equals("isolated thunderstorms"))
				WeatherData.icon = "weather_thunderstorm.bmp";
			else if (cond.equals("chance of snow")
					|| cond.equals("snow showers")
					|| cond.equals("ice/snow")
					|| cond.equals("flurries"))
				WeatherData.icon = "weather_snow.bmp";
			else
				WeatherData.icon = "weather_cloudy.bmp";

			WeatherData.received = true;

			Idle.updateLcdIdle(context);

		} catch (Exception e) {
			Log.e(MetaWatch.TAG, "Exception while retreiving weather", e);
		} finally {
			Log.d(MetaWatch.TAG,
					"Monitors.updateWeatherData(): finish");
		}
		
	}
	
	private static synchronized void updateWeatherDataWunderground(Context context) {
		try {

			if (WeatherData.updating)
				return;
			
			WeatherData.updating = true;
			
			Log.d(MetaWatch.TAG,
					"Monitors.updateWeatherData(): start");
			

			if (LocationData.received && Preferences.wundergroundKey != "") {
				
				String latLng = Double.toString(LocationData.latitude)+","+Double.toString(LocationData.longitude);
			
				JSONObject json = getJSONfromURL( "http://api.wunderground.com/api/"+Preferences.wundergroundKey+"/geolookup/conditions/forecast/q/"+latLng+".json" );
				JSONObject location = json.getJSONObject("location");
				JSONObject current = json.getJSONObject("current_observation");
				
				JSONObject forecast = json.getJSONObject("forecast");
				JSONObject today = forecast.getJSONObject("simpleforecast").getJSONArray("forecastday").getJSONObject(0);
				
				WeatherData.locationName = location.getString("city");			
				WeatherData.condition = current.getString("weather");
				
				String cond = current.getString("icon");
				
				if (cond.equals("clear") 
						|| cond.equals("sunny")
						|| cond.equals("partlysunny")
						|| cond.equals("mostlysunny"))
					WeatherData.icon = "weather_sunny.bmp";
				else if (cond.equals("cloudy")
						|| cond.equals("partlycloudy")
						|| cond.equals("mostlycloudy"))
					WeatherData.icon = "weather_cloudy.bmp";
				if (cond.equals("rain") 
						|| cond.equals("chancerain"))
					WeatherData.icon = "weather_rain.bmp";
				if (cond.equals("fog") 
						|| cond.equals("hazy"))
					WeatherData.icon = "weather_fog.bmp";
				if (cond.equals("tstorms") 
						|| cond.equals("chancetstorms"))
					WeatherData.icon = "weather_thunderstorm.bmp";
				if (cond.equals("snow") 
						|| cond.equals("chancesnow")
						|| cond.equals("sleet")
						|| cond.equals("chancesleet")
						|| cond.equals("flurries")
						|| cond.equals("chanceflurries"))
					WeatherData.icon = "weather_snow.bmp";
				else
					WeatherData.icon = "weather_cloudy.bmp";
				
				if (Preferences.weatherCelsius) {
					WeatherData.temp = current.getString("temp_c");
					WeatherData.tempLow = today.getJSONObject("low").getString("celsius");
					WeatherData.tempHigh= today.getJSONObject("high").getString("celsius");
				}
				else {
					WeatherData.temp = current.getString("temp_f");
					WeatherData.tempLow = today.getJSONObject("low").getString("fahrenheit");
					WeatherData.tempHigh= today.getJSONObject("high").getString("fahrenheit");
				}
				WeatherData.icon = "weather_cloudy.bmp";
			
				WeatherData.received = true;
				
				Idle.updateLcdIdle(context);
	
		    }

			
		} catch (Exception e) {
			Log.e(MetaWatch.TAG, "Exception while retreiving weather", e);
		} finally {
			Log.d(MetaWatch.TAG,
					"Monitors.updateWeatherData(): finish");
			
			WeatherData.updating = false;
			
		}
	}

	public static void updateWeatherData(final Context context) {
		Thread thread = new Thread("WeatherUpdater") {
			@Override
			public void run() {
				if (useGeolocation) {
					updateWeatherDataWunderground(context);
				}
				else {
					updateWeatherDataGoogle(context);
				}
				
			}
		};
		thread.start();
	}
	
	static void startAlarmTicker(Context context) {		
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("action_update", "update");
		sender = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, 30 * 60 * 1000, sender);		
	}
	
	static void stopAlarmTicker() {
		alarmManager.cancel(sender);
	}
	
	private static class ContentObserverMessages extends ContentObserver {

		Context context;
		
		public ContentObserverMessages(Context context) {
			super(null);
			this.context = context;			
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);			
			// change in SMS/MMS database			
			Idle.updateLcdIdle(context);
		}
	}
	
	private static class ContentObserverCalls extends ContentObserver {

		Context context;
		
		public ContentObserverCalls(Context context) {
			super(null);
			this.context = context;			
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);			
			// change in call history database
			Log.d(MetaWatch.TAG, "call history change");
			Idle.updateLcdIdle(context);
		}
	}
	
	private static class NetworkLocationListener implements LocationListener {

		Context context;
		
		public NetworkLocationListener(Context context) {
			this.context = context;
		}
		
		public void onLocationChanged(Location location) {
					
			locationManager.removeUpdates(this);

			LocationData.latitude = location.getLatitude();
			LocationData.longitude = location.getLongitude();
			
			Log.d(MetaWatch.TAG, "location changed "+location.toString() );
							
			if (!LocationData.received && !WeatherData.updating) {
				LocationData.received = true;
				Log.d(MetaWatch.TAG, "First location - getting weather");
				Monitors.updateWeatherData(context);
				locationManager.requestLocationUpdates(locationProvider, 60 * 60 * 1000, 1500, networkLocationListener);

			}
			
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	//http://p-xr.com/android-tutorial-how-to-parse-read-json-data-into-a-android-listview/
	public static JSONObject getJSONfromURL(String url){

		//initialize
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}

		//try parse the string to a JSON object
		try{
	        	jArray = new JSONObject(result);
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return jArray;
	}

	
}
