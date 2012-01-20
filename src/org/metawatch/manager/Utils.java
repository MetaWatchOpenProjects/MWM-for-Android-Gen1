                                                                     
                                                                     
                                                                     
                                             
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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class Utils {

	public static String getContactNameFromNumber(Context context, String number) {
		
		try {
			if (number.equals(""))
				return "Private number";
	
			String[] projection = new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup.NUMBER };
			
			Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
			
			if (c==null)
				return number;
			
			if (c.moveToFirst()) {
				String name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
	
				if (name.length() > 0)
					return name;
				else
					return number;
			}
			
			c.close();
			return number;
		}
		catch(java.lang.IllegalStateException e) {
			return number;
		}
	}
	
	public static Bitmap getContactPhotoFromNumber(Context context, String number) {

		try {
			if (number.equals(""))
				return null;
	
			String[] projection = new String[] {PhoneLookup.PHOTO_ID, PhoneLookup.NUMBER};
			
			Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			Cursor c = context.getContentResolver().query(contactUri, projection, null, null, null);
			
			if (c==null)
				return null;
			
			if (c.moveToFirst()) {
				int photoID = c.getInt(c.getColumnIndex(PhoneLookup.PHOTO_ID));
				c.close();

				Uri photoUri = ContactsContract.Data.CONTENT_URI;
				c = context.getContentResolver().query(photoUri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO, ContactsContract.Data.PHOTO_ID}, Data.PHOTO_ID + " = " + photoID, null, null);
				
				if (c.moveToFirst() == true) {
		            try {
		            	ByteArrayInputStream rawPhotoStream = new ByteArrayInputStream(c.getBlob(c.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)));
		            	Bitmap contactPhoto = BitmapFactory.decodeStream(rawPhotoStream);
		            	c.close();
		            	return contactPhoto;
		            }
		            catch (NullPointerException ex) {
		            	c.close();
		            	return null;
		            }
		        }
			}
			
			c.close();
			return null;
		}
		catch(java.lang.IllegalStateException e) {
			return null;
		}

	}
	
	public static int getUnreadSmsCount(Context context) {

		int count = 0;
		try {
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
		}
		catch (java.lang.IllegalStateException e) {
			Log.d(MetaWatch.TAG, "Failed to query SMS content provider");
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
		Log.d(MetaWatch.TAG, "Utils.getUnreadGmailCount(): account='"+account+"' label='"+label+"'");
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
							int count = Integer.parseInt(c.getString(i));
							Log.d(MetaWatch.TAG,
									"Utils.getUnreadGmailCount(): found count, returning " + count);
							return count;
						}
					}

				c.moveToNext();

				if (c.isLast()) {
					break;
				}
			}
		} catch (Exception x) {
			Log.d(MetaWatch.TAG, "Utils.getUnreadGmailCount(): caught exception: " + x.toString());
		}

		Log.d(MetaWatch.TAG, "Utils.getUnreadGmailCount(): couldn't find count, returning 0.");
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
	
	static final Uri k9AccountsUri = Uri.parse("content://com.fsck.k9.messageprovider/accounts/");
	static final String k9UnreadUri = "content://com.fsck.k9.messageprovider/account_unread/";
	
	private static int k9UnreadCount = 0;	
	private static long k9LastRefresh = 0;
	
	public static int getUnreadK9Count(Context context) {
		long time = System.currentTimeMillis();
		if(time - k9LastRefresh > 1*60*1000)
			refreshUnreadK9Count(context);
		
		return k9UnreadCount;
	}
	
	private static int getUnreadK9Count(Context context, int accountNumber) {
		try {
			Cursor cur = context.getContentResolver().query(Uri.parse(k9UnreadUri+"/"+accountNumber+"/"), null, null, null, null);
		    if (cur!=null) {
		    	Log.d(MetaWatch.TAG, "k9: "+cur.getCount()+ " unread rows returned");
		    			    	
		    	if (cur.getCount()>0) {
			    	cur.moveToFirst();
			    	int unread = 0;
			    	int nameIndex = cur.getColumnIndex("accountName");
			    	int unreadIndex = cur.getColumnIndex("unread");
			    	do {
			    		String acct = cur.getString(nameIndex);
			    		int unreadForAcct = cur.getInt(unreadIndex);
			    		Log.d(MetaWatch.TAG, "k9: "+acct+" - "+unreadForAcct+" unread");
			    		unread += unreadForAcct;
			    	} while (cur.moveToNext());
				    cur.close();
				    return unread;
		    	}
		    }
		    else {
		    	Log.d(MetaWatch.TAG, "Failed to query k9 unread contentprovider.");
		    }
		}
		catch (IllegalStateException e) {
			Log.d(MetaWatch.TAG, "k-9 unread uri unknown.");
		}
		return 0;
	}
	
	public static void refreshUnreadK9Count(Context context) {		
		int accounts = getK9AccountCount(context);
		if (accounts>0) {
			int count = 0;
			for (int acct=0; acct<accounts; ++acct) {
				count += getUnreadK9Count(context, acct);
			}
			k9UnreadCount = count;
			k9LastRefresh = System.currentTimeMillis();
		}	
	}
	
	public static int getK9AccountCount(Context context) {
		try {
			Cursor cur = context.getContentResolver().query(k9AccountsUri, null, null, null, null);
		    if (cur!=null) {
		    	Log.d(MetaWatch.TAG, "k9: "+cur.getCount()+ " account rows returned");

		    	int count = cur.getCount();
		    	
		    	cur.close();
		    	
		    	return count;
		    }
		    else {
		    	Log.d(MetaWatch.TAG, "Failed to query k9 unread contentprovider.");
		    }
		}
		catch (IllegalStateException e) {
			Log.d(MetaWatch.TAG, "k-9 accounts uri unknown.");
		}
		catch (java.lang.SecurityException e) {
			Log.d(MetaWatch.TAG, "Permissions failure accessing k-9 databases");
		}
		return 0;

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
	
	public static Bitmap ditherTo1bit(Bitmap input, boolean inverted) {
	
		Bitmap output = input.copy(Config.RGB_565, true);
		
		double[][] pixels = new double[input.getWidth()][input.getHeight()];
		
		final int w=input.getWidth();
		final int h=input.getHeight();
		
		for(int y=0; y<h; ++y) {
		   for(int x=0; x<w; ++x) {
			   int col = input.getPixel(x, y);
			   
			   double R = ((col >> 16) & 0xff)/256.0; 
		       double G = ((col >> 8) & 0xff)/256.0;
		       double B = (col & 0xff)/256.0;
		        
		       pixels[x][y] = ( (0.3*R) + (0.59*G) + (0.11*B) );		        
		   }
		}
		
		for(int y=0; y<h; ++y) {
			   for(int x=0; x<w; ++x) {
				   double oldpixel = pixels[x][y];
				   double newpixel = oldpixel<0.5 ? 0 : 1;
				   	   
				   pixels[x][y] = newpixel;
				   double quant_error = oldpixel - newpixel;
				   if(x<w-1)
					   pixels[x+1][y] += 7.0/16.0 * quant_error;
				   if(x>0 && y<h-1)
					   pixels[x-1][y+1] += 3.0/16.0 * quant_error;
				   if(y<h-1)
					   pixels[x][y+1] += 5.0/16.0 * quant_error;
				   if(x<w-1 && y<h-1)
					   pixels[x+1][y+1] += 1.0/16.0 * quant_error;
			
				   int col = 0;
				   if (inverted)
					   col = newpixel > 0.5 ? 0xff000000 : 0xffffffff;   
				   else
					   col = newpixel > 0.5 ? 0xffffffff : 0xff000000;
				   output.setPixel(x, y, col);
			   
			   }
		}

		return output;
	}
	
	public static Bitmap resize(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}
	
	public static void dumpBitmapToSdCard(Bitmap bitmap) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("/sdcard/test.png");
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
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
	
	public static String ReadInputStream(InputStream in) throws IOException {
		StringBuffer stream = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
		stream.append(new String(b, 0, n));
		}
		return stream.toString();
		}
	

}
