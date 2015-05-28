package com.fhtrier.voiceDiary;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.holoeverywhere.ArrayAdapter;
import org.holoeverywhere.widget.ListView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleAdapter;

public class RecordingDiary extends AlertDialog {

	public final static String ITEM_TITLE = "title";  
	public final static String ITEM_CAPTION = "caption";
	Context context;
	
	public Map<String,?> createItem(String title, String caption) {  
        Map<String,String> item = new HashMap<String,String>();  
        item.put(ITEM_TITLE, title);  
        item.put(ITEM_CAPTION, caption);  
        return item;  
    }  
	
	public SeparatedListAdapter fillAdapter(Map<String,String[]> dates, SeparatedListAdapter adapter)
	{
		Set keys = dates.keySet(); 
		for (Iterator i = keys.iterator(); i.hasNext();)
		{
			String   key   = (String) i.next();
		    String[] value = (String[]) dates.get(key);
		    adapter.addSection(key, new ArrayAdapter<String>(this.getContext(), R.layout.list_item, value));
		}
		return adapter;
	}
	
	public RecordingDiary(Context context) throws ParseException {
		super(context);
		setTitle(R.string.recordingDiary);
		
		/*List<Map<String,?>> security = new LinkedList<Map<String,?>>();  
		security.add(createItem("Remember passwords", "Save usernames and passwords for Web sites"));  
		security.add(createItem("Clear passwords", "Save usernames and passwords for Web sites"));  
		security.add(createItem("Show security warnings", "Show warning if there is a problem with a site's security"));  */

		// create our list and custom adapter  
		SeparatedListAdapter adapter = fillAdapter(MyApplication.getRecDates(), new SeparatedListAdapter(this.getContext()));  
		//adapter.addSection("Array test", new ArrayAdapter<String>(this.getContext(), R.layout.list_item, new String[] { "First item", "Item two" }));  
		//adapter.addSection("Security", new SimpleAdapter(this.getContext(), security, R.layout.list_complex, new String[] { ITEM_TITLE, ITEM_CAPTION }, new int[] { R.id.list_complex_title, R.id.list_complex_caption }));  
		
		setButton(AlertDialog.BUTTON_NEGATIVE,context.getString(R.string.button_cancel) , (new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// this will never be called
			}
		}));
		
		ListView list = new ListView(this.getContext());  
		list.setAdapter(adapter);  
		this.setView(list);   
		return;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dismiss();
			}
		});
	}
}
