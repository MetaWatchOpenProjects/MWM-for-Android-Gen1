package org.metawatch.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.widgets.WidgetManager;
import org.metawatch.manager.widgets.InternalWidget.WidgetData;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

public class WidgetSetup extends Activity {
	
	private ExpandableListView widgetList;
	private SimpleExpandableListAdapter adapter;
	
	private List<Map<String, String>> groupData;
	private List<List<Map<String, String>>> childData;
	
	private Map<String,WidgetData> widgetMap;
	
    private static final String NAME = "NAME";
    private static final String ID = "ID";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.widget_setup);
   
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        setContentView(R.layout.widget_setup);
        adapter = null;
        onStart();
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		
		if(adapter!=null)
			return;
		
		widgetMap = WidgetManager.getCachedWidgets(null);
			
		widgetList = (ExpandableListView) findViewById(R.id.widgetList);		
		widgetList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent i = new Intent(getApplicationContext(), WidgetPicker.class);
				i.putExtra("groupPosition", groupPosition);
				i.putExtra("childPosition", childPosition);
				startActivityForResult(i,  1);
				return false;
			}
		});
				
		groupData = new ArrayList<Map<String, String>>();
	    childData = new ArrayList<List<Map<String, String>>>();

		String[] rows = Preferences.widgets.split("\\|");
			
		int i=1;
		for(String line : rows) {
	    	Map<String, String> curGroupMap = new HashMap<String, String>();
	        groupData.add(curGroupMap);
	        curGroupMap.put(NAME, "Row " + (i++));
	        curGroupMap.put(ID, "Id");
	        
	        List<Map<String, String>> children = new ArrayList<Map<String, String>>();
	        
			String[] widgets = line.split(",");
			for(String widget : widgets) {
	        	Map<String, String> curChildMap = new HashMap<String, String>();
	            children.add(curChildMap);
	            String name = widget;
	            if(widgetMap.containsKey(widget))
	            	name = widgetMap.get(widget).description;
	            curChildMap.put(NAME, name);
	            curChildMap.put(ID, widget);
	        }
			
			while(children.size()<5) {
	        	Map<String, String> curChildMap = new HashMap<String, String>();
	            children.add(curChildMap);
	            curChildMap.put(NAME, "<empty>");
	            curChildMap.put(ID, "");
			}
			
	        childData.add(children);
		}	    
	            
	    // Set up our adapter
		adapter = new SimpleExpandableListAdapter(
			this,
			groupData,
			android.R.layout.simple_expandable_list_item_1,
			new String[] { NAME, ID },
			new int[] { android.R.id.text1, android.R.id.text2 },
			childData,
			android.R.layout.simple_expandable_list_item_2,
			new String[] { NAME, ID },
			new int[] { android.R.id.text1, android.R.id.text2 }
		);
	    widgetList.setAdapter(adapter);
		
		refreshPreview();
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

		widgetMap = WidgetManager.getCachedWidgets(null);
    	
        if (resultCode == Activity.RESULT_OK) {      	  
        	String id = data.getStringExtra("selectedWidget");
        	int groupPosition = data.getIntExtra("groupPosition", -1);
        	int childPosition = data.getIntExtra("childPosition", -1);
        	
        	if(groupPosition>-1 && childPosition>-1) {
        		Map<String,String> curChildMap = childData.get(groupPosition).get(childPosition);
        		if(id==null) {
    	            curChildMap.put(NAME, "<empty>");
    	            curChildMap.put(ID, "");
        		}
        		else {
    	            String name = id;
    	            if(widgetMap.containsKey(id))
    	            	name = widgetMap.get(id).description;
    	            curChildMap.put(NAME, name);
		            curChildMap.put(ID, id);
        		}
        	}
        	adapter.notifyDataSetChanged();
        	storeWidgetLayout();
        	refreshPreview();
        	Idle.updateLcdIdle(this);
        }
    }
    
    private void refreshPreview() {
    	ImageView v = (ImageView) findViewById(R.id.idlePreview);
    	v.setImageBitmap(Idle.createLcdIdle(this, 0));
    }
    
    private void storeWidgetLayout() {
    	
    	String out = "";
    	for(List<Map<String, String>> row : childData) {
    		if(out.length()>0)
    			out+="|";
    		
    		String line = "";
    		for(Map<String, String> child : row) {
        		String id = child.get(ID);	
        		if(id!="") {
	        		if(line.length()>0)
	        			line+=",";
	        		
	        		line+=id;
        		}
    		}
    		
    		out+=line;
    	}
    	
    	Preferences.widgets = out;
    	MetaWatchService.saveWidgets(this, out);
    }
}
