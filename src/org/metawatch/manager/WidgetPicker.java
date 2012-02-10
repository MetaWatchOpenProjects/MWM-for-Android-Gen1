package org.metawatch.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.widgets.InternalWidget.WidgetData;
import org.metawatch.manager.widgets.WidgetManager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WidgetPicker extends ListActivity {
	
	private List<WidgetData> widgetList;
	private int groupPosition;
	private int childPosition;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<WidgetData> mWidgets;

        public EfficientAdapter(Context context, List<WidgetData> widgets) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            mWidgets = widgets;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mWidgets.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficient to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.text.setText(mWidgets.get(position).description);
            if(mWidgets.get(position).bitmap!=null)
            	holder.icon.setImageBitmap(mWidgets.get(position).bitmap);

            return convertView;
        }

        static class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }
    
    private static Comparator<WidgetData> COMPARATOR = new Comparator<WidgetData>()
    {
	// This is where the sorting happens.
        public int compare(WidgetData o1, WidgetData o2)
        {
            return o1.id.compareTo(o2.id);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            
        Map<String,WidgetData> widgetMap = WidgetManager.refreshWidgets(null);
        widgetList = new ArrayList<WidgetData>();
        
        WidgetData dummy = new WidgetData();
        dummy.id = "";
        dummy.description = "<empty>";
        
        widgetList.add(dummy);
        
        for (Map.Entry<String,WidgetData> e : widgetMap.entrySet())
        	widgetList.add(e.getValue());
        
        Collections.sort(widgetList, COMPARATOR);
        
        Log.d(MetaWatch.TAG, "Showing " +widgetList.size() + " widgets");
        
    	groupPosition = getIntent().getIntExtra("groupPosition", -1);
    	childPosition = getIntent().getIntExtra("childPosition", -1);
        
        setListAdapter(new EfficientAdapter(this, widgetList));
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent result = new Intent();
    	
    	result.putExtra("selectedWidget", widgetList.get(position).id);
    	result.putExtra("groupPosition", groupPosition);
    	result.putExtra("childPosition", childPosition);
    	
    	setResult(Activity.RESULT_OK, result);

    	super.onListItemClick(l, v, position, id);
    	
    	finish();
    }

}
