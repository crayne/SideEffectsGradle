package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MedListArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final ArrayList<String> values;
	private final MedListArrayAdapter arrayAdapter;
	private final Activity CurrentActivity;
	private final ListView listView;
    private int fPosition;
 
	public MedListArrayAdapter(Context context, ArrayList<String> myMedicationListItems, MedListStorage mListStorage, Activity currentActivity, ListView myMedicationListView) {
		super(context, R.layout.my_medication_listitem, myMedicationListItems);
		this.context = context;
		this.values = myMedicationListItems;
		this.arrayAdapter = this;
		this.CurrentActivity = currentActivity;
		this.listView = myMedicationListView;

	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		fPosition = position;
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.my_medication_listitem, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.medname);
		//ImageView imageView = (ImageView) rowView.findViewById(R.id.logo); do name for pseudoButton
        String medName = (String) values.get(position);
		textView.setText(medName);
		//set pseudobutton value here
		Button button = (Button) rowView.findViewById(R.id.medbutton);
	
		//fPosition is wrong here -- need to get the correct item to delete
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() != MotionEvent.ACTION_UP) return true;
				//Get the correct position of the row to delete
				int index = getIndexForRow(arg0);
			    values.remove(index);
			    arrayAdapter.notifyDataSetChanged();
			    MedListStorage medListStorage = new MedListStorage(CurrentActivity); 
			    medListStorage.deleteMed(arrayAdapter, listView);
				return true;
			}
		  });

  		textView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() != MotionEvent.ACTION_UP) return true;
				//Get the correct position of the row to delete
				int index = getIndexForRow(arg0);
				String medName = (String) values.get(index);
				//Start activity that lists all symptoms and frequencies for the medication name in "selection"
				Intent intent = new Intent();
				intent.setClassName("com.oryxtech.android.sideeffects",
						"com.oryxtech.android.sideeffects.SymptomsForMedicationScreen");
				//key-value pair, where key needs current package prefix
				intent.putExtra("com.oryx.allaboard.CurrentMedication", medName);
				CurrentActivity.startActivity(intent);
				return true;
			}
		});
        return rowView;
	}

	private int getIndexForRow(View deleteButton){
		ViewGroup rowViewGroup = (ViewGroup) deleteButton.getParent();
		int count = rowViewGroup.getChildCount();
		TextView textView = null;
		View view = rowViewGroup.getChildAt(0);
		if (view instanceof TextView) {
			textView = (TextView) view;
		}
		String rowText = (String)textView.getText();
		//Now find the position of the row with this text
		for (int i = 0; i<values.size(); i++){
			String medName = (String) values.get(i);
			if (medName.equals(rowText)){
				return i;
			}
		}
		return -1;
	}
}
