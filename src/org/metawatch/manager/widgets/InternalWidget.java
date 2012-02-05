package org.metawatch.manager.widgets;

import java.util.Dictionary;
import java.util.List;

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
	
	public void init(List<String> widgetIds);
	public void shutdown();
	
	public void refresh(List<String> widgetIds);
	public void get(List<String> widgetIds, Dictionary<String,WidgetData> result);
	
}
