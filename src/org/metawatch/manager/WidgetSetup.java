package org.metawatch.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;

public class WidgetSetup extends Activity {
	
	private ExpandableListView widgetList;
	private ExpandableListAdapter mAdapter;
	
    private static final String NAME = "NAME";
    private static final String ID = "ID";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.widget_setup);
   
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		
		//Button button = (Button) findViewById(R.id.test);
		//button.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //    	startActivityForResult(new Intent(getApplicationContext(), WidgetPicker.class), 1);
        //    }
        //});
		
		widgetList = (ExpandableListView) findViewById(R.id.widgetList);

		
		widgetList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				startActivityForResult(new Intent(getApplicationContext(), WidgetPicker.class), 1);
				return false;
			}
		});
		
		
		List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
	    List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
	    for (int i = 0; i < 2; i++) {
	    	Map<String, String> curGroupMap = new HashMap<String, String>();
	        groupData.add(curGroupMap);
	        curGroupMap.put(NAME, "Row " + (i+1));
	        curGroupMap.put(ID, "Id");
	            
	        List<Map<String, String>> children = new ArrayList<Map<String, String>>();
	        for (int j = 0; j < 3; j++) {
	        	Map<String, String> curChildMap = new HashMap<String, String>();
	            children.add(curChildMap);
	            curChildMap.put(NAME, "<empty>");
	            curChildMap.put(ID, "");
	        }
	        childData.add(children);
	    }
	        
	    // Set up our adapter
	    mAdapter = new SimpleExpandableListAdapter(
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
	    widgetList.setAdapter(mAdapter);
		
		refreshPreview();
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {      	  
        	String id = (data.getStringExtra("selected_widget"));
        }
    }
    
    private void refreshPreview() {
    	ImageView v = (ImageView) findViewById(R.id.idlePreview);
    	v.setImageBitmap(Idle.createLcdIdle(this, 0));
    }
}
