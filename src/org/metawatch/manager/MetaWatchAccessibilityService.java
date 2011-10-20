package org.metawatch.manager;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MetaWatchAccessibilityService extends AccessibilityService {

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		AccessibilityServiceInfo asi = new AccessibilityServiceInfo();
		asi.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		asi.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		asi.flags = AccessibilityServiceInfo.DEFAULT;
		asi.notificationTimeout = 100;
		setServiceInfo(asi);

		// ArrayList<PInfo> apps = getInstalledApps(true);
		// for (PInfo pinfo : apps) {
		// appsByPackage.put(pinfo.pname, pinfo);
		// }
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

		/* Acquire details of event. */
		CharSequence packageName = event.getPackageName();
		List<CharSequence> text = event.getText();

		/* Build string from text */
		StringBuilder sb = new StringBuilder();
		for (CharSequence cs : text) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(cs);
		}
		String notificationText = sb.toString().trim();

		/* Ignore notifications without text. */
		if (notificationText.length() == 0) {
			return;
		}

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String appBlacklist = sharedPreferences.getString("appBlacklist", AppBlacklist.DEFAULT_BLACKLIST);
		CharSequence className = event.getClassName();
		Log.d(MetaWatch.TAG,
				"onAccessibilityEvent(): Received event, className='"
						+ className + "' packagename='" + packageName
						+ "' text='" + sb.toString() + "'");
		

		/* Forward calendar event */
		if (packageName.equals("com.android.calendar")) {
			if (sharedPreferences.getBoolean("NotifyCalendar", true)) {
				Log.d(MetaWatch.TAG,
						"onAccessibilityEvent(): Sending calendar event: '"
								+ notificationText + "'.");
				NotificationBuilder.createCalendar(this, notificationText);
			}
		} else {
			/* Some other notification */
			if (sharedPreferences.getBoolean("NotifyOtherNotification", true)) {

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
					appName = packageInfo.applicationInfo.loadLabel(pm)
							.toString();

				} catch (NameNotFoundException e) {
					/* OK, appName is null */
				}

				if (appName == null) {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Unknown app -- sending notification: '"
									+ notificationText + "'.");
					NotificationBuilder.createOtherNotification(this,
							"Notification", notificationText);
				} else {
					Log.d(MetaWatch.TAG,
							"onAccessibilityEvent(): Sending notification: app='"
									+ appName + "' notification='"
									+ notificationText + "'.");
					NotificationBuilder.createOtherNotification(this, appName,
							notificationText);
				}

			}
		}

	}

	@Override
	public void onInterrupt() {
		/* Do nothing */
	}

}
