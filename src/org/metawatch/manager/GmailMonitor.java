                                                                     
                                                                     
                                                                     
                                             
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
  * GmailMonitor.java                                                         *
  * GmailMonitor                                                              *
  * Watching for latest Gmail e-mails, working with Gmail version older than  *
  * version 2.3.5 (excluded)                                                  *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metawatch.manager.MetaWatchService.Preferences;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GmailMonitor {

	Context context;
	
	MyContentObserver contentObserver = new MyContentObserver();
	ContentResolver contentResolver;
	
	public static String account = "";
	public static int lastUnreadGmailCount = 0;
	
	public GmailMonitor(Context ctx) {
		super();		
		context = ctx;
	}

	public void startMonitor() {
		try {
			AccountManager mgr = AccountManager.get(context);
			Account[] accts = mgr.getAccounts();
			final int count = accts.length;
			Account acct = null;

			for (int i = 0; i < count; i++) {
				acct = accts[i];
				if (acct.type.equals("com.google")) {
					account = acct.name;
					break;
				}
				// Log.d("ow",
				// "eclair account - name="+acct.name+", type="+acct.type);
			}

			Uri uri = Uri.parse("content://gmail-ls/conversations/" + account);
			contentResolver = context.getContentResolver();
			contentResolver.registerContentObserver(uri, true, contentObserver);
		} catch (Exception x) {
			Log.d(MetaWatch.TAG, x.toString());
		}
	}


	private class MyContentObserver extends ContentObserver {

		public MyContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			Log.d("ow", "onChange observer - unread");

			if (Preferences.notifyGmail) 
			{
				int currentGmailUnreadCount = getUnreadGmailCount(account, "^u");

				//Log.d("ow", "current gmail unread count: " + Integer.toString(currentGmailUnreadCount));

				if (currentGmailUnreadCount > lastUnreadGmailCount)
				{
						Log.d("ow", Integer.toString(currentGmailUnreadCount) + " > " + Integer.toString(lastUnreadGmailCount));
						sendUnreadGmail(account);						
				}
				
				lastUnreadGmailCount = currentGmailUnreadCount;
			}
		}
	}
	
	public int getUnreadGmailCount(String account, String label) {
		// label = "^u";

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
					// if (c.getString(nameColumn).equals("^i"))
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
		}

		return 0;
	}
	
	
	void sendUnreadGmail(String account) {
		try {

			int nameColumn = 0;
			String id = "";
			String convId = "";

			double maxDate = 0;

			Cursor c = context.getContentResolver().query(Uri.parse("content://gmail-ls/labels/" + account), null, null, null, null);
			c.moveToFirst();

			for (int i = 0; i < c.getColumnCount(); i++)
				if (c.getColumnName(i).equals("canonicalName"))
					nameColumn = i;

			while (true) {
				if (c.getString(nameColumn).equals("^u"))
					for (int i = 0; i < c.getColumnCount(); i++) {
						if (c.getColumnName(i).equals("_id")) {
							id = c.getString(i);
						}
					}

				if (c.isLast())
					break;

				c.moveToNext();
			}

			Cursor c2 = context.getContentResolver().query(Uri.parse("content://gmail-ls/conversations/" + account), null, null, null, null);
			c2.moveToLast();

			for (int i = 0; i < c2.getColumnCount(); i++)
				if (c2.getColumnName(i).equals("labelIds"))
					nameColumn = i;

			while (true) {
				if (c2.getString(nameColumn).indexOf(id) >= 0)
					for (int i = 0; i < c2.getColumnCount(); i++) {
						if (c2.getColumnName(i).equals("conversation_id"))
							convId = c2.getString(i);
					}

				if (c2.isFirst())
					break;

				c2.moveToPrevious();
			}
			// ///////////////

			maxDate = 0;

			int colConvId = 0;
			int colSub = 0;
			int colFrom = 0;
			int colRcv = 0;

			String subject = "";
			String sender = "";
			String snippet = "";

			Cursor c3 = context.getContentResolver().query(Uri.parse("content://gmail-ls/conversations/" + account + "/" + convId + "/messages"), null, null, null, null);
			// startManagingCursor(c3);
			c3.moveToFirst();

			for (int i = 0; i < c3.getColumnCount(); i++) {
				if (c3.getColumnName(i).equals("conversation"))
					colConvId = i;
				if (c3.getColumnName(i).equals("subject"))
					colSub = i;
				if (c3.getColumnName(i).equals("fromAddress"))
					colFrom = i;
				if (c3.getColumnName(i).equals("dateReceivedMs"))
					colRcv = i;
			}

			while (true) {
				if (c3.getString(colConvId).indexOf(convId) >= 0) {
					double thisDate = Double.parseDouble(c3.getString(colRcv));

					if (thisDate > maxDate) {
						subject = c3.getString(colSub);
						sender = c3.getString(colFrom);
						snippet = c3.getString(c3.getColumnIndex("snippet"));
						maxDate = thisDate;
					}
				}

				if (c3.isLast())
					break;

				c3.moveToNext();
			}

			Pattern pattern = Pattern.compile("(\"[^\"]*\") (<.*>)");
			Matcher matcher = pattern.matcher(sender);
			matcher.find();

	        String senderName = matcher.group(1).replace("\"", "");
	        String senderMail = matcher.group(2).replace("<", "").replace(">", "");			
			
			NotificationBuilder.createGmail(context, senderName, senderMail, subject, snippet);

		} catch (Exception x) {
		}
	}
	
}
