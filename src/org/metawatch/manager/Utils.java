                                                                     
                                                                     
                                                                     
                                             
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
  * Utils.java                                                                *
  * Utils                                                                     *
  * Different utils                                                           *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import java.io.IOException;
import java.io.InputStream;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class Utils {

	public static String getContactNameFromNumber(Context context, String number) {
		
		if (number.equals(""))
			return "Private number";

		String[] projection = new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup.NUMBER };
		Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
		
		if (c.moveToFirst()) {
			String name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));

			if (name.length() > 0)
				return name;
			else
				return number;
		}
		
		return number;		 
	}
	
	public static int getUnreadSmsCount(Context context) {

		int count = 0;

		Cursor cursor = context.getContentResolver().query(
				Uri.withAppendedPath(Uri.parse("content://sms"), "inbox"), 
				new String[] { "_id" }, 
				"read=0", 
				null, 
				null
			);
		
		if (cursor != null) {
			try {
				count = cursor.getCount();
			} finally {
				cursor.close();
			}
		}
		return count;
	}
	
	public static int getMissedCallsCount(Context context) {
		int missed = 0;
		try {
			Cursor cursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, null);
			cursor.moveToFirst();

			while (true) {
				if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == 3)
					missed += cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW));

				if (cursor.isLast())
					break;

				cursor.moveToNext();
			}

		} catch (Exception x) {
		}
		return missed;
	}
	
	public static int getUnreadGmailCount(Context context, String account, String label) {
		try {
			int nameColumn = 0;

			Cursor c = context.getContentResolver().query(Uri.parse("content://gmail-ls/labels/" + account), null, null, null, null);
			c.moveToFirst();

			for (int i = 0; i < c.getColumnCount(); i++)
				if (c.getColumnName(i).equals("canonicalName")) {
					nameColumn = i;
					break;
				}

			while (true) {
				if (c.getString(nameColumn).equals(label))
					for (int i = 0; i < c.getColumnCount(); i++) {
						if (c.getColumnName(i).equals("numUnreadConversations")) {
							return Integer.parseInt(c.getString(i));
						}
					}

				c.moveToNext();

				if (c.isLast()) {
					break;
				}
			}
		} catch (Exception x) {
			Log.d(MetaWatch.TAG, x.toString());
		}

		return 0;
	}
	
	public static String getGoogleAccountName(Context context) {
		AccountManager accountManager = AccountManager.get(context);
		Account[] accounts = accountManager.getAccounts();
		int count = accounts.length;
		Account account = null;

		for (int i = 0; i < count; i++) {
			account = accounts[i];
			if (account.type.equals("com.google")) {
				return account.name;
			}
		}
		return "";
	}
	
	public static Bitmap loadBitmapFromAssets(Context context, String path) {
		try {
			InputStream inputStream = context.getAssets().open(path);
	        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	        inputStream.close();
	        //Log.d(MetaWatch.TAG, "ok");
	        return bitmap;
		} catch (IOException e) {
			//Log.d(MetaWatch.TAG, e.toString());
			return null;
		}
	}
	/*
	public static Bitmap loadBitmapFromPath(Context context, String path) {
			return BitmapFactory.decodeFile(path);
	}
	*/
	
	public static String getVersion(Context context) {		
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		return "unknown";
	}
	
	public static boolean isGmailAccessSupported(Context context) {
		
		
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo("com.google.android.gm", 0);
			// check for Gmail version earlier than v2.3.5 (169)
			if (packageInfo.versionCode < 169)
					return true;			
			
		} catch (NameNotFoundException e) {
		}
		
		
		return false;
	}
	

}
