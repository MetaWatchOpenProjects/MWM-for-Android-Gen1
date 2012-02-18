package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.widgets.WidgetRow;
import org.metawatch.manager.widgets.InternalWidget.WidgetData;
import org.metawatch.manager.widgets.GmailWidget;
import org.metawatch.manager.widgets.K9Widget;
import org.metawatch.manager.widgets.MissedCallsWidget;
import org.metawatch.manager.widgets.SmsWidget;
//import org.metawatch.manager.widgets.TestWidget;
import org.metawatch.manager.widgets.WeatherWidget;
import org.metawatch.manager.widgets.CalendarWidget;

import android.content.Context;

public class WidgetManager {
	static List<InternalWidget> widgets = new ArrayList<InternalWidget>();
	static Map<String,WidgetData> dataCache;
	
	public static void initWidgets(Context context, List<String> widgetsDesired) {
		
		if(widgets.size()==0) {
			widgets.add(new MissedCallsWidget());
			widgets.add(new SmsWidget());
			widgets.add(new K9Widget());
			widgets.add(new GmailWidget());
			widgets.add(new WeatherWidget());
			widgets.add(new CalendarWidget());
			//widgets.add(new TestWidget());
		}
		
		for(InternalWidget widget : widgets) {
			widget.init(context, widgetsDesired);
		}
	}
	
	public static Map<String,WidgetData> refreshWidgets(List<String> widgetsDesired) {
		if(dataCache==null)
			dataCache = new HashMap<String,WidgetData>();
		
		for(InternalWidget widget : widgets) {
			widget.refresh(widgetsDesired);
			widget.get(widgetsDesired, dataCache);
		}
		
		return dataCache;
	}
	
	public static Map<String,WidgetData> getCachedWidgets(List<String> widgetsDesired) {
		if(dataCache==null)
			return refreshWidgets(widgetsDesired);
		
		return dataCache;
	}

	
	
	
	public static List<WidgetRow> getDesiredWidgetsFromPrefs() {
		
		String[] rows = Preferences.widgets.split("\\|");
		
		List<WidgetRow> result = new ArrayList<WidgetRow>();
		
		for(String line : rows) {
			WidgetRow row = new WidgetRow();
			String[] widgets = line.split(",");
			for(String widget : widgets) {
				row.add(widget);
			}
			result.add(row);
		}
		
		return result;
	}
}
