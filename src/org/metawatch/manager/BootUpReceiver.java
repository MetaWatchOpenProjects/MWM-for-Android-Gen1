package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		
		if (sharedPreferences.getBoolean("StartOnBoot", Preferences.startOnBoot)) {
        	context.startService(new Intent(context, MetaWatchService.class));
		}
        	
    }

}
