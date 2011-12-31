package org.metawatch.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SendTextReceiver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(MetaWatch.TAG, "SendTextReciever created");   
		
        Intent i = getIntent();
        
        Log.d(MetaWatch.TAG, "action: " + i.getAction());                        
        //Log.d(MetaWatch.TAG, "data: "+ i.getData().getPath() );
        
        if (Intent.ACTION_SEND.equals(i.getAction())) 
		{  

			Bundle bundle = i.getExtras();
			
			Log.d(MetaWatch.TAG, "extras: " + bundle.keySet().toString());   
			
			String title="Message";
			
			if (bundle.containsKey(Intent.EXTRA_TITLE)) {
		          
	        	title = bundle.getString(Intent.EXTRA_TITLE);
	        	title = title.replaceAll("\\p{Cntrl}", "");
			}
			
	        if (bundle.containsKey(Intent.EXTRA_TEXT)) {
	           
	        	String text = bundle.getString(Intent.EXTRA_TEXT);
	            NotificationBuilder.createSmart(this, title, text);
	            Log.d(MetaWatch.TAG, text);
	          
	        } 
			
		}
        
        finish();
	}
}
