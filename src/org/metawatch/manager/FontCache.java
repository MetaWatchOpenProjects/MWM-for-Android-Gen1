package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.Context;
import android.graphics.Typeface;

public class FontCache {
	
	public class FontInfo {
		
		public FontInfo(Context context, String assetPath, int size, int realSize) {
			face = Typeface.createFromAsset(context.getAssets(), assetPath);
			this.size = size;
			this.realSize = realSize;
		}
		
		public Typeface face;
		public int size;
		public int realSize;
	}
	
	public enum FontSize {
		SMALL,
		MEDIUM,
		LARGE 
	}
	
	public static FontInfo Small = null;
	public static FontInfo Medium = null;
	public static FontInfo Large = null;
	
	private static FontCache instance = new FontCache();
	
	public static void Initialize(Context context) {
		Small = instance.new FontInfo(context, "metawatch_8pt_5pxl_CAPS.ttf", 8, 5);
		Medium = instance.new FontInfo(context, "metawatch_8pt_7pxl_CAPS.ttf", 8, 7);
		Large = instance.new FontInfo(context,  "metawatch_16pt_11pxl.ttf", 16, 11);
	}
	
	public static FontInfo Get(FontSize size) {
		switch(size) {
		case SMALL:
			return Small;
			
		case MEDIUM:
			return Medium;
			
		case LARGE:
			return Large;
		}
		
		return null;
	}
	
	public static FontInfo Get(int size) {
		switch(size) {
		case 1:
			return Small;
			
		case 2:
			return Medium;
			
		case 3:
			return Large;
		}
		
		return null;
	}
	
	public static FontInfo Get() {
		return Get(Preferences.fontSize);
	}
	
	
}
