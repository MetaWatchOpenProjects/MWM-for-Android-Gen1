package org.metawatch.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent)
	{
	     context.startService(new Intent(context, MetaWatchService.class));
	     Log.v(MetaWatch.TAG, "Service loaded at start");
	}

}
