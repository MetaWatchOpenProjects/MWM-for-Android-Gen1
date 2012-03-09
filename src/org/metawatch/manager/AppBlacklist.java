package org.metawatch.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppBlacklist extends Activity {

	public static final String DEFAULT_BLACKLIST = "com.android.mms,com.google.android.gm,com.fsck.k9,com.android.alarmclock,com.htc.android.worldclock,com.android.deskclock,com.sonyericsson.alarm,com.motorola.blur.alarmclock";
	private List<AppInfo> appInfos;

	private class AppLoader extends AsyncTask<Void, Void, List<AppInfo>> {
		private ProgressDialog pdWait;

		@Override
		protected void onPreExecute() {
			pdWait = new ProgressDialog(AppBlacklist.this);
			pdWait.setTitle("Loading apps, please wait...");
			pdWait.show();
		}

		@Override
		protected List<AppInfo> doInBackground(Void... params) {
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(AppBlacklist.this);
			String blacklist = sharedPreferences.getString("appBlacklist",
					DEFAULT_BLACKLIST);
			PackageManager pm = getPackageManager();
			List<PackageInfo> packages = pm.getInstalledPackages(0);
			List<AppInfo> appInfos = new ArrayList<AppInfo>();
			for (PackageInfo pi : packages) {
				/* Ignore system (non-versioned) packages */
				if (pi.versionName == null) {
					continue;
				}
				/* Ignore Android System */
				if (pi.packageName.equals("android")) {
					continue;
				}
				AppInfo appInfo = new AppInfo();
				appInfo.packageInfo = pi;
				appInfo.name = appInfo.packageInfo.applicationInfo
						.loadLabel(pm).toString();
				appInfo.icon = appInfo.packageInfo.applicationInfo.loadIcon(pm);
				appInfo.isBlacklisted = blacklist.contains(pi.packageName);
				appInfos.add(appInfo);
			}
			Collections.sort(appInfos);

			// for (AppInfo appInfo : appInfos) {
			// if (Preferences.logging) Log.d(MetaWatch.TAG, "appName='" + appInfo.name
			// + "' packageName='" + appInfo.packageInfo.packageName +
			// "' selected="
			// + appInfo.selected);
			// }

			return appInfos;
		}

		@Override
		protected void onPostExecute(List<AppInfo> appInfos) {

			ListView listView = (ListView) findViewById(android.R.id.list);
			// listView.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1, menuList));
			listView.setAdapter(new BlacklistAdapter(appInfos));
			AppBlacklist.this.appInfos = appInfos;
			pdWait.dismiss();

		}

	}

	public class AppInfo implements Comparable<AppInfo> {
		String name;
		Drawable icon;
		PackageInfo packageInfo;
		boolean isBlacklisted;

		public int compareTo(AppInfo another) {
			return this.name.compareTo(another.name);
		}
	}

	class BlacklistAdapter extends ArrayAdapter<AppInfo> {
		private final List<AppInfo> apps;

		public BlacklistAdapter( List<AppInfo> apps) {
			super(AppBlacklist.this, R.layout.app_blacklist_list_item, apps);
			this.apps = apps;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			LayoutInflater inflater = AppBlacklist.this.getLayoutInflater();
			if(view == null) {
			    view = inflater.inflate(R.layout.app_blacklist_list_item, null);
			}
			ImageView icon = (ImageView) view
					.findViewById(R.id.app_blacklist_list_item_icon);
			TextView appName = (TextView) view
					.findViewById(R.id.app_blacklist_list_item_name);
			CheckBox checkbox = (CheckBox) view
					.findViewById(R.id.app_blacklist_list_item_check);
			final AppInfo appInfo = apps.get(position);
			icon.setImageDrawable(appInfo.icon);
			appName.setText(appInfo.name);
			checkbox.setChecked(appInfo.isBlacklisted);
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					appInfo.isBlacklisted = isChecked;
				}
			});
			return view;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.app_blacklist);

		AppLoader appLoader = new AppLoader();
		appLoader.execute((Void[]) null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			StringBuilder sb = new StringBuilder();
			for (AppInfo appInfo : appInfos) {
				if (appInfo.isBlacklisted) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(appInfo.packageInfo.packageName);
				}
			}
			SharedPreferences sharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			String blacklist = sb.toString();
			editor.putString("appBlacklist", blacklist);
			editor.commit();		
			if (Preferences.logging) Log.d(MetaWatch.TAG, "App blacklist: " + blacklist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
