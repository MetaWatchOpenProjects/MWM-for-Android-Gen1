                                                                     
                                                                     
                                                                     
                                             
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
  * Main activity with tab container                                          *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/


package org.metawatch.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WeatherProvider;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;

import com.bugsense.trace.BugSenseHandler;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MetaWatch extends TabActivity {
   
	public static final String TAG = "MetaWatch";
	
	public static TextView textView = null;	
	public static ToggleButton toggleButton = null;
	
	/** Messenger for communicating with service. */
    Messenger mService = null;
	
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    
    long startupTime = 0;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If you want to use BugSense for your fork, register with them
        // and place your API key in /assets/bugsense.txt
        // (This prevents me receiving reports of crashes from forked versions
        // which is somewhat confusing!)      
        try {
			InputStream inputStream = getAssets().open("bugsense.txt");
			String key = Utils.ReadInputStream(inputStream);
			key=key.trim();
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Using bugsense key '"+key+"'");
			BugSenseHandler.setup(this, key);
		} catch (IOException e) {
			if (Preferences.logging) Log.d(MetaWatch.TAG, "No bugsense keyfile found");
		}
        
        startupTime = System.currentTimeMillis();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        final Resources res = getResources();
        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("Status",res.getDrawable(R.drawable.ic_tab_status))
                .setContent(new Intent(this, MetaWatchStatus.class)));

        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator("Preferences",res.getDrawable(R.drawable.ic_tab_settings))
                .setContent(new Intent(this, Settings.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator("Widgets",res.getDrawable(R.drawable.ic_tab_widgets))
                .setContent(new Intent(this, WidgetSetup.class)));
        
        tabHost.addTab(tabHost.newTabSpec("tab4")
                .setIndicator("Tests",res.getDrawable(R.drawable.ic_tab_test))
                .setContent(new Intent(this, Test.class)));
        
        // This tab sets the intent flag so that it is recreated each time
        // the tab is clicked.
        //tabHost.addTab(tabHost.newTabSpec("tab3")
        //        .setIndicator("destroy")
        //        .setContent(new Intent(this, Controls2.class)
        //                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        
        synchronized (MetaWatchStatus.textView) {
        	if (MetaWatchStatus.textView==null) {
		        try {
					MetaWatchStatus.textView.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					finish();
				}
        	}
	        textView = MetaWatchStatus.textView;
	        toggleButton = MetaWatchStatus.toggleButton;
        }

		MetaWatchService.loadPreferences(this);
		
		if (Preferences.watchMacAddress == "") {
			// Show the watch discovery screen on first start
			startActivity(new Intent(getApplicationContext(), DeviceSelection.class));
		}
		
	
		toggleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(toggleButton.isChecked())
            		startService();
            	else
            		stopService();
            }
        });
		
		displayStatus();
		
		Protocol.configureMode();
		
		if (isServiceRunning() || Preferences.autoConnect) {
			startService();
		}
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

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
		Context context = getApplicationContext();
		//context.startService(new Intent(this, MetaWatchService.class));
		
		context.bindService(new Intent(MetaWatch.this, 
                MetaWatchService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        
        toggleButton.setChecked(true);
	}
	
    void stopService() {
    	
    	if (mService != null) {
    		Context context = getApplicationContext();
            try {
                Message msg = Message.obtain(null,
                        MetaWatchService.Msg.UNREGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // There is nothing special we need to do if the service
                // has crashed.
            }
            
            try {
            	stopService(new Intent(this, MetaWatchService.class));
                // Detach our existing connection.
                context.unbindService(mConnection);
                mIsBound = false;
            }
            catch(IllegalArgumentException e) {
            	// The service wasn't running
            	if (Preferences.logging) Log.d(MetaWatch.TAG, e.getMessage());          	
            }
        }
    	
    	toggleButton.setChecked(false);

        displayStatus();
    }
    
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("org.metawatch.manager.MetaWatchService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    void exit() {
    	System.exit(0);
    }
    
    void showAbout() {
    	
    	WebView webView = new WebView(this);
		String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>About</title></head><body><center>" + 
						"<h1>MetaWatch</h1>" +
						"<img src=\"splash.png\">" +
						"<p>Version " + Utils.getVersion(this) + ".</p>" +
						"<p>Modified by Dobie Wollert, Chris Sewell, Prash D, Craig Oliver, Richard Munn, Matthias Gruenewald and Kyle Schroeder.</p>" +
						"<p>© Copyright 2011-2012 Meta Watch Ltd.</p>" +
						"</center></body></html>";
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
        
        new AlertDialog.Builder(this).setView(webView).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {			
			//@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();        
    }
    
    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MetaWatchService.Msg.UPDATE_STATUS:
                    displayStatus();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    private void displayStatus() {
    	textView.setText("MetaWatch Manager\n\n");
    	
    	switch (MetaWatchService.connectionState) {
	    	case MetaWatchService.ConnectionState.DISCONNECTED:
	    		Utils.appendColoredText(textView, "DISCONNECTED\n", Color.RED);
	    		break;
	    	case MetaWatchService.ConnectionState.CONNECTING:
	    		Utils.appendColoredText(textView, "CONNECTING\n", Color.YELLOW);
	    		break;
	    	case MetaWatchService.ConnectionState.CONNECTED:
	    		Utils.appendColoredText(textView, "CONNECTED\n", Color.GREEN);
	    		break;
	    	case MetaWatchService.ConnectionState.DISCONNECTING:
	    		Utils.appendColoredText(textView, "DISCONNECTING\n", Color.YELLOW);
	    		break;
    	}
    	
    	if (Preferences.weatherProvider != WeatherProvider.DISABLED) {
    		textView.append("\n");
    		if (WeatherData.received) {
    			textView.append("Weather last updated:\n");
    			textView.append("  Forecast:\n    ");
    			printDate(WeatherData.forecastTimeStamp);
    			textView.append("  Current Observation:\n    ");
    			printDate(WeatherData.timeStamp);
    		}
    		else {
    			textView.append("Waiting for weather data.\n");
    		}
    	}
    	
    	if (Preferences.weatherGeolocation) {
    		textView.append("\n");
    		if (LocationData.received) {
    			textView.append("Location last updated:\n  ");
    			printDate(LocationData.timeStamp);
    		}
    		else {
    			textView.append("Waiting for location data.\n");
    		}
    	}
    	
    	if (Utils.isAccessibilityEnabled(this)) {
	    	if (MetaWatchAccessibilityService.accessibilityReceived) {
	    		Utils.appendColoredText(textView, "\nAccessibility enabled and working\n", Color.GREEN);
	    	}
	    	else {
	    		if(startupTime==0 || System.currentTimeMillis()-startupTime<60*1000) {
	    			textView.append("\nAccessibility enabled - waiting for notifications\n");
	    		}
	    		else {
	    			Utils.appendColoredText(textView, "\nAccessibility not working - disable and renable Accessibility in the system settings\n", Color.RED);
	    		}
	    	}
	    }
    	else {
    		textView.append("\nAccessibility disabled\n");
    	}
    
    	textView.append("\nMessage Queue Length: " + Protocol.getQueueLength());
    	textView.append("\nNotification Queue Length: " + Notification.getQueueLength() + "\n");
    }
    
    private void printDate(long ticks) {
    	if(ticks==0) {
    		textView.append("...loading...");
    	}
    	else {
	    	final Calendar cal = Calendar.getInstance();
	    	cal.setTimeInMillis(ticks);
	    	Date date = cal.getTime();
	    	textView.append(DateFormat.getDateFormat(this).format(date)+" "+DateFormat.getTimeFormat(this).format(date));
    	}
    	textView.append("\n");
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            textView.append("Attached to service\n");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        MetaWatchService.Msg.REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            textView.append("Disconnected from service\n");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MetaWatch.this, 
                MetaWatchService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        textView.append("Binding.\n");
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            MetaWatchService.Msg.UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            textView.append("Binding.\n");
        }
    }
}
