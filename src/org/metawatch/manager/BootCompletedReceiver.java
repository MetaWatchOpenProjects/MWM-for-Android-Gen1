package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent)
	{
		 if(Preferences.autoConnect) {
			 context.startService(new Intent(context, MetaWatchService.class));
			 if (Preferences.logging) Log.v(MetaWatch.TAG, "Service loaded at start");
		 }
	     
	}

}
