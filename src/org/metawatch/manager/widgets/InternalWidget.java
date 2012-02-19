package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

public interface InternalWidget {
	
	public class WidgetData {
		public String id;
		public String description;
		
		public int width;
		public int height;
		
		public Bitmap bitmap;
		
		public int priority;
	}
	
	public void init(Context context, ArrayList<CharSequence> widgetIds);
	public void shutdown();
	
	public void refresh(ArrayList<CharSequence> widgetIds);
	public void get(ArrayList<CharSequence> widgetIds, Map<String,WidgetData> result);
	
}
