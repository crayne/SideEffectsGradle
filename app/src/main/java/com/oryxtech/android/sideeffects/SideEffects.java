package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class SideEffects extends Activity {
	Activity CurrentActivity;
    ArrayList<String> myMedicationListItems;
    ArrayAdapter<String> myMedicationListAdapter;
    ArrayAdapter<String> allMedicationListAdapter;
    AutoCompleteTextView autoCompleteTextView;
    ListView myMedicationListView;
    MedListStorage medListStorage = null;
    
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentActivity = this;
        Utils.CurrentActivity = this;
        medListStorage = new MedListStorage(CurrentActivity);

        setContentView(R.layout.activity_sideeffects);
     // Get a reference to the AutoCompleteTextView in the layout
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_medications);
        // Get the string array
        String[] countries = getResources().getStringArray(R.array.countries_array);
        // Create the adapter and set it to the AutoCompleteTextView
        allMedicationListAdapter = 
                new ArrayAdapter<String>(this, R.layout.med_autocomplete_listitem, countries);
        autoCompleteTextView.setAdapter(allMedicationListAdapter);
        autoCompleteTextView.setOnItemClickListener(new allMedicationsAutoCompleteClickListener());
        autoCompleteTextView.setThreshold(2);  
          
        autoCompleteTextView.addTextChangedListener(autoCompleteTextChecker); 
        
        //Event listener for the autocomplete text view delete button
		Button button = (Button) this.findViewById(R.id.autocomplete_medications_delete_button);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
			    autoCompleteTextView.setText("");
				return false;
			}
		  });       
        
        myMedicationListItems=new ArrayList<String>(); 
		myMedicationListView = (ListView) this.findViewById(R.id.medication_list);
        myMedicationListAdapter=new MedListArrayAdapter(this, myMedicationListItems, medListStorage, CurrentActivity, myMedicationListView);
		myMedicationListView.setAdapter(myMedicationListAdapter);
		medListStorage.restoreMedList(myMedicationListItems, myMedicationListAdapter, myMedicationListView);
		Button symptomScreenButton =  (Button)CurrentActivity.findViewById(R.id.symptom_screen_button_id);
		symptomScreenButton.setOnClickListener(symptomScreenButtonClickListener);

		Button interactionsButton = (Button)CurrentActivity.findViewById(R.id.interactions_button_id);
		interactionsButton.setOnClickListener(interactionsButtonClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sideeffects, menu);
          
        return true;
    }
    
    public class allMedicationsAutoCompleteClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
	        String selection = (String)parent.getItemAtPosition(pos);
	        myMedicationListItems.add(selection);
	        medListStorage.addMed(selection);
	        myMedicationListAdapter.notifyDataSetChanged();
            int maxRows = myMedicationListAdapter.getCount();
            if (maxRows > Utils.maxMedlistRows) maxRows = Utils.maxMedlistRows;
	        Utils.setListViewHeight(myMedicationListView, maxRows);
	        //Start activity that lists all symptoms and frequencies for the medication name in "selection"
	    	Intent intent = new Intent();
			intent.setClassName("com.oryxtech.android.sideeffects",
					"com.oryxtech.android.sideeffects.SymptomsForMedicationScreen");
			//key-value pair, where key needs current package prefix
			intent.putExtra("com.oryx.allaboard.CurrentMedication", selection); 
			CurrentActivity.startActivity(intent);

		}

	} 
    
	 private OnClickListener symptomScreenButtonClickListener = new OnClickListener() {
		    public void onClick(View v) {
		    	//If there are no medications, do nothing
		    	int numMeds = myMedicationListAdapter.getCount();
		    	if (numMeds == 0){
		    		new Alert("Please pick one or more medications before searching by symptom");
		    		return;
		    	}
		    	Intent intent = new Intent();
				intent.setClassName("com.oryxtech.android.sideeffects",
						"com.oryxtech.android.sideeffects.SymptomScreen");
				//key-value pair, where key needs current package prefix
				String[] myMedicationListArray = myMedicationListItems.toArray(new String[myMedicationListItems.size()]);
				intent.putExtra("com.oryx.allaboard.MedicationArray", myMedicationListArray); 
				CurrentActivity.startActivity(intent);
		    }
 	};

	private OnClickListener interactionsButtonClickListener = new OnClickListener() {
		public void onClick(View v) {
			//new Alert("In interactionsButtonClickListener");
			//If there are no medications, do nothing
			int numMeds = myMedicationListAdapter.getCount();
			if (numMeds == 0){
				new Alert("Please pick one or more medications before searching by symptom");
				return;
			}
			Intent intent = new Intent();
			intent.setClassName("com.oryxtech.android.sideeffects",
					"com.oryxtech.android.sideeffects.Interactions");
			//key-value pair, where key needs current package prefix
			String[] myMedicationListArray = myMedicationListItems.toArray(new String[myMedicationListItems.size()]);
			intent.putExtra("com.oryxtech.android.sideeffects.MedicationArray", myMedicationListArray);
			CurrentActivity.startActivity(intent);
		}
	};

	final TextWatcher autoCompleteTextChecker = new TextWatcher() {
        public void afterTextChanged(Editable s) {}  
      
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}  
      
        public void onTextChanged(CharSequence s, int start, int before, int count)  
        {  
        	if (s.length() < 3) return;
        	//allMedicationListAdapter.clear(); 
        	//Toast.makeText(CurrentActivity, "In TextWatcher.onTextChanged, CharacterSequence is: " + s, Toast.LENGTH_SHORT).show();
                  
            //Get list of medications from server 
        	GetAllMedications getAllMeds = new GetAllMedications();
        	String[] allMeds = getAllMeds.getMedsFromServer(Utils.urlBase, s, CurrentActivity, autoCompleteTextView);
                                 
        }       
    };    
}




