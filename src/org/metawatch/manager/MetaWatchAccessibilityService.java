package org.metawatch.manager;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
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
		// Log.d(MetaWatch.TAG,
		// "MetaWatchAccessibilityService.onServiceConnected()");
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
		String calendarText = sb.toString();

		// CharSequence className = event.getClassName();
		// Log.d(MetaWatch.TAG,
		// "onAccessibilityEvent(): Received event, className='"
		// + className + "' packagename='" + packageName
		// + "' text='" + sb.toString() + "'");

		/* Forward calendar event */
		if (MetaWatchService.Preferences.notifyCalendar) {
			if (packageName.equals("com.android.calendar")) {
				NotificationBuilder.createCalendar(this, calendarText);
			}
		}

	}

	@Override
	public void onInterrupt() {
		/* Do nothing */
	}

}
