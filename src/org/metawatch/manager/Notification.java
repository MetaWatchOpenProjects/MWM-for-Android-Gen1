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
 * Notification.java                                                         *
 * Notification                                                              *
 * Notification watch mode                                                   *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.metawatch.manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WatchType;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

public class Notification {

	private static NotificationType lastNotification = null;

	final static byte NOTIFICATION_TIMEOUT = 0;
	final static byte NOTIFICATION_UP = 30;
	final static byte NOTIFICATION_DOWN = 31;
	final static byte NOTIFICATION_DISMISS = 32;
	
	private static byte notifyButtonPress = 0;
	
	private static BlockingQueue<NotificationType> notificationQueue = new LinkedBlockingQueue<NotificationType>();
	private static volatile boolean notificationSenderRunning = false;

	private static void addToNotificationQueue(NotificationType notification) {
		if (MetaWatchService.connectionState == MetaWatchService.ConnectionState.CONNECTED) {
			notificationQueue.add(notification);
			MetaWatchService.notifyClients();
		}
	}

	private static class NotificationSender implements Runnable {
		private Context context;

		public NotificationSender(Context context) {
			this.context = context;
		}

		public void run() {
			int currentNotificationPage = 0;
			while (notificationSenderRunning) {
				try {
					NotificationType notification = notificationQueue.take();
					MetaWatchService.watchState = MetaWatchService.WatchStates.NOTIFICATION;
					MetaWatchService.WatchModes.NOTIFICATION = true;

					if (MetaWatchService.watchType == WatchType.DIGITAL) {

						if (notification.bitmaps != null && notification.bitmaps.length>0) {
							Protocol.sendLcdBitmap(notification.bitmaps[0],
									MetaWatchService.WatchBuffers.NOTIFICATION);
							currentNotificationPage = 0;
							
							Log.d(MetaWatch.TAG,
									"Notification contains " + notification.bitmaps.length + " bitmaps.");
														
						}
						else if (notification.array != null)
							Protocol.sendLcdArray(notification.array,
									MetaWatchService.WatchBuffers.NOTIFICATION);
						else if (notification.buffer != null)
							Protocol.sendLcdBuffer(notification.buffer,
									MetaWatchService.WatchBuffers.NOTIFICATION);
						else {
							continue;
						}

						Protocol.updateDisplay(2);

						if (notification.vibratePattern.vibrate)
							Protocol.vibrate(notification.vibratePattern.on,
									notification.vibratePattern.off,
									notification.vibratePattern.cycles);
						
						if (Preferences.notifyLight)
							Protocol.ledChange(true);

						Log.d(MetaWatch.TAG, "notif bitmap sent from thread");

					} else {

						Protocol.sendOledDisplay(notification.oledTop, true,
								false);
						Protocol.sendOledDisplay(notification.oledBottom,
								false, false);

						if (notification.vibratePattern.vibrate)
							Protocol.vibrate(notification.vibratePattern.on,
									notification.vibratePattern.off,
									notification.vibratePattern.cycles);

						if (notification.oledScroll != null) {

							Log.d(MetaWatch.TAG, "notification.scrollLength = "
									+ notification.scrollLength);

							/*
							 * If requested, let the notification stay on the
							 * screen for a few seconds before starting to
							 * scroll.
							 */
							SharedPreferences sharedPreferences = PreferenceManager
									.getDefaultSharedPreferences(context);							
							if (sharedPreferences.getBoolean("pauseBeforeScrolling", false)) {
								Log.d(MetaWatch.TAG,
										"Pausing before scrolling.");
								Thread.sleep(3000);
							}

							if (notification.scrollLength >= 240) {

								Protocol.sendOledBufferPart(
										notification.oledScroll, 0, 240, true,
										false);
								// wait continue with scroll

								for (int i = 240; i < notification.scrollLength; i += 80) {
									try {
										synchronized (Notification.scrollRequest) {
											Notification.scrollRequest
													.wait(60000);
										}
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									if (i + 80 >= notification.scrollLength)
										Protocol.sendOledBufferPart(
												notification.oledScroll, i, 80,
												false, true);
									else
										Protocol.sendOledBufferPart(
												notification.oledScroll, i, 80,
												false, false);
								}

							} else if (notification.scrollLength > 0) {

								int len = notification.scrollLength / 20 + 1;
								Protocol.sendOledBufferPart(
										notification.oledScroll, 0, len * 20,
										true, true);

							}
						}

					}

					lastNotification = notification;

					/* Give some space between notifications. */
					
					if (notification.timeout < 0) {
						notifyButtonPress = NOTIFICATION_TIMEOUT;
						if (notification.bitmaps!=null & notification.bitmaps.length>1) {
							Protocol.enableButton(0, 0, NOTIFICATION_UP, 2); // Right top immediate
							Protocol.enableButton(1, 0, NOTIFICATION_DOWN, 2); // Right middle immediate
						}
						Protocol.enableButton(2, 0, NOTIFICATION_DISMISS, 2); // Right bottom immediate

						Log.d(MetaWatch.TAG,
								"NotificationSender.run(): Notification sent, waiting for dismiss " );
						
						int timeout = getStickyNotificationTimeout(context);
						
						do {
							try {
								synchronized (Notification.buttonPressed) {
									buttonPressed.wait(timeout);	
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							if (notifyButtonPress==NOTIFICATION_UP && currentNotificationPage>0) {
								currentNotificationPage--;
								Protocol.sendLcdBitmap(notification.bitmaps[currentNotificationPage],
										MetaWatchService.WatchBuffers.NOTIFICATION);
							}
							else if (notifyButtonPress==NOTIFICATION_DOWN && currentNotificationPage<notification.bitmaps.length-1) {
								currentNotificationPage++;
								Protocol.sendLcdBitmap(notification.bitmaps[currentNotificationPage],
										MetaWatchService.WatchBuffers.NOTIFICATION);
							}
							else if (notifyButtonPress==NOTIFICATION_TIMEOUT) {
								notifyButtonPress = NOTIFICATION_DISMISS;
							}
							
							Log.d(MetaWatch.TAG,
									"Displaying page " + currentNotificationPage +" / "+ notification.bitmaps.length );
							
							Protocol.updateDisplay(2);
						} while (notifyButtonPress != NOTIFICATION_DISMISS);
						
						
						Protocol.disableButton(0, 0, 2); // Right top
						Protocol.disableButton(1, 0, 2); // Right middle
						Protocol.disableButton(2, 0, 2); // Right bottom
					
						Log.d(MetaWatch.TAG,
								"NotificationSender.run(): Done sleeping.");
						
					}
					else {
						Log.d(MetaWatch.TAG,
								"NotificationSender.run(): Notification sent, sleeping for "
										+ notification.timeout + "ms");
						Thread.sleep(notification.timeout);
						Log.d(MetaWatch.TAG,
								"NotificationSender.run(): Done sleeping.");
					}
					
					if (MetaWatchService.WatchModes.CALL == false) {
						exitNotification(context);
					}
					Thread.sleep(2000);

				} catch (InterruptedException ie) {
					/* If we've been interrupted, exit gracefully. */
					Log.d(MetaWatch.TAG,
							"NotificationSender was interrupted waiting for next notification, exiting.");
					break;
				}
				catch (Exception e)
				{
					Log.e(MetaWatch.TAG, "Exception in NotificationSender: "+e.toString());
				}
			}
		}
	};

	private static Thread notificationSenderThread = null;

	public static synchronized void startNotificationSender(Context context) {
		if (notificationSenderRunning == false) {
			notificationSenderRunning = true;
			NotificationSender notificationSender = new NotificationSender(
					context);
			notificationSenderThread = new Thread(notificationSender,
					"NotificationSender");
			notificationSenderThread.setDaemon(true);
			notificationSenderThread.start();
		}
	}

	public static synchronized void stopNotificationSender() {
		if (notificationSenderRunning == true) {
			/* Stops thread gracefully */
			notificationSenderRunning = false;
			/* Wakes up thread if it's sleeping on the queue */
			notificationSenderThread.interrupt();
			/* Thread is dead, we can mark it for garbage collection. */
			notificationSenderThread = null;
		}
	}

	private static final String NOTIFICATION_TIMEOUT_SETTING = "notificationTimeout";
	private static final String DEFAULT_NOTIFICATION_TIMEOUT_STRING = "5";
	private static final int DEFAULT_NOTIFICATION_TIMEOUT = 5;
	private static final String STICKY_NOTIFICATION_TIMEOUT_SETTING = "stickyNotificationTimeout";
	private static final String STICKY_NOTIFICATION_TIMEOUT_STRING = "120";
	private static final int DEFAULT_STICKY_NOTIFICATION_TIMEOUT = 120;
	private static final int NUM_MS_IN_SECOND = 1000;

	public static int getDefaultNotificationTimeout(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String timeoutString = sharedPreferences.getString(
				NOTIFICATION_TIMEOUT_SETTING,
				DEFAULT_NOTIFICATION_TIMEOUT_STRING);
		try {
			int timeout = Integer.parseInt(timeoutString) * NUM_MS_IN_SECOND;
			return timeout;
		} catch (NumberFormatException nfe) {
			return DEFAULT_NOTIFICATION_TIMEOUT;
		}
	}
	
	public static int getStickyNotificationTimeout(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String timeoutString = sharedPreferences.getString(
				STICKY_NOTIFICATION_TIMEOUT_SETTING,
				STICKY_NOTIFICATION_TIMEOUT_STRING);
		try {
			int timeout = Integer.parseInt(timeoutString) * NUM_MS_IN_SECOND;
			return timeout;
		} catch (NumberFormatException nfe) {
			return DEFAULT_STICKY_NOTIFICATION_TIMEOUT;
		}
	}

	public static Object scrollRequest = new Object();
	public static Object buttonPressed = new Object();

	public static class NotificationType {
		Bitmap[] bitmaps;
		int[] array;
		byte[] buffer;

		byte[] oledTop;
		byte[] oledBottom;
		byte[] oledScroll;

		int scrollLength;
		int timeout;

		VibratePattern vibratePattern;
	}

	public static class VibratePattern {
		
		public static final VibratePattern NO_VIBRATE = new VibratePattern(false,0,0,0);
		
		boolean vibrate = false;
		int on;
		int off;
		int cycles;

		public VibratePattern(boolean vibrate, int on, int off, int cycles) {
			this.vibrate = vibrate;
			this.on = on;
			this.off = off;
			this.cycles = cycles;
		}
	}

	public static void addTextNotification(Context context, String text,
			VibratePattern vibratePattern, int timeout) {
		NotificationType notification = new NotificationType();
		notification.bitmaps = new Bitmap[]{ Protocol.createTextBitmap(context, text) };
		notification.timeout = timeout;
		if (vibratePattern == null)
			notification.vibratePattern = VibratePattern.NO_VIBRATE;
		else
			notification.vibratePattern = vibratePattern;
		addToNotificationQueue(notification);
	}
	
	public static void addBitmapNotification(Context context, Bitmap bitmap,
			VibratePattern vibratePattern, int timeout) {
		addBitmapNotification(context, new Bitmap[] {bitmap}, vibratePattern, timeout);
	}

	public static void addBitmapNotification(Context context, Bitmap[] bitmaps,
			VibratePattern vibratePattern, int timeout) {
		
		if (bitmaps!=null) {
			Log.d(MetaWatch.TAG, "Notification comprised of "+bitmaps.length+" bitmaps");
		}
		
		NotificationType notification = new NotificationType();
		notification.bitmaps = bitmaps;
		notification.timeout = timeout;
		if (vibratePattern == null)
			notification.vibratePattern = VibratePattern.NO_VIBRATE;
		else
			notification.vibratePattern = vibratePattern;
		addToNotificationQueue(notification);
	}

	public static void addArrayNotification(Context context, int[] array,
			VibratePattern vibratePattern) {
		NotificationType notification = new NotificationType();
		notification.array = array;

		int notificationTimeout = getDefaultNotificationTimeout(context);
		notification.timeout = notificationTimeout;
		if (vibratePattern == null)
			notification.vibratePattern = VibratePattern.NO_VIBRATE;
		else
			notification.vibratePattern = vibratePattern;
		addToNotificationQueue(notification);

	}

	public static void addBufferNotification(Context context, byte[] buffer,
			VibratePattern vibratePattern) {
		NotificationType notification = new NotificationType();
		notification.buffer = buffer;
		int notificationTimeout = getDefaultNotificationTimeout(context);
		notification.timeout = notificationTimeout;
		if (vibratePattern == null)
			notification.vibratePattern = VibratePattern.NO_VIBRATE;
		else
			notification.vibratePattern = vibratePattern;
		addToNotificationQueue(notification);

	}

	public static void addOledNotification(Context context, byte[] top,
			byte[] bottom, byte[] scroll, int scrollLength,
			VibratePattern vibratePattern) {
		NotificationType notification = new NotificationType();
		notification.oledTop = top;
		notification.oledBottom = bottom;
		notification.oledScroll = scroll;
		notification.scrollLength = scrollLength;
		int notificationTimeout = getDefaultNotificationTimeout(context);
		notification.timeout = notificationTimeout;
		if (vibratePattern == null)
			notification.vibratePattern = VibratePattern.NO_VIBRATE;
		else
			notification.vibratePattern = vibratePattern;
		addToNotificationQueue(notification);

	}

	private static void exitNotification(Context context) {
		// disable notification mode
		MetaWatchService.WatchModes.NOTIFICATION = false;

		if (MetaWatchService.WatchModes.CALL == true)
			return;
		else if (MetaWatchService.WatchModes.APPLICATION == true)
			Application.toApp();
		else if (MetaWatchService.WatchModes.IDLE == true)
			Idle.toIdle(context);
	}

	public static void replay(Context context) {
		Log.d(MetaWatch.TAG, "Notification.replay()");
		//if (lastNotification != null) {
		//	lastNotification.vibratePattern.vibrate = false;
			
			NotificationBuilder.createCalendar(context,Utils.Meeting_Title);
			
			
			//addToNotificationQueue(lastNotification);

	//	}
	}
	
	public static void buttonPressed(byte button) {
		Log.d(MetaWatch.TAG,
				"Notification:Button pressed "+button );

		notifyButtonPress = button;
		synchronized (Notification.buttonPressed) {
			Notification.buttonPressed.notify();	
		}		
	}
	
	public static int getQueueLength() {
		return notificationQueue.size();
	}

}
