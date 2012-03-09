
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
 * MetaWatchService.java                                                     *
 * MetaWatchService                                                          *
 * Always connected BT watch service                                         *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.metawatch.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.metawatch.manager.Notification.VibratePattern;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MetaWatchService extends Service {

	/* TODO: Making this static will break if we want multiple MWMs running. */
	static volatile Context context;
	
	public static BluetoothAdapter bluetoothAdapter;
	BluetoothSocket bluetoothSocket;
	static InputStream inputStream;
	static OutputStream outputStream;

	TelephonyManager telephonyManager;
	AudioManager audioManager;
	NotificationManager notificationManager;
	android.app.Notification notification;

	public static PowerManager powerManger;
	public static PowerManager.WakeLock wakeLock;

	public static volatile int connectionState;
	public static int watchType;
	public static int watchState;

	public static TestSmsLoop testSmsLoop;
	private boolean lastConnectionState = false;

	final class ConnectionState {
		static final int DISCONNECTED = 0;
		static final int CONNECTING = 1;
		static final int CONNECTED = 2;
		static final int DISCONNECTING = 3;
	}

	final class WatchBuffers {
		static final int IDLE = 0;
		static final int APPLICATION = 1;
		static final int NOTIFICATION = 2;
	}

	final class WatchStates {
		static final int OFF = 0;
		static final int IDLE = 1;
		static final int APPLICATION = 2;
		static final int NOTIFICATION = 3;
		static final int CALL = 3;
	}
	
	final class Msg {
		static final int REGISTER_CLIENT = 0;
		static final int UNREGISTER_CLIENT = 1;
		static final int UPDATE_STATUS = 2;
		static final int STOP_SERVICE = 3;
	}
	
	public final class WeatherProvider {
		public static final int DISABLED = 0;
		public static final int GOOGLE = 1;
		public static final int WUNDERGROUND = 2;
	}

	static class WatchModes {
		public static volatile boolean IDLE = false;
		public static volatile boolean APPLICATION = false;
		public static volatile boolean NOTIFICATION = false;
		public static volatile boolean CALL = false;
	}

	public static class Preferences {
		public static boolean loaded = false;
		public static boolean logging = true;
		
		public static boolean notifyCall = true;
		public static boolean notifySMS = true;
		public static boolean notifyGmail = true;
		public static boolean notifyK9 = true;
		public static boolean notifyAlarm = true;
		public static boolean notifyBatterylow = true;
		public static boolean notifyMusic = true;
		public static boolean notifyCalendar = true;
		public static String watchMacAddress = "";
		public static int packetWait = 10;
		public static boolean skipSDP = false;
		public static boolean invertLCD = false;
		public static boolean notificationCenter = false;
		public static boolean notifyLight = false;
		public static boolean stickyNotifications = true;
		public static int weatherProvider = WeatherProvider.GOOGLE;
		public static String weatherCity = "Dallas,US";
		public static boolean weatherCelsius = false;
		public static boolean weatherGeolocation = false;
		public static String wundergroundKey = "";
		public static int fontSize = 2;
		public static int smsLoopInterval = 15;
		public static boolean idleMusicControls = false;
		public static int idleMusicControlMethod = MediaControl.MUSICSERVICECOMMAND;
		public static boolean idleReplay = false;
		public static boolean notificationLarger = false;
		public static boolean autoConnect = false;
		public static boolean autoRestart = false;
		public static String widgets = "weather_96_32|missedCalls_24_32,unreadSms_24_32,unreadGmail_24_32";
		public static boolean hapticFeedback = false;
		public static boolean readCalendarDuringMeeting = true;
		public static int readCalendarMinDurationToMeetingEnd = 15;
		public static boolean displayLocationInSmallCalendarWidget = false;
		public static boolean displayWidgetRowSeparator = false;
		public static boolean overlayWeatherText = false;
	}

	final class WatchType {
		static final int ANALOG = 1;
		static final int DIGITAL = 2;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	public static void loadPreferences(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		Preferences.logging = sharedPreferences.getBoolean("logging",
				Preferences.logging);
		Preferences.notifyCall = sharedPreferences.getBoolean("NotifyCall",
				Preferences.notifyCall);
		Preferences.notifySMS = sharedPreferences.getBoolean("NotifySMS",
				Preferences.notifySMS);
		Preferences.notifyGmail = sharedPreferences.getBoolean("NotifyGmail",
				Preferences.notifyGmail);
		Preferences.notifyK9 = sharedPreferences.getBoolean("NotifyK9",
				Preferences.notifyK9);
		Preferences.notifyAlarm = sharedPreferences.getBoolean("NotifyAlarm",
				Preferences.notifyAlarm);
		Preferences.notifyMusic = sharedPreferences.getBoolean("NotifyMusic",
				Preferences.notifyMusic);
		Preferences.notifyCalendar = sharedPreferences.getBoolean(
				"NotifyCalendar", Preferences.notifyCalendar);
		Preferences.watchMacAddress = sharedPreferences.getString("MAC",
				Preferences.watchMacAddress);
		Preferences.skipSDP = sharedPreferences.getBoolean("SkipSDP",
				Preferences.skipSDP);
		Preferences.invertLCD = sharedPreferences.getBoolean("InvertLCD",
				Preferences.invertLCD);
		Preferences.notificationCenter = sharedPreferences.getBoolean(
				"notificationCenter", Preferences.notificationCenter);
		Preferences.notifyLight = sharedPreferences.getBoolean("notifyLight",
				Preferences.notifyLight);
		Preferences.stickyNotifications = sharedPreferences.getBoolean(
				"stickyNotifications", Preferences.stickyNotifications);
		Preferences.weatherCity = sharedPreferences.getString("WeatherCity",
				Preferences.weatherCity);	
		Preferences.weatherProvider = Integer.parseInt(
				sharedPreferences.getString("WeatherProvider", 
				Integer.toString(Preferences.weatherProvider)));
		Preferences.weatherCelsius = sharedPreferences.getBoolean(
				"WeatherCelsius", Preferences.weatherCelsius);
		Preferences.weatherGeolocation = sharedPreferences.getBoolean(
				"WeatherGeolocation", Preferences.weatherGeolocation);
		Preferences.wundergroundKey = sharedPreferences.getString(
				"WundergroundKey", Preferences.wundergroundKey);
		Preferences.idleMusicControls = sharedPreferences.getBoolean(
				"IdleMusicControls", Preferences.idleMusicControls);
		Preferences.idleMusicControlMethod = Integer.parseInt(
				sharedPreferences.getString("IdleMusicControlMethod", 
				Integer.toString(Preferences.idleMusicControlMethod)));
		Preferences.idleReplay = sharedPreferences.getBoolean("IdleReplay",
				Preferences.idleReplay);
		Preferences.autoConnect = sharedPreferences.getBoolean(
				"AutoConnect", Preferences.autoConnect);	
		Preferences.autoRestart = sharedPreferences.getBoolean("AutoRestart", 
				Preferences.autoRestart);
		Preferences.widgets = sharedPreferences.getString("widgets",
				Preferences.widgets);
		Preferences.hapticFeedback = sharedPreferences.getBoolean("HapticFeedback",
				Preferences.hapticFeedback);
		Preferences.readCalendarDuringMeeting = sharedPreferences.getBoolean("ReadCalendarDuringMeeting",
				Preferences.readCalendarDuringMeeting);
		Preferences.readCalendarMinDurationToMeetingEnd = Integer.parseInt(
				sharedPreferences.getString("ReadCalendarMinDurationToMeetingEnd", 
				Integer.toString(Preferences.readCalendarMinDurationToMeetingEnd)));
		Preferences.displayLocationInSmallCalendarWidget = sharedPreferences.getBoolean("DisplayLocationInSmallCalendarWidget",
				Preferences.displayLocationInSmallCalendarWidget);
		Preferences.displayWidgetRowSeparator = sharedPreferences.getBoolean("DisplayWidgetRowSeparator",
				Preferences.displayWidgetRowSeparator);
		Preferences.overlayWeatherText = sharedPreferences.getBoolean("OverlayWeatherText",
				Preferences.overlayWeatherText);

		try {
			Preferences.fontSize = Integer.valueOf(sharedPreferences.getString(
					"FontSize", Integer.toString(Preferences.fontSize)));
			Preferences.packetWait = Integer.valueOf(sharedPreferences
					.getString("PacketWait",
							Integer.toString(Preferences.packetWait)));
			Preferences.smsLoopInterval = Integer.valueOf(sharedPreferences
					.getString("SmsLoopInterval",
							Integer.toString(Preferences.smsLoopInterval)));
		} catch (NumberFormatException e) {
		}

	}

	public static void saveMac(Context context, String mac) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();

		editor.putString("MAC", mac);
		editor.commit();
	}
	
	public static void saveWidgets(Context context, String widgets) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();

		editor.putString("widgets", widgets);
		editor.commit();
	}
	
	public void createNotification() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean hideNotificationIcon = sharedPreferences.getBoolean(
				"HideNotificationIcon", false);
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.createNotification(): hideNotificationIcon="
						+ hideNotificationIcon);
		int notificationIcon = (hideNotificationIcon ? R.drawable.transparent_square
				: R.drawable.disconnected);
		notification = new android.app.Notification(notificationIcon, "MetaWatch Manager",
				System.currentTimeMillis());
		notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

		notification.setLatestEventInfo(this, "MetaWatch Manager", "Idle", createNotificationPendingIntent());

		startForeground(1, notification);
	}

	private PendingIntent createNotificationPendingIntent() {
		return PendingIntent.getActivity(this, 0, new Intent(this,
				MetaWatch.class), 0);
	}

	public void updateNotification() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean hideNotificationIcon = sharedPreferences.getBoolean(
				"HideNotificationIcon", false);
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.updateNotification(): hideNotificationIcon="
						+ hideNotificationIcon);
		switch (connectionState) {
		case ConnectionState.CONNECTING:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.disconnected);
			notification.setLatestEventInfo(this, "MetaWatch Manager", "Connecting", createNotificationPendingIntent());
			broadcastConnection(false);
			break;
		case ConnectionState.CONNECTED:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.connected);
			notification.setLatestEventInfo(this, "MetaWatch Manager", "Connected", createNotificationPendingIntent());
			broadcastConnection(true);
			break;
		default:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.disconnected);
			notification.setLatestEventInfo(this, "MetaWatch Manager", "Disconnected", createNotificationPendingIntent());
			broadcastConnection(false);
			break;
		}
		
		startForeground(1, notification);
		notifyClients();
	}

	public void removeNotification() {
		stopForeground(true);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.onCreate()");

		context = this;

		initialize();

	}
	
	private void initialize() {
		if (!Preferences.loaded)
			loadPreferences(context);
		
		createNotification();

		connectionState = ConnectionState.CONNECTING;
		watchState = WatchStates.OFF;
		watchType = WatchType.DIGITAL;

		if (bluetoothAdapter == null)
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		powerManger = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManger.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MetaWatch");

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		Idle.updateLcdIdle(context);
		
		Monitors.start(this, telephonyManager);

		start();

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.onStartCommand()");
		
		if (connectionState == ConnectionState.DISCONNECTED)
			initialize();
		
	    return START_STICKY;
	}

	@Override
	public void onDestroy() {
		disconnectExit();
		super.onDestroy();
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.onDestroy()");

		Monitors.stop(this);
		removeNotification();
		notifyClients();
	}

	void connect(Context context) {

		try {

			if (Preferences.logging) Log.d(MetaWatch.TAG, "Remote device address: "
					+ Preferences.watchMacAddress);
			if (!Preferences.loaded)
				loadPreferences(context);
			BluetoothDevice bluetoothDevice = bluetoothAdapter
					.getRemoteDevice(Preferences.watchMacAddress);

			if (!bluetoothAdapter.isEnabled()) {
				return;
			}

			/*
			 * if (Preferences.logging) Log.d(MetaWatch.TAG, "remote device name: " +
			 * bluetoothDevice.getName()); int bondState =
			 * bluetoothDevice.getBondState(); String bond = ""; switch
			 * (bondState) { case BluetoothDevice.BOND_BONDED: bond = "bonded";
			 * break; case BluetoothDevice.BOND_BONDING: bond = "bonding";
			 * break; case BluetoothDevice.BOND_NONE: bond = "none"; break; }
			 * if (Preferences.logging) Log.d(MetaWatch.TAG, "bond state: " + bond);
			 */

			if (Preferences.skipSDP) {
				Method method = bluetoothDevice.getClass().getMethod(
						"createRfcommSocket", new Class[] { int.class });
				bluetoothSocket = (BluetoothSocket) method.invoke(
						bluetoothDevice, 1);
			} else {
				UUID uuid = UUID
						.fromString("00001101-0000-1000-8000-00805F9B34FB");
				bluetoothSocket = bluetoothDevice
						.createRfcommSocketToServiceRecord(uuid);
			}

			// if (Preferences.logging) Log.d(MetaWatch.TAG, "got Bluetooth socket");
			// if (bluetoothSocket == null)
			// if (Preferences.logging) Log.d(MetaWatch.TAG, "Bluetooth socket is null");

			bluetoothSocket.connect();
			inputStream = bluetoothSocket.getInputStream();
			outputStream = bluetoothSocket.getOutputStream();

			connectionState = ConnectionState.CONNECTED;
			updateNotification();

			Protocol.startProtocolSender();
			//Protocol.setNvalTime(context);
			Protocol.sendRtcNow(context);
			Protocol.getDeviceType();

			Notification.startNotificationSender(this);

			if (Preferences.idleReplay)
				Protocol.enableReplayButton();
			else
				Protocol.disableReplayButton();
			
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			
			boolean displaySplash = sharedPreferences.getBoolean("DisplaySplashScreen", true);
			if (displaySplash) {
				Notification.addBitmapNotification(this, Utils.loadBitmapFromAssets(context, "splash.png"), new VibratePattern(false, 0, 0, 0), 10000);
			}
			
			/* Notify watch on connection if requested. */
			boolean notifyOnConnect = sharedPreferences.getBoolean("NotifyWatchOnConnect", false);
			if (Preferences.logging) Log.d(MetaWatch.TAG, "MetaWatchService.connect(): notifyOnConnect=" + notifyOnConnect);
			if (notifyOnConnect) {
				NotificationBuilder.createOtherNotification(context, "MetaWatch", "Connected");
			}

		} catch (IOException ioexception) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, ioexception.toString());
			// sendToast(ioexception.toString());
		} catch (SecurityException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
		} catch (NoSuchMethodException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
		} catch (IllegalArgumentException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
		} catch (IllegalAccessException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
		} catch (InvocationTargetException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
		}

	}

    /** Keeps track of all current registered clients. */
    static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
  
	private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "handleMessage "+msg);
			switch (msg.what) {
            case Msg.REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case Msg.UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case Msg.STOP_SERVICE:
            	
            	break;
                
            default:
                super.handleMessage(msg);
			}
		}

	};

	/**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(messageHandler);

    public static void notifyClients() {
    	for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                        Msg.UPDATE_STATUS, 0, 0));
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
	
	void disconnect() {
		Protocol.stopProtocolSender();
		Notification.stopNotificationSender();
		try {
			if (outputStream != null)
				outputStream.close();
		} catch (IOException e) {
		}
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e) {
		}
		try {
			if (bluetoothSocket != null)
				bluetoothSocket.close();
		} catch (IOException e) {
		}
		broadcastConnection(false);
	}

	void disconnectExit() {
		connectionState = ConnectionState.DISCONNECTING;
		disconnect();
	}

	void start() {
		Thread thread = new Thread("MetaWatch Service Thread") {
			@Override
			public void run() {
				boolean run = true;
				Looper.prepare();

				while (run) {
					switch (connectionState) {
					case ConnectionState.DISCONNECTED:
						if (Preferences.logging) Log.d(MetaWatch.TAG, "state: disconnected");
						break;
					case ConnectionState.CONNECTING:
						if (Preferences.logging) Log.d(MetaWatch.TAG, "state: connecting");
						// create initial connection or reconnect
						updateNotification();
						connect(context);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ie) {
							/* If we've been interrupted, exit gracefully. */
							run = false;
						}
						break;
					case ConnectionState.CONNECTED:
						if (Preferences.logging) Log.d(MetaWatch.TAG, "state: connected");
						// read from input stream
						readFromDevice();
						break;
					case ConnectionState.DISCONNECTING:
						if (Preferences.logging) Log.d(MetaWatch.TAG, "state: disconnecting");
						// exit
						run = false;
						break;
					}
				}
			}
		};
		thread.start();

		/* DEBUG */
		String voltageFrequencyString = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						"collectWatchVoltage", "0");
		try {
			
			final int voltageFrequency = Integer
					.parseInt(voltageFrequencyString);
			if (voltageFrequency > 0) {
				
				AlarmManager alarmManager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent(context, AlarmReceiver.class);
				intent.putExtra("action_poll_voltage", "poll_voltage");
				PendingIntent sender = PendingIntent.getBroadcast(context, 1,
						intent, PendingIntent.FLAG_UPDATE_CURRENT);
				long sleep = voltageFrequency * 60 * 1000;
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, sleep,
						sender);
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.start(): Set voltage reading every "
								+ sleep + "ms");
			}
			
		} catch (NumberFormatException nfe) {
			if (Preferences.logging) Log.e(MetaWatch.TAG,
					"MetaWatchService.start(): bad voltage frequency string '"
							+ voltageFrequencyString + "'");
		}

	}

	void readFromDevice() {

		try {
			byte[] bytes = new byte[256];
			if (Preferences.logging) Log.d(MetaWatch.TAG, "before blocking read");
			// Do a proper read loop 
			int haveread = 0;
			int lengthtoread = 4;
			while((lengthtoread-haveread) != 0)
			{
			    haveread += inputStream.read(bytes, haveread, lengthtoread-haveread);
			    if(haveread > 1)
			    {
			        lengthtoread = bytes[1];
			    }
			}
			wakeLock.acquire(5000);

			// print received
			String str = "received: ";
			int len = (bytes[1] & 0xFF);
			if (Preferences.logging) Log.d(MetaWatch.TAG, "packet length: " + len);

			for (int i = 0; i < len; i++) {
				// str+= Byte.toString(bytes[i]) + ", ";
				str += "0x"
						+ Integer.toString((bytes[i] & 0xff) + 0x100, 16)
								.substring(1) + ", ";
			}
			if (Preferences.logging) Log.d(MetaWatch.TAG, str);
			/*
			 * switch (bytes[2]) { case eMessageType.GetDeviceTypeResponse.msg:
			 * if (Preferences.logging) Log.d(MetaWatch.TAG, "received: device type response"); break;
			 * case eMessageType.NvalOperationResponseMsg.msg:
			 * if (Preferences.logging) Log.d(MetaWatch.TAG, "received: nval response"); break; case
			 * eMessageType.StatusChangeEvent.msg: if (Preferences.logging) Log.d(MetaWatch.TAG,
			 * "received: status change event"); break; }
			 */
			/*
			 * if (bytes[2] == 0x31) { // nval response if (bytes[3] == 0x00) //
			 * success if (bytes[4] == 0x00) // set to 12 hour format
			 * Protocol.setNvalTime(true); }
			 */
			if (bytes[2] == eMessageType.NvalOperationResponseMsg.msg) {
			    if (Preferences.logging) Log.d(MetaWatch.TAG,
                        "MetaWatchService.readFromDevice(): NvalOperationResponseMsg");
			    // Do something here?
			} else if (bytes[2] == eMessageType.StatusChangeEvent.msg) { // status
																	// change
																	// event
			    if (Preferences.logging) Log.d(MetaWatch.TAG,
                        "MetaWatchService.readFromDevice(): status change");
				if (bytes[4] == 0x11) {
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): scroll request notification");

					synchronized (Notification.scrollRequest) {
						Notification.scrollRequest.notify();
					}
				} else if (bytes[4] == 0x10) {
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): scroll complete.");
				}
			}

			else if (bytes[2] == eMessageType.ButtonEventMsg.msg) { // button
																	// press
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): button event");
				pressedButton(bytes[3]);
			}

			else if (bytes[2] == eMessageType.GetDeviceTypeResponse.msg) { // device
																			// type
				if (bytes[4] == 1 || bytes[4] == 4) {
					watchType = WatchType.ANALOG;
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): device type response; analog watch");

					Idle.toIdle(this);

				} else {
					watchType = WatchType.DIGITAL;
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): device type response; digital watch");

					Protocol.configureMode();
					Idle.toIdle(this);
					Idle.updateLcdIdle(this);

					Protocol.queryNvalTime();

				}
			}

			else if (bytes[2] == eMessageType.GeneralPurposePhoneMsg.msg) {
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): general purpose message");
				// Music Message
				if (bytes[3] == 0x42) {
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): music message");

					switch (bytes[4]) {
					case MediaControl.NEXT:
						MediaControl.next(context);
						break;
					case MediaControl.PREVIOUS:
						MediaControl.previous(context);
						break;
					case MediaControl.TOGGLE:
						MediaControl.togglePause(context);
						break;
					case MediaControl.VOLUME_UP:
						MediaControl.volumeUp(audioManager);
						break;
					case MediaControl.VOLUME_DOWN:
						MediaControl.volumeDown(audioManager);
						break;
					}
				}
			} else if (bytes[2] == eMessageType.ReadBatteryVoltageResponse.msg) {
				boolean powerGood = bytes[4] > 0;
				boolean batteryCharging = bytes[5] > 0;
				float batterySense = (((int) bytes[7] << 8) + (int) bytes[6]) / 1000.0f;
				float batteryAverage = (((int) bytes[9] << 8) + (int) bytes[8]) / 1000.0f;
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): received battery voltage response."
								+ " power_good=" + powerGood
								+ " battery_charging=" + batteryCharging
								+ " battery_sense=" + batterySense
								+ " battery_average=" + batteryAverage);
				String voltageFrequencyString = PreferenceManager.getDefaultSharedPreferences(this).getString(
						"collectWatchVoltage", "0");
				final int voltageFrequency = Integer.parseInt(voltageFrequencyString);
				if (voltageFrequency > 0) {
					File sdcard = Environment.getExternalStorageDirectory();
					File csv = new File(sdcard,"metawatch_voltage.csv");
					boolean fileExists = csv.exists();
					FileWriter fw = new FileWriter(csv, true);
					if (fileExists == false) {
						fw.write("Date,Sense,Average\n");
					}
					Date date = new Date();
					fw.write("\"" + date.toString()+ "\"," + batterySense + "," + batteryAverage + "\n");
					fw.flush();
					fw.close();
				}		
			} else if (bytes[2] == eMessageType.ReadLightSensorResponse.msg) {
				float lightSense = (((int) bytes[1] << 8) + (int) bytes[0]) / 1000.0f;
				float lightAverage = (((int) bytes[3] << 8) + (int) bytes[2]) / 1000.0f;
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): received light sensore response."
								+ " light_sense=" + lightSense
								+ " light_average=" + lightAverage);
			
			} else {
				if (Preferences.logging) Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): Unknown message : 0x"+Integer.toString((bytes[2] & 0xff) + 0x100, 16).substring(1) + ", ");
			}

		} catch (IOException e) {
			wakeLock.acquire(5000);
			if (connectionState != ConnectionState.DISCONNECTING) {
				connectionState = ConnectionState.CONNECTING;
				broadcastConnection(false);
			}
		}

	}

	void broadcastConnection(boolean connected) {
		if (connected != lastConnectionState) {
			lastConnectionState = connected;
			Intent intent = new Intent(
					"org.metawatch.manager.CONNECTION_CHANGE");
			intent.putExtra("state", connected);
			sendBroadcast(intent);
			notifyClients();
			if (Preferences.logging) Log.d(MetaWatch.TAG,
					"MetaWatchService.broadcastConnection(): Broadcast connection change: state='"
							+ connected + "'");
			Protocol.resetLCDDiffBuffer();
		}
	}

	void pressedButton(byte button) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "button code: " + Byte.toString(button));
		
		if(button>0 && Preferences.hapticFeedback)
			Protocol.vibrate(5, 5, 2);

		if (Preferences.logging) Log.d(MetaWatch.TAG, "MetaWatchService.pressedButton(): watchState="
				+ watchState);
		switch (watchState) {
		case WatchStates.IDLE: {
			switch (button) {
			case MediaControl.VOLUME_UP:
				MediaControl.volumeUp(audioManager);
				break;
			case MediaControl.VOLUME_DOWN:
				MediaControl.volumeDown(audioManager);
				break;
			case MediaControl.NEXT:
				MediaControl.next(this);
				break;
			case MediaControl.PREVIOUS:
				MediaControl.previous(this);
				break;
			case MediaControl.TOGGLE:
				MediaControl.togglePause(this);
				break;
			case Protocol.REPLAY:
				Notification.replay(this);
				break;
				
			case Idle.IDLE_NEXT_PAGE:
				Idle.nextPage();
				Idle.updateLcdIdle(this);
				break;
				
			case Call.CALL_SPEAKER:
				MediaControl.ToggleSpeakerphone(audioManager);
				break;
				
			case Call.CALL_ANSWER:
				MediaControl.AnswerCall(context);
				break;
				
			case Call.CALL_DISMISS:
				MediaControl.DismissCall(context);
				break;
		
			}
		}
			break;
		case WatchStates.APPLICATION:
			broadcastButton(button, watchState);
			break;
		case WatchStates.NOTIFICATION:
			switch (button) {
			case Notification.NOTIFICATION_UP:
				Notification.buttonPressed(button);
				break;
				
			case Notification.NOTIFICATION_DOWN:
				Notification.buttonPressed(button);
				break;
				
			case Notification.NOTIFICATION_DISMISS:
				Notification.buttonPressed(button);
				break;
			}
			break;
		}

	}

	void broadcastButton(byte button, int state) {
		Intent intent = new Intent("org.metawatch.manager.BUTTON_PRESS");
		intent.putExtra("button", button);
		switch (state) {
		case WatchStates.IDLE:
			intent.putExtra("mode", "idle");
			break;
		case WatchStates.APPLICATION:
			intent.putExtra("mode", "application");
			break;
		}
		sendBroadcast(intent);
	}
	
	@Override
	public void onLowMemory() {		
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"MetaWatchService.onLowMemory(): " + availableMegs + "Mb free");	
		
		super.onLowMemory();
	}
	

}
