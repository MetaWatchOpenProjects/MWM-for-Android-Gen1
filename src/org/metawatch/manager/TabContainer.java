package org.metawatch.manager;

import java.io.IOException;
import java.io.InputStream;

import com.bugsense.trace.BugSenseHandler;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;

public class TabContainer  extends TabActivity {

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
			Log.d(MetaWatch.TAG, "Using bugsense key '"+key+"'");
			BugSenseHandler.setup(this, key);
		} catch (IOException e) {
			Log.d(MetaWatch.TAG, "No bugsense keyfile found");
		}
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        final Resources res = getResources();
        final TabHost tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator("Status",res.getDrawable(R.drawable.ic_tab_status))
                .setContent(new Intent(this, MetaWatch.class)));

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
    }
}
