                                                                     
                                                                     
                                                                     
                                             
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
  * MetaWatch.java                                                            *
  * MetaWatch                                                                 *
  * Main activity with menu                                                            *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

public class MetaWatch extends Activity {

	public static final String TAG = "MetaWatch";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(getString(R.string.app_name));
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		MetaWatchService.loadPreferences(this);
		
		if (Preferences.idleMusicControls)
			Protocol.enableMediaButtons();
		//else 
			//Protocol.disableMediaButtons();
		
		if (Preferences.idleReplay)
			Protocol.enableReplayButton();
		//else
			//Protocol.disableReplayButton();
		
		Protocol.configureMode();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case R.id.start:
	    	startService();
	        return true;
	    case R.id.stop:	   
	    	stopService();
	        return true;
	    case R.id.test:
	    	startActivity(new Intent(this, Test.class));
	        return true;
	    case R.id.settings:	 
	    	startActivity(new Intent(this, Settings.class));
	        return true;
	    case R.id.about:
	    	showAbout();
	        return true;
	    case R.id.exit:	        
	    	exit();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
    
	void startService() {
		startService(new Intent(this, MetaWatchService.class));
	}
	
    void stopService() {
    	stopService(new Intent(this, MetaWatchService.class));
    }
    
    void exit() {
    	System.exit(0);
    }
    
    void showAbout() {
    	
    	WebView webView = new WebView(this);
		String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>About</title></head><body>" + 
						"<h1>MetaWatch</h1>" +
						"<p>Version " + Utils.getVersion(this) + ".</p>" +
						"<p>&copy; Copyright 2011 Meta Watch Ltd.</p>" +
						"</body></html>";
        webView.loadData(html, "text/html", "utf-8");
        
        new AlertDialog.Builder(this).setView(webView).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {			
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();        
    }
    
}