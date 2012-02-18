                                                                     
                                                                     
                                                                     
                                             
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
  * ImageViewer.java                                                          *
  * ImageViewer                                                               *
  * System wide "Send to watch" that shows pictures in app mode               *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.Notification.VibratePattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class ImageViewer extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Intent i = getIntent();
        
        Log.d(MetaWatch.TAG, "action: " + i.getAction());                        
        Log.d(MetaWatch.TAG, "data: "+ i.getData().getPath() );
        
        InputStream is;
		try {
			is = getContentResolver().openInputStream(i.getData());
			
	        BitmapFactory.Options options = new BitmapFactory.Options();       
	        Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
	        
	        if (bmp!=null) {
		        Bitmap scaled = Utils.resize(bmp, 96, 96,);
		        Bitmap dithered = Utils.ditherTo1bit(scaled, Preferences.invertLCD);
		        
		        VibratePattern vibratePattern = new VibratePattern(false, 1,1,1);
		        		
		        Notification.addBitmapNotification(this, dithered, vibratePattern, -1);
	        }
        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        finish();
	}
	
	

}
