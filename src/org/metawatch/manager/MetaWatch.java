                                                                     
                                                                     
                                                                     
                                             
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

import java.util.Calendar;
import java.util.Date;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Monitors.LocationData;
import org.metawatch.manager.Monitors.WeatherData;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class MetaWatch extends Activity {

	public static final String TAG = "MetaWatch";
	
	private TextView textView;
	private Button buttonStart;
	private Button buttonStop;
	
	/** Messenger for communicating with service. */
    Messenger mService = null;
	
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        BugSenseHandler.setup(this, "49eba7ee");
             
        textView = (TextView) findViewById(R.id.textview);
        
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
		
		if (Preferences.watchMacAddress == "") {
			// Show the watch discovery screen on first start
			startActivity(new Intent(getApplicationContext(), DeviceSelection.class));
		}
		
		if (Preferences.idleMusicControls)
			Protocol.enableMediaButtons();
		//else 
			//Protocol.disableMediaButtons();
		
		buttonStart = (Button) findViewById(R.id.start);
		buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService();
            }
        });
		
		buttonStop = (Button) findViewById(R.id.stop);
		buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
		
        bindService(new Intent(MetaWatch.this, 
                MetaWatchService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
	}
	
    void stopService() {
    	
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
            
            stopService(new Intent(this, MetaWatchService.class));
            
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
        }

        // Detach our existing connection.
        unbindService(mConnection);
        mIsBound = false;
        
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
		String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>About</title></head><body>" + 
						"<h1>MetaWatch</h1>" +
						"<p>Version " + Utils.getVersion(this) + ".</p>" +
						"<p>Modified by Dobie Wollert, Chris Sewell, Prash D, Craig Oliver and Richard Munn.</p>" +
						"<p>© Copyright 2011 Meta Watch Ltd.</p>" +
						"</body></html>";
        webView.loadData(html, "text/html", "utf-8");
        
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
	    		textView.append("DISCONNECTED\n");
	    		break;
	    	case MetaWatchService.ConnectionState.CONNECTING:
	    		textView.append("CONNECTING\n");
	    		break;
	    	case MetaWatchService.ConnectionState.CONNECTED:
	    		textView.append("CONNECTED\n");
	    		break;
	    	case MetaWatchService.ConnectionState.DISCONNECTING:
	    		textView.append("DISCONNECTING\n");
	    		break;
    	}
    	
    	if (!Preferences.disableWeather) {
    		textView.append("\n");
    		if (WeatherData.received)
    		{
    			textView.append("Weather last updated:\n");
    			printDate(WeatherData.timeStamp);
    		}
    		else
    		{
    			textView.append("Waiting for weather data.\n");
    		}
    	}
    	
    	if (Preferences.weatherGeolocation) {
    		textView.append("\n");
    		if (LocationData.received)
    		{
    			textView.append("Location last updated:\n");
    			printDate(LocationData.timeStamp);
    		}
    		else
    		{
    			textView.append("Waiting for location data.\n");
    		}
    	}
    	
    	textView.append("\nMessage Queue Length " + Protocol.getQueueLength() + "\n");
    	
    	
    }
    
    private void printDate(long ticks) {
    	final Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(ticks);
    	Date date = cal.getTime();
    	textView.append(date.toLocaleString());
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

                //// Give it some value as an example.
                //msg = Message.obtain(null,
                //        MessengerService.MSG_SET_VALUE, this.hashCode(), 0);
                //mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            //Toast.makeText(Binding.this, R.string.remote_service_connected,
            //        Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            textView.append("Disconnected from service\n");

            //// As part of the sample, tell the user what happened.
            //Toast.makeText(Binding.this, R.string.remote_service_disconnected,
            //        Toast.LENGTH_SHORT).show();
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