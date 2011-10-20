                                                                     
                                                                     
                                                                     
                                             
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
  * MediaControl.java                                                         *
  * MediaControl                                                              *
  * Volume control and vanilla Android player control via intents             *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MediaControl {
	
	final static byte VOLUME_UP = 10;
	final static byte VOLUME_DOWN = 11;
	final static byte NEXT = 15;
	final static byte PREVIOUS = 16;
	final static byte TOGGLE = 20;
	
	public static void next(Context context) {
		context.sendBroadcast(new Intent("com.android.music.musicservicecommand.next"));
	}
	
	public static void previous(Context context) {
		context.sendBroadcast(new Intent("com.android.music.musicservicecommand.previous"));
	}
	
	public static void togglePause(Context context) {
		context.sendBroadcast(new Intent("com.android.music.musicservicecommand.togglepause"));
	}
	
	public static void volumeDown(AudioManager audioManager) {
		audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
	}
	
	public static void volumeUp(AudioManager audioManager) {
		audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
	}
}
