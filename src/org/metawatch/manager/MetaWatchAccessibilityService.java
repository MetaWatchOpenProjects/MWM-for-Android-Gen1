package org.metawatch.manager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MetaWatchAccessibilityService extends AccessibilityService {

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		AccessibilityServiceInfo asi = new AccessibilityServiceInfo();
		asi.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		asi.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		asi.flags = AccessibilityServiceInfo.DEFAULT;
		asi.notificationTimeout = 100;
		setServiceInfo(asi);

		// ArrayList<PInfo> apps = getInstalledApps(true);
		// for (PInfo pinfo : apps) {
		// appsByPackage.put(pinfo.pname, pinfo);
		// }
	}

	private String currentActivity = "";
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

		/* Acquire details of event. */
		int eventType = event.getEventType();
		CharSequence packageName = event.getPackageName();
		CharSequence className = event.getClassName();
				
		if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			Log.d(MetaWatch.TAG,
					"MetaWatchAccessibilityService.onAccessibilityEvent(): Received event, packageName = '"
							+ packageName + "' className = '" + className + "'");
	
			Parcelable p = event.getParcelableData();
			if (p instanceof android.app.Notification == false) {
				Log.d(MetaWatch.TAG,
						"MetaWatchAccessibilityService.onAccessibilityEvent(): Not a real notification, ignoring.");
				return;
			}
	
			android.app.Notification notification = (android.app.Notification) p;
			Log.d(MetaWatch.TAG,
					"MetaWatchAccessibilityService.onAccessibilityEvent(): notification text = '"
							+ notification.tickerText + "' flags = "
							+ notification.flags + " ("
							+ Integer.toBinaryString(notification.flags) + ")");
	
			if (notification.tickerText == null
					|| notification.tickerText.toString().trim().length() == 0) {
				Log.d(MetaWatch.TAG,
						"MetaWatchAccessibilityService.onAccessibilityEvent(): Empty text, ignoring.");
				return;
			}
	
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(this);
	
			/* Forward calendar event */
			if (packageName.equals("com.android.calendar")) {
				if (sharedPreferences.getBoolean("NotifyCalendar", true)) {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Sending calendar event: '"
									+ notification.tickerText + "'.");
					NotificationBuilder.createCalendar(this,
							notification.tickerText.toString());
					return;
				}
			}
			
			/* Forward google chat or voice event */
			if (packageName.equals("com.google.android.gsf") || packageName.equals("com.google.android.apps.googlevoice")) {
				if (sharedPreferences.getBoolean("notifySMS", true)) {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Sending SMS event: '"
									+ notification.tickerText + "'.");
					NotificationBuilder.createSMS(this,"Google Message" ,notification.tickerText.toString());
					return;
				}
			}
			
			
			/* Deezer track notification */
			if (packageName.equals("deezer.android.app")) {
				
				String text = notification.tickerText.toString().trim();
				
				int truncatePos = text.indexOf(" - ");
				if (truncatePos>-1)
				{
					String artist = text.substring(0, truncatePos);
					String track = text.substring(truncatePos+3);
					
					MediaControl.updateNowPlaying(this, artist, "", track, packageName.toString());
					
					return;
				}
				
				return;
			}
			
			if ((notification.flags & android.app.Notification.FLAG_ONGOING_EVENT) > 0) {
				/* Ignore updates to ongoing events. */
				Log.d(MetaWatch.TAG,
						"MetaWatchAccessibilityService.onAccessibilityEvent(): Ongoing event, ignoring.");
				return;
			}
			
			/* Some other notification */
			if (sharedPreferences.getBoolean("NotifyOtherNotification", true)) {
	
				String appBlacklist = sharedPreferences.getString("appBlacklist",
						AppBlacklist.DEFAULT_BLACKLIST);
	
				/* Ignore if on blacklist */
				if (appBlacklist.contains(packageName)) {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): App is blacklisted, ignoring.");
					return;
				}
	
				PackageManager pm = getPackageManager();
				PackageInfo packageInfo = null;
				String appName = null;
				try {
					packageInfo = pm.getPackageInfo(packageName.toString(), 0);
					appName = packageInfo.applicationInfo.loadLabel(pm).toString();
	
				} catch (NameNotFoundException e) {
					/* OK, appName is null */
				}
	
				if (appName == null) {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Unknown app -- sending notification: '"
									+ notification.tickerText + "'.");
					NotificationBuilder.createOtherNotification(this,
							"Notification", notification.tickerText.toString());
				} else {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Sending notification: app='"
									+ appName + "' notification='"
									+ notification.tickerText + "'.");
					NotificationBuilder.createOtherNotification(this, appName,
							notification.tickerText.toString());
				}
			}
		}
		else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
		{
			String newActivity = className.toString();
			if( MetaWatchService.Preferences.showK9Unread) {
				if (currentActivity.startsWith("com.fsck.k9")) {
					if (!newActivity.startsWith("com.fsck.k9")) {
						// User has switched away from k9, so refresh the read count
						Utils.refreshUnreadK9Count(this);
						Idle.updateLcdIdle(this);
					}
				}
			}
			
			currentActivity = newActivity;
		}
	}

	@Override
	public void onInterrupt() {
		/* Do nothing */
	}

}
