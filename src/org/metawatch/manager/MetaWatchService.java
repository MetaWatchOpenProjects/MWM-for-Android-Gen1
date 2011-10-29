
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
import java.util.Date;
import java.util.UUID;

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
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MetaWatchService extends Service {

	Context context;
	public static BluetoothAdapter bluetoothAdapter;
	// BluetoothServerSocket bluetoothServerSocket;
	BluetoothSocket bluetoothSocket;
	static InputStream inputStream;
	static OutputStream outputStream;

	TelephonyManager telephonyManager;
	AudioManager audioManager;
	NotificationManager notificationManager;
	RemoteViews remoteViews;
	android.app.Notification notification;

	public static PowerManager powerManger;
	public static PowerManager.WakeLock wakeLock;

	static int connectionState;
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

	static class WatchModes {
		public static volatile boolean IDLE = false;
		public static volatile boolean APPLICATION = false;
		public static volatile boolean NOTIFICATION = false;
		public static volatile boolean CALL = false;
	}

	static class Preferences {
		public static boolean notifyCall = true;
		public static boolean notifySMS = true;
		public static boolean notifyGmail = true;
		public static boolean notifyK9 = true;
		public static boolean notifyAlarm = true;
		public static boolean notifyBatterylow = true;
		public static boolean notifyTimezonechange = true;
		public static boolean notifyMusic = true;
		public static boolean notifyCalendar = true;
		public static String watchMacAddress = "";
		public static int packetWait = 10;
		public static boolean skipSDP = false;
		public static boolean invertLCD = false;
		public static String weatherCity = "Dallas,US";
		public static boolean weatherCelsius = false;
		public static int fontSize = 2;
		public static int smsLoopInterval = 15;
		public static boolean idleMusicControls = false;
		public static boolean idleReplay = false;
		public static boolean disableWeather = false;
	}

	final class WatchType {
		static final int ANALOG = 1;
		static final int DIGITAL = 2;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static void loadPreferences(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

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
		Preferences.weatherCity = sharedPreferences.getString("WeatherCity",
				Preferences.weatherCity);
		Preferences.weatherCelsius = sharedPreferences.getBoolean(
				"WeatherCelsius", Preferences.weatherCelsius);
		Preferences.idleMusicControls = sharedPreferences.getBoolean(
				"IdleMusicControls", Preferences.idleMusicControls);
		Preferences.idleReplay = sharedPreferences.getBoolean("IdleReplay",
				Preferences.idleReplay);
		Preferences.disableWeather = sharedPreferences.getBoolean(
				"DisableWeather", Preferences.disableWeather);

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

	public void createNotification() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		boolean hideNotificationIcon = sharedPreferences.getBoolean(
				"HideNotificationIcon", false);
		Log.d(MetaWatch.TAG,
				"MetaWatchService.createNotification(): hideNotificationIcon="
						+ hideNotificationIcon);
		int notificationIcon = (hideNotificationIcon ? R.drawable.transparent_square
				: R.drawable.disconnected);
		notification = new android.app.Notification(notificationIcon, null,
				System.currentTimeMillis());
		notification.flags |= android.app.Notification.FLAG_ONGOING_EVENT;

		remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
		remoteViews.setTextViewText(R.id.notification_title,
				getString(R.string.app_name));
		remoteViews.setImageViewResource(R.id.notification_button,
				R.drawable.connected_large);

		notification.contentView = remoteViews;
		notification.contentIntent = createNotificationPendingIntent();

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
		Log.d(MetaWatch.TAG,
				"MetaWatchService.updateNotification(): hideNotificationIcon="
						+ hideNotificationIcon);
		switch (connectionState) {
		case ConnectionState.CONNECTING:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.disconnected);
			remoteViews.setTextViewText(R.id.notification_subtitle,
					"Connecting");
			broadcastConnection(false);
			break;
		case ConnectionState.CONNECTED:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.connected);
			remoteViews
					.setTextViewText(R.id.notification_subtitle, "Connected");
			broadcastConnection(true);
			break;
		default:
			notification.icon = (hideNotificationIcon ? R.drawable.transparent_square
					: R.drawable.disconnected);
			remoteViews.setTextViewText(R.id.notification_subtitle,
					"Disconnected");
			broadcastConnection(false);
			break;
		}
		startForeground(1, notification);
	}

	public void removeNotification() {
		stopForeground(true);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		context = this;
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
		Monitors.start(this, telephonyManager);

		start();

	}

	@Override
	public void onDestroy() {
		disconnectExit();
		super.onDestroy();

		Monitors.stop();
		removeNotification();
	}

	void connect(Context context) {

		try {

			Log.d(MetaWatch.TAG, "Remote device address: "
					+ Preferences.watchMacAddress);
			if (Preferences.watchMacAddress.equals(""))
				loadPreferences(context);
			BluetoothDevice bluetoothDevice = bluetoothAdapter
					.getRemoteDevice(Preferences.watchMacAddress);

			if (!bluetoothAdapter.isEnabled()) {
				return;
			}

			/*
			 * Log.d(MetaWatch.TAG, "remote device name: " +
			 * bluetoothDevice.getName()); int bondState =
			 * bluetoothDevice.getBondState(); String bond = ""; switch
			 * (bondState) { case BluetoothDevice.BOND_BONDED: bond = "bonded";
			 * break; case BluetoothDevice.BOND_BONDING: bond = "bonding";
			 * break; case BluetoothDevice.BOND_NONE: bond = "none"; break; }
			 * Log.d(MetaWatch.TAG, "bond state: " + bond);
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

			// Log.d(MetaWatch.TAG, "got Bluetooth socket");
			// if (bluetoothSocket == null)
			// Log.d(MetaWatch.TAG, "Bluetooth socket is null");

			bluetoothSocket.connect();
			inputStream = bluetoothSocket.getInputStream();
			outputStream = bluetoothSocket.getOutputStream();

			connectionState = ConnectionState.CONNECTED;
			updateNotification();

			Protocol.startProtocolSender();
			Protocol.sendRtcNow(context);
			Protocol.getDeviceType();

			Notification.startNotificationSender(this);

			if (Preferences.idleReplay)
				Protocol.enableReplayButton();
			else
				Protocol.disableReplayButton();

		} catch (IOException ioexception) {
			Log.d(MetaWatch.TAG, ioexception.toString());
			// sendToast(ioexception.toString());
		} catch (SecurityException e) {
			Log.d(MetaWatch.TAG, e.toString());
		} catch (NoSuchMethodException e) {
			Log.d(MetaWatch.TAG, e.toString());
		} catch (IllegalArgumentException e) {
			Log.d(MetaWatch.TAG, e.toString());
		} catch (IllegalAccessException e) {
			Log.d(MetaWatch.TAG, e.toString());
		} catch (InvocationTargetException e) {
			Log.d(MetaWatch.TAG, e.toString());
		}

	}

	public void sendToast(String text) {
		Message m = new Message();
		m.what = 1;
		m.obj = text;
		messageHandler.sendMessage(m);
	}

	private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(context, (CharSequence) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

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
						Log.d(MetaWatch.TAG, "state: disconnected");
						break;
					case ConnectionState.CONNECTING:
						Log.d(MetaWatch.TAG, "state: connecting");
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
						Log.d(MetaWatch.TAG, "state: connected");
						// read from input stream
						readFromDevice();
						break;
					case ConnectionState.DISCONNECTING:
						Log.d(MetaWatch.TAG, "state: disconnecting");
						// exit
						run = false;
						break;
					}
				}
			}
		};
		thread.start();

		/* DEBUG */
		String voltageFrequencyString = PreferenceManager.getDefaultSharedPreferences(this).getString(
				"collectWatchVoltage", "0");
		final int voltageFrequency = Integer.parseInt(voltageFrequencyString);
		if (voltageFrequency > 0) {
			Thread voltageThread = new Thread("Voltage monitoring thread") {
				@Override
				public void run() {
					boolean run = true;
					Log.d(MetaWatch.TAG,
							"MetaWatchService.start(): Starting voltage monitoring thread.");
					while (run) {
						try {
							/* Sleep for a while */
							long sleep = voltageFrequency * 60 * 1000;
							Log.d(MetaWatch.TAG,
									"MetaWatchService.start(): Sleeping for "+sleep+" ms.");
							Thread.sleep(sleep);
							if (connectionState == ConnectionState.CONNECTED) {
								Protocol.readBatteryVoltage();
							}
						} catch (InterruptedException ie) {
							/* If we've been interrupted, exit gracefully. */
							run = false;
							Log.d(MetaWatch.TAG,
									"MetaWatchService.start(): voltage monitoring thread interrupted.");
						}
					}
					Log.d(MetaWatch.TAG,
							"MetaWatchService.start(): voltage monitoring thread ended.");
				}
			};
			voltageThread.setDaemon(true);
			voltageThread.start();
		}

	}

	void readFromDevice() {

		try {
			byte[] bytes = new byte[256];
			Log.d(MetaWatch.TAG, "before blocking read");
			inputStream.read(bytes);
			wakeLock.acquire(5000);

			// print received
			String str = "received: ";
			int len = (bytes[1] & 0xFF);
			Log.d(MetaWatch.TAG, "packet length: " + len);

			for (int i = 0; i < len; i++) {
				// str+= Byte.toString(bytes[i]) + ", ";
				str += "0x"
						+ Integer.toString((bytes[i] & 0xff) + 0x100, 16)
								.substring(1) + ", ";
			}
			Log.d(MetaWatch.TAG, str);
			/*
			 * switch (bytes[2]) { case eMessageType.GetDeviceTypeResponse.msg:
			 * Log.d(MetaWatch.TAG, "received: device type response"); break;
			 * case eMessageType.NvalOperationResponseMsg.msg:
			 * Log.d(MetaWatch.TAG, "received: nval response"); break; case
			 * eMessageType.StatusChangeEvent.msg: Log.d(MetaWatch.TAG,
			 * "received: status change event"); break; }
			 */
			/*
			 * if (bytes[2] == 0x31) { // nval response if (bytes[3] == 0x00) //
			 * success if (bytes[4] == 0x00) // set to 12 hour format
			 * Protocol.setNvalTime(true); }
			 */

			if (bytes[2] == eMessageType.StatusChangeEvent.msg) { // status
																	// change
																	// event
				Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): status change");
				if (bytes[4] == 0x11) {
					Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): scroll request notification");

					synchronized (Notification.scrollRequest) {
						Notification.scrollRequest.notify();
					}
				} else if (bytes[4] == 0x10) {
					Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): scroll complete.");
				}
			}

			else if (bytes[2] == eMessageType.ButtonEventMsg.msg) { // button
																	// press
				Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): button event");
				pressedButton(bytes[3]);
			}

			else if (bytes[2] == eMessageType.GetDeviceTypeResponse.msg) { // device
																			// type
				if (bytes[4] == 1 || bytes[4] == 4) {
					watchType = WatchType.ANALOG;
					Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): device type response; analog watch");

					Idle.toIdle(this);

				} else {
					watchType = WatchType.DIGITAL;
					Log.d(MetaWatch.TAG,
							"MetaWatchService.readFromDevice(): device type response; digital watch");

					if (Preferences.idleMusicControls)
						Protocol.enableMediaButtons();
					// else
					// Protocol.disableMediaButtons();

					Protocol.configureMode();
					Idle.toIdle(this);
					Idle.updateLcdIdle(this);

					Protocol.queryNvalTime();

				}
			}

			else if (bytes[2] == eMessageType.GeneralPurposePhoneMsg.msg) {
				Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): general purpose message");
				// Music Message
				if (bytes[3] == 0x42) {
					Log.d(MetaWatch.TAG,
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
				Log.d(MetaWatch.TAG,
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
					FileWriter fw = new FileWriter(csv, true);
					Date date = new Date();
					fw.write("\"" + date.toString()+ "\"," + batteryAverage + "\n");
					fw.flush();
					fw.close();
				}				
			} else {
				Log.d(MetaWatch.TAG,
						"MetaWatchService.readFromDevice(): Unknown message?");
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
			Log.d(MetaWatch.TAG,
					"MetaWatchService.broadcastConnection(): Broadcast connection change: state='"
							+ connected + "'");
		}
	}

	void pressedButton(byte button) {
		Log.d(MetaWatch.TAG, "button code: " + Byte.toString(button));

		Log.d(MetaWatch.TAG, "MetaWatchService.pressedButton(): watchState="
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
			}
			/*
			 * if (Idle.isIdleButtonOverriden(button)) { Log.d(MetaWatch.TAG,
			 * "this button is overriden"); broadcastButton(button, watchState);
			 * }
			 */
		}
			break;
		case WatchStates.APPLICATION:
			broadcastButton(button, watchState);
			break;
		case WatchStates.NOTIFICATION:
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

}
