
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
 * Protocol.java                                                             *
 * Protocol                                                                  *
 * Basic low level protocol commands                                         *
 *                                                                           *
 *                                                                           *
 *****************************************************************************/

package org.metawatch.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.metawatch.manager.MetaWatchService.ConnectionState;
import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.preference.PreferenceManager;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;

public class Protocol {

	public static final byte REPLAY = 30;
	
	private static volatile BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<byte[]>();

	private static boolean idleShowClock = true;
	
	private static byte[][][] LCDDiffBuffer = new byte[3][48][30];
  
	public static void resetLCDDiffBuffer() {
		LCDDiffBuffer = new byte[3][48][30];
	}

	private static volatile boolean protocolSenderRunning = false;
	private static Runnable protocolSender = new Runnable() {
		public void run() {
			while (protocolSenderRunning) {
				try {
					byte[] message = sendQueue.take();
					send(message);
					Thread.sleep(Preferences.packetWait);

				} catch (InterruptedException ie) {
					/* If we've been interrupted, exit gracefully. */
					if (Preferences.logging) Log.d(MetaWatch.TAG,
							"ProtocolSender was interrupted waiting for next message, exiting.");
					break;
				} catch (IOException e) {
					if (Preferences.logging) Log.e(MetaWatch.TAG,
							"ProtocolSender encountered an I/O error sending message!");
					sendQueue.clear();
					break;
				}
			}
		}
	};
	private static Thread protocolSenderThread = null;

	private static Runnable protocolWatchdog = new Runnable() {
		public void run() {

			while (protocolSenderRunning) {
				try {

					/* Remember the current queue length */
					int prevSentPackets=sentPackets;

					/* Wait some time */
					Thread.sleep(5*1000);

					/* If the queue did not decrease, restart protocol */
					if ((sendQueue.size()!=0)&&(prevSentPackets==sentPackets)) {
						if (MetaWatchService.connectionState==ConnectionState.CONNECTED) {
							if (Preferences.autoRestart) {
								Protocol.stopProtocolSender();
								Protocol.startProtocolSender();
								Log.d(MetaWatch.TAG, "Protocol restarted due to stalled queue");  
								return;
							}
						}
					}

				}
				catch (InterruptedException ioe) {
					//if (Preferences.logging) Log.d(MetaWatch.TAG, "Timeout aborted");        
				}

			}      
		}
	};
	private static int sentPackets = 0;  
	private static Thread protocolWatchdogThread = null;

	public static synchronized void startProtocolSender() {
		if (protocolSenderRunning == false) {
			protocolSenderRunning = true;
			protocolSenderThread = new Thread(protocolSender, "ProtocolSender");
			protocolSenderThread.setDaemon(true);
			protocolSenderThread.start();
			protocolWatchdogThread = new Thread(protocolWatchdog, "ProtocolWatchdog");
			protocolWatchdogThread.start();
		}
	}

	public static synchronized void stopProtocolSender() {
		if (protocolSenderRunning == true) {
			/* Stops thread gracefully */
			protocolSenderRunning = false;
			/* Wakes up thread if it's sleeping on the queue */
			protocolSenderThread.interrupt();
			protocolWatchdogThread.interrupt();
			/* Thread is dead, we can mark it for garbage collection. */
			protocolSenderThread = null;
			protocolWatchdogThread = null;
		}
	}

	public static boolean sendLcdBitmap(Bitmap bitmap, int bufferType) {
		if (bitmap==null) 
			return false;
		
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendLcdBitmap()");
		int pixelArray[] = new int[96 * 96];
		bitmap.getPixels(pixelArray, 0, 96, 0, 0, 96, 96);

		return sendLcdArray(pixelArray, bufferType);
	}

	static boolean sendLcdArray(int[] pixelArray, int bufferType) {
		byte send[] = new byte[1152];

		for (int i = 0; i < 1152; i++) {
			int p[] = new int[8];

			for (int j = 0; j < 8; j++) {
				if (pixelArray[i * 8 + j] == Color.WHITE)
					/*
					 * if (Preferences.invertLCD) p[j] = 1; else
					 */
					p[j] = 0;
				else
					/*
					 * if (Preferences.invertLCD) p[j] = 0; else
					 */
					p[j] = 1;
			}
			send[i] = (byte) (p[7] * 128 + p[6] * 64 + p[5] * 32 + p[4] * 16
					+ p[3] * 8 + p[2] * 4 + p[1] * 2 + p[0] * 1);
		}
		return sendLcdBuffer(send, bufferType);
	}

	static boolean sendLcdBuffer(byte[] buffer, int bufferType) {
		if (MetaWatchService.connectionState != MetaWatchService.ConnectionState.CONNECTED)
			return false;

		int i = 0;
		//if (bufferType == MetaWatchService.WatchBuffers.IDLE && idleShowClock)
		//	i = 30;

		int sentLines = 0;
		for (; i < 96; i += 2) {
			byte[] bytes = new byte[30];

			bytes[0] = 0x01;
			bytes[1] = (byte) (bytes.length+2); // packet length
			bytes[2] = eMessageType.WriteBuffer.msg; 
			bytes[3] = (byte) bufferType; 

			bytes[4] = (byte) i; // row A
			for (int j = 0; j < 12; j++)
				bytes[j + 5] = buffer[i * 12 + j];

			bytes[4 + 13] = (byte) (i + 1); // row B
			for (int j = 0; j < 12; j++)
				bytes[j + 5 + 13] = buffer[i * 12 + j + 12];

			// Only send the row packet if the data's changed since the
			// last time we sent it
			if( !Arrays.equals(LCDDiffBuffer[bufferType][i/2], bytes) ) {
				enqueue(bytes);
				LCDDiffBuffer[bufferType][i/2] = bytes;
				sentLines += 2;
			}
		}
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Sent "+sentLines+ "/96");

		return (sentLines>0);
	}
	
	public static void idleShowClock(boolean show) {
		idleShowClock = show;
		
	}

	public static void enqueue(byte[] bytes) {

		sendQueue.add(bytes);

		if (sendQueue.size() % 10 == 0)
			MetaWatchService.notifyClients();
	}

	public static void send(byte[] bytes) throws IOException {
		if (bytes == null)
			return;

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(bytes);
		byteArrayOutputStream.write(crc(bytes));

		SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(MetaWatchService.context);
		if (sharedPreferences.getBoolean("logPacketDetails", false)) {
			String str = "sending: ";
			byte[] b = byteArrayOutputStream.toByteArray();
			for (int i = 0; i < b.length; i++) {
				str += "0x"
					+ Integer.toString((b[i] & 0xff) + 0x100, 16)
					.substring(1) + ", ";
			}
			if (Preferences.logging) Log.d(MetaWatch.TAG, str);
		}

		if (MetaWatchService.outputStream == null)
			throw new IOException("OutputStream is null");

		MetaWatchService.outputStream
			.write(byteArrayOutputStream.toByteArray());
		MetaWatchService.outputStream.flush();

		sentPackets++;
		if (sentPackets<0)
		  sentPackets=1;
		
		if (sendQueue.size() % 10 == 0)
			MetaWatchService.notifyClients();
	}

	public static void sendAdvanceHands() {
		try {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendAdvanceHands()");
			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			int hour = calendar.get(Calendar.HOUR);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);

			byte[] bytes = new byte[7];

			bytes[0] = eMessageType.start;
			bytes[1] = 9; // length
			bytes[2] = eMessageType.AdvanceWatchHandsMsg.msg; // advance watch hands
			bytes[3] = 0x04; // complete

			bytes[4] = (byte) hour;
			bytes[5] = (byte) minute;
			bytes[6] = (byte) second;

			// send(bytes);
		} catch (Exception x) {
		}
	}

	public static void sendRtcNow(Context context) {
		try {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendRtcNow()");
			boolean isMMDD = true;
			char[] ch = DateFormat.getDateFormatOrder(context);

			for (int i = 0; i < ch.length; i++) {
				if (ch[i] == DateFormat.DATE) {
					isMMDD = false;
					break;
				}
				if (ch[i] == DateFormat.MONTH) {
					isMMDD = true;
					break;
				}	
			}

			Date date = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);

			byte[] bytes = new byte[14];

			bytes[0] = eMessageType.start;
			bytes[1] = (byte) (bytes.length+2); // length
			bytes[2] = eMessageType.SetRealTimeClock.msg; // set rtc
			bytes[3] = 0x00; // not used

			bytes[4] = (byte) (year/256);
			bytes[5] = (byte) (year%256);
			bytes[6] = (byte) (calendar.get(Calendar.MONTH) + 1);
			bytes[7] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
			bytes[8] = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 1);
			bytes[9] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
			bytes[10] = (byte) calendar.get(Calendar.MINUTE);
			bytes[11] = (byte) calendar.get(Calendar.SECOND);
			if (DateFormat.is24HourFormat(context))
				bytes[12] = (byte) 1; // 24hr
			else
				bytes[12] = (byte) 0; // 12hr
			if (isMMDD)
				bytes[13] = (byte) 0; // mm/dd
			else
				bytes[13] = (byte) 1; // dd/mm

			//send(bytes);
			enqueue(bytes);
			//processSendQueue();

		} catch (Exception x) {
		}
	}

	public static byte[] crc(byte[] bytes) {
		byte[] result = new byte[2];
		short crc = (short) 0xFFFF;
		for (int j = 0; j < bytes.length; j++) {
			byte c = bytes[j];
			for (int i = 7; i >= 0; i--) {
				boolean c15 = ((crc >> 15 & 1) == 1);
				boolean bit = ((c >> (7 - i) & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= 0x1021; // 0001 0000 0010 0001 (0, 5, 12)
			}
		}
		int crc2 = crc - 0xffff0000;
		result[0] = (byte) (crc2 % 256);
		result[1] = (byte) (crc2 / 256);
		return result;
	}

	public static Bitmap createTextBitmap(Context context, String text) {

		FontCache.FontInfo font = FontCache.instance(context).Get();

		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(font.size);
		if( Preferences.notificationCenter ) {
			paint.setTextAlign(Align.CENTER);	
		}
		paint.setTypeface(font.face);
		canvas.drawColor(Color.WHITE);
		canvas = breakText(canvas, text, paint, 0, 0);
		/*
		 * FileOutputStream fos = new FileOutputStream("/sdcard/test.png");
		 * image.compress(Bitmap.CompressFormat.PNG, 100, fos); fos.close();
		 * if (Preferences.logging) Log.d("ow", "bmp ok");
		 */
		return bitmap;
	}

	public static Canvas breakText(Canvas canvas, String text, Paint pen,
			int x, int y) {
		TextPaint textPaint = new TextPaint(pen);
		StaticLayout staticLayout = new StaticLayout(text, textPaint, 94,
				android.text.Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);

		int top = 1;
		int left = 1;
		if( Preferences.notificationCenter ) {
			top = 48 - (staticLayout.getHeight()/2);
			if( top<1 ) {
				top = 1;
			}
			left = 48;
		}

		canvas.translate(left, top); // position the text
		staticLayout.draw(canvas);
		return canvas;
	}

	public static void loadTemplate(int mode) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.loadTemplate(): mode=" + mode);
		byte[] bytes = new byte[5];

		bytes[0] = 0x01;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.LoadTemplate.msg; // load template
		bytes[3] = (byte) mode;

		bytes[4] = (byte) 0; // write all "0"

		enqueue(bytes);
	}

	public static void updateDisplay(int bufferType) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.updateDisplay(): bufferType="+bufferType);
		byte[] bytes = new byte[4];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.UpdateDisplay.msg; // update display
		bytes[3] = (byte) (bufferType + 16);

		enqueue(bytes);
	}

	public static void vibrate(int on, int off, int cycles) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.vibrate(): on="+on+" off="+off+" cycles="+cycles);
		byte[] bytes = new byte[10];

		bytes[0] = eMessageType.start;
		bytes[1] = 12; // delka
		bytes[2] = eMessageType.SetVibrateMode.msg; // set vibrate
		bytes[3] = 0x00; // unused

		bytes[4] = 0x01; // enabled
		bytes[5] = (byte) (on % 256);
		bytes[6] = (byte) (on / 256);
		bytes[7] = (byte) (off % 256);
		bytes[8] = (byte) (off / 256);
		bytes[9] = (byte) cycles;

		enqueue(bytes);
	}

	public static void writeBuffer() {

		//for (int j = 0; j < 96; j = j+2) {		
		byte[] bytes = new byte[17];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.WriteBuffer.msg; // write lcd buffer
		//bytes[3] = 0x02; // notif, two lines
		//bytes[3] = 18;
		bytes[3] = 0;
		//bytes[3] = 16;

		bytes[4] = 31;

		bytes[5] = 15;
		bytes[6] = 15;
		bytes[7] = 15;
		bytes[8] = 15;
		bytes[9] = 15;
		bytes[10] = 15;
		bytes[11] = 15;
		bytes[12] = 15;
		bytes[13] = 15;
		bytes[14] = 15;
		bytes[15] = 15;
		bytes[16] = 15;

		enqueue(bytes);
		//processSendQueue();
		//}
	}
	
	public static void enableButton(int button, int type, int code, int mode) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.enableButton(): button="+button+" type="+type+" code=" + code);
		byte[] bytes = new byte[9];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.EnableButtonMsg.msg; // enable button
		bytes[3] = 0; // not used

		bytes[4] = (byte) mode; // (idle,etc)
		bytes[5] = (byte) button;
		bytes[6] = (byte) type; // immediate
		bytes[7] = 0x34;
		bytes[8] = (byte) code;

		enqueue(bytes);
	}
	
	public static void disableButton(int button, int type, int mode) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.disableButton(): button="+button+" type="+type);
		byte[] bytes = new byte[7];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.DisableButtonMsg.msg; // disable button
		bytes[3] = 0; // not used

		bytes[4] = (byte) mode; // (idle,etc)
		bytes[5] = (byte) button;
		bytes[6] = (byte) type; // immediate

		enqueue(bytes);
	}

	public static void enableReplayButton() {
		enableButton(1, 0, REPLAY, 0);
	}

	public static void disableReplayButton() {
		disableButton(1, 0, 0);
	}

	public static void enableMediaButtons() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "enableMediaButtons()");

		enableButton(1, 0, MediaControl.TOGGLE, 1); // right middle - immediate

		enableButton(5, 0, MediaControl.VOLUME_DOWN, 1); // left middle - press
		enableButton(5, 2, MediaControl.PREVIOUS, 1); // left middle - hold
		enableButton(5, 3, MediaControl.PREVIOUS, 1); // left middle - long hold
		
		enableButton(6, 0, MediaControl.VOLUME_UP, 1); // left top - press
		enableButton(6, 2, MediaControl.NEXT, 1); // left top - hold
		enableButton(6, 3, MediaControl.NEXT, 1); // left top - long hold
	}

	public static void disableMediaButtons() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "disableMediaButtons()");
		
		disableButton(1, 0, 1);
	
		disableButton(5, 0, 1);
		//disableButton(5, 1, 1);
		disableButton(5, 2, 1);
		disableButton(5, 3, 1);

		disableButton(6, 0, 1);
		//disableButton(6, 1, 1);
		disableButton(6, 2, 1);
		disableButton(6, 3, 1);
	}

	public static void readButtonConfiguration() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.readButtonConfiguration()");
		byte[] bytes = new byte[9];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.ReadButtonConfigMsg.msg; // 
		bytes[3] = 0; // not used

		bytes[4] = 0;
		bytes[5] = 1;
		bytes[6] = 2; // press type
		bytes[7] = 0x34;
		bytes[8] = 0;

		enqueue(bytes);
	}

	public static void configureMode() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.configureMode()");
		byte[] bytes = new byte[6];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.ConfigureMode.msg; // 
		bytes[3] = 0;

		bytes[4] = 10;
		bytes[5] = (byte) (MetaWatchService.Preferences.invertLCD ? 1 : 0); // invert

		enqueue(bytes);
	}

	public static void configureIdleBufferSize(boolean showClock) {
		
		if(idleShowClock !=showClock) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.configureIdleBufferSize("+showClock+")");
			
			idleShowClock = showClock;
			
			byte[] bytes = new byte[5];
	
			bytes[0] = eMessageType.start;
			bytes[1] = (byte) (bytes.length+2); // length
			bytes[2] = eMessageType.ConfigureIdleBufferSize.msg; 
			bytes[3] = 0;
			bytes[4] = (byte) (idleShowClock ? 0 : 1);
			
			enqueue(bytes);
		}
	}
	
	public static void getDeviceType() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.getDeviceType()");
		byte[] bytes = new byte[4];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.GetDeviceType.msg;
		bytes[3] = 0;

		enqueue(bytes);
	}

	public static void readBatteryVoltage() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.readBatteryVoltage()");
		byte[] bytes = new byte[4];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.ReadBatteryVoltageMsg.msg;
		bytes[3] = 0;

		enqueue(bytes);		
	}

	public static void readLightSensor() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.readLightSensor()");
		byte[] bytes = new byte[4];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.ReadLightSensorMsg.msg;
		bytes[3] = 0;

		enqueue(bytes);		
	}

	public static void queryNvalTime() {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.queryNvalTime()");
		byte[] bytes = new byte[7];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.NvalOperationMsg.msg; // nval operations
		bytes[3] = 0x01; // read

		bytes[4] = 0x09;
		bytes[5] = 0x20;
		bytes[6] = 0x01; // size

		enqueue(bytes);
	}

	public static void setNvalTime(Context context) {
		// Set the watch to 12h or 24h mode, depending on watch setting
		if (DateFormat.is24HourFormat(context)) {
			Protocol.setNvalTime(true);
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Setting watch to 24h format");
		}
		else {
			Protocol.setNvalTime(false);
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Setting watch to 12h format");
		}
	}

	public static void setNvalTime(boolean militaryTime) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.setNvalTime()");
		byte[] bytes = new byte[8];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.NvalOperationMsg.msg; // nval operations
		bytes[3] = 0x02; // write

		bytes[4] = 0x09;
		bytes[5] = 0x20;
		bytes[6] = 0x01; // size
		if (militaryTime)
			bytes[7] = 0x01; // 24 hour mode
		else
			bytes[7] = 0x00; // 12 hour mode

		enqueue(bytes);
	}

	public static void ledChange(boolean ledOn) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.ledChange()");
		byte[] bytes = new byte[4];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.LedChange.msg;
		bytes[3] = ledOn ? (byte)0x01 : (byte)0x00;

		enqueue(bytes);		
	}	

	public static void test(Context context) {
		sendOledDisplay(createOled1line(context, null, "test abc 123"), true,
				false);
		sendOledDisplay(
				createOled2lines(context, "second display", "second line 123"),
				false, true);
		byte[] display = new byte[800];
		createOled2linesLong(context,
				"long test text, long test text, long test text...", display);
		sendOledBufferPart(display, 0, 80, true, true);
	}

	public static byte[] createOled1line(Context context, String icon,
			String line) {
		int offset = 0;

		if (icon != null)
			offset += 17;

		Bitmap image = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Large.size);
		paint.setTypeface(FontCache.instance(context).Large.face);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(line, offset, 14, paint);

		if (icon != null) {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, icon), 0, 0,
					null);
		}

		int poleInt[] = new int[16 * 80];
		image.getPixels(poleInt, 0, 80, 0, 0, 80, 16);

		byte[] display = new byte[160];

		for (int i = 0; i < 160; i++) {
			boolean[] column = new boolean[8];
			for (int j = 0; j < 8; j++) {
				if (i < 80) {
					if (poleInt[80 * j + i] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				} else {
					if (poleInt[80 * 8 + 80 * j + i - 80] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				}
			}
			for (int j = 0; j < 8; j++) {
				if (column[j])
					display[i] += Math.pow(2, j);
			}
		}

		return display;
	}

	public static byte[] createOled2lines(Context context, String line1,
			String line2) {
		int offset = 0;
		/*
		 * if (logo) offset += 17;
		 */

		/* Convert newlines to spaces */
		line1 = line1.replace('\n', ' ');
		line2 = line2.replace('\n', ' ');

		Bitmap image = Bitmap.createBitmap(80, 16, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Small.size);
		paint.setTypeface(FontCache.instance(context).Small.face);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(line1, offset, 7, paint);
		canvas.drawText(line2, offset, 15, paint);

		/*
		 * if (logo) { Bitmap imageImmutable =
		 * BitmapFactory.decodeResource(context.getResources(), iconType);
		 * Bitmap imageIcon = imageImmutable.copy(Bitmap.Config.RGB_565, true);
		 * canvas.drawBitmap(imageIcon, 0, 0, null); }
		 */

		int poleInt[] = new int[16 * 80];
		image.getPixels(poleInt, 0, 80, 0, 0, 80, 16);

		byte[] display = new byte[160];

		for (int i = 0; i < 160; i++) {
			boolean[] column = new boolean[8];
			for (int j = 0; j < 8; j++) {
				if (i < 80) {
					if (poleInt[80 * j + i] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				} else {
					if (poleInt[80 * 8 + 80 * j + i - 80] == Color.WHITE)
						column[j] = false;
					else
						column[j] = true;
				}
			}
			for (int j = 0; j < 8; j++) {
				if (column[j])
					display[i] += Math.pow(2, j);
			}
		}

		return display;
	}

	public static int createOled2linesLong(Context context, String line,
			byte[] display) {
		int offset = 0 - 79;
		/*
		 * if (logo) offset += 17;
		 */

		/* Replace newlines with spaces */
		line = line.replace('\n', ' ');

		final int width = 800;

		Bitmap image = Bitmap.createBitmap(width, 8, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(image);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Small.size);
		paint.setTypeface(FontCache.instance(context).Small.face);
		canvas.drawColor(Color.WHITE);
		canvas.drawText(line, offset, 7, paint);

		/*
		 * if (logo) { Bitmap imageImmutable =
		 * BitmapFactory.decodeResource(context.getResources(), iconType);
		 * Bitmap imageIcon = imageImmutable.copy(Bitmap.Config.RGB_565, true);
		 * canvas.drawBitmap(imageIcon, 0, 0, null); }
		 */

		int poleInt[] = new int[8 * width];
		image.getPixels(poleInt, 0, width, 0, 0, width, 8);

		for (int i = 0; i < width; i++) {
			boolean[] column = new boolean[8];
			for (int j = 0; j < 8; j++) {
				if (poleInt[width * j + i] == Color.WHITE)
					column[j] = false;
				else
					column[j] = true;
			}
			for (int j = 0; j < 8; j++) {
				if (column[j])
					display[i] += Math.pow(2, j);
			}
		}
		// int len = (int) paint.measureText(line);

		return (int) paint.measureText(line) - 79;
	}

	public static void sendOledDisplay(byte[] display, boolean top,
			boolean scroll) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendOledDisplay()");
		try {

			byte[] bytes;

			for (int a = 0; a < 160; a += 20) {
				bytes = new byte[27];
				bytes[0] = eMessageType.start;
				bytes[1] = (byte) (bytes.length+2); // length
				bytes[2] = eMessageType.OledWriteBufferMsg.msg; // oled write
				if (scroll)				
					bytes[3] = (byte) 0x82; // notification + scroll
				else
					bytes[3] = 0x02; // notification

				if (top)
					bytes[4] = 0x00; // top page
				else
					bytes[4] = 0x01; // bottom page
				bytes[5] = (byte) a; // row
				bytes[6] = 0x14; // size

				System.arraycopy(display, a, bytes, 7, 20);

				enqueue(bytes);
			}

			updateOledNotification(top, scroll);

		} catch (Exception x) {
			if (Preferences.logging) Log.e(MetaWatch.TAG, "Protocol.sendOledDisplay(): exception occured", x);
		}
	}

	public static void updateOledNotification(boolean top, boolean scroll) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.updateOledNotification(): top="+top+" scroll="+scroll);
		byte[] bytes = new byte[7];

		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.OledWriteBufferMsg.msg; // oled write
		if (scroll)
			bytes[3] = (byte) 0xC2; // notification, activate, scroll
		else
			bytes[3] = 0x42; // notification, activate

		if (top)
			bytes[4] = 0x00; // top page
		else
			bytes[4] = 0x01; // bottom page
		bytes[5] = 0x00; // row
		bytes[6] = 0x00; // size

		enqueue(bytes);
	}

	public static void updateOledsNotification() {
		updateOledNotification(true, false);
		updateOledNotification(false, false);
	}

	public static void sendOledBuffer(boolean startScroll) {
		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendOledBuffer(): startScroll=" + startScroll);
		byte[] bytes = new byte[25];
		bytes[0] = eMessageType.start;
		bytes[1] = (byte) (bytes.length+2); // length
		bytes[2] = eMessageType.OledWriteScrollBufferMsg.msg; // write oled buffer		
		if (startScroll)
			bytes[3] = 0x02; // not last, start
		else
			bytes[3] = 0x00; // not last

		bytes[4] = 20; // size
		for (int i = 0; i < 20; i++)
			bytes[5 + i] = (byte) 0xAA;

		enqueue(bytes);
	}

	public static void sendOledBufferPart(byte[] display, int start,
			int length, boolean startScroll, boolean last) {

		if (Preferences.logging) Log.d(MetaWatch.TAG, "Protocol.sendOledBufferPart(): sending oled buffer part, start: " + start
				+ ", length: " + length);

		for (int j = start; j < start + length; j += 20) {
			byte[] bytes = new byte[25];
			bytes[0] = eMessageType.start;
			bytes[1] = (byte) (bytes.length+2); // length
			bytes[2] = eMessageType.OledWriteScrollBufferMsg.msg; // write oled buffer		
			bytes[3] = 0x00; // not last

			if (j + 20 >= start + length) { // is last packet
				if (startScroll)
					bytes[3] = 0x02; // not last, start
				if (last)
					bytes[3] = 0x01; // last
				if (startScroll && last)
					bytes[3] = 0x03; // last, start
			}

			bytes[4] = 20; // size
			for (int i = 0; i < 20; i++)
				bytes[5 + i] = display[j + i];

			enqueue(bytes);
		}

	}

	public static int getQueueLength() {
		return sendQueue.size();
	}

}
