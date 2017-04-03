package com.oryxtech.android.sideeffects;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SymptomScreen extends Activity {
	Activity CurrentActivity;
    ArrayList<String> symptomFrequencyListItems;
    ArrayAdapter<String> symptomFrequencyListAdapter;
    ArrayAdapter<String> symptomListAdapter;
    AutoCompleteTextView autoCompleteTextView;
    ListView symptomFrequencyListView;
    String[] medicationArray;
    String currentSymptom;
    
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrentActivity = this;
        Utils.CurrentActivity = this;

        setContentView(R.layout.activity_symptoms);
        Intent intent = getIntent();
        medicationArray = intent.getStringArrayExtra("com.oryx.allaboard.MedicationArray");

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        Button mailButton = (Button) this.findViewById(R.id.send_to_doctor);
        mailButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                sendSearchResultsEmail();
                return true;
            }
        });


     // Get a reference to the AutoCompleteTextView in the layout
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autocomplete_symptoms);
        // Get the string array
        String[] symptoms = {"a","b","c"};
        // Create the adapter and set it to the AutoCompleteTextView 
        symptomListAdapter = 
                new ArrayAdapter<String>(this, R.layout.med_autocomplete_listitem, symptoms);
        autoCompleteTextView.setAdapter(symptomListAdapter);
        autoCompleteTextView.setOnItemClickListener(new symptomAutoCompleteClickListener());
        autoCompleteTextView.setThreshold(2);  
          
        autoCompleteTextView.addTextChangedListener(autoCompleteTextChecker);  
        /*
         * Perhaps creation of the symptomFrequencyListAdapter  should occur in symptomAutoCompleteClickListener, since is 
         * no content for the symptom frequency list until then
         */
        symptomFrequencyListItems=new ArrayList<String>(); 
        symptomFrequencyListAdapter= new ArrayAdapter<String>(this, R.layout.symptom_medication_listitem);
        symptomFrequencyListView = (ListView) this.findViewById(R.id.frequency_medication_list);
        symptomFrequencyListView.setAdapter(symptomFrequencyListAdapter);
		
        //Event listener for the autocomplete text view delete button
		Button button = (Button) this.findViewById(R.id.autocomplete_symptoms_delete_button);
		button.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
			    autoCompleteTextView.setText("");
				return false;
			}
		  });
	    Button returnButton = (Button)findViewById(R.id.back_button);
	    returnButton.setOnClickListener(leavePageListener);
    }

    private void sendSearchResultsEmail(){
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Side effects from my medications:");

        emailIntent.setType("plain/text");
        String intro = "The symptom '"+currentSymptom+"' occurs as a side effect in my medications with the following frequencies:";

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, intro);
        int adapterCount = symptomFrequencyListAdapter.getCount();
        String resultText = intro + "\n\n";
        String item;
        for (int i=0; i<adapterCount; i++) {
            item = symptomFrequencyListAdapter.getItem(i);
            resultText = resultText + item + "\n";
        }

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, resultText);


        startActivity(Intent.createChooser(emailIntent, "Send your email in:"));

    }
    
	private OnClickListener leavePageListener = new OnClickListener() {
	    public void onClick(View v) {
	    	finish();
	    }
	};


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sideeffects, menu);
          
        return true;
    }
    
    public class symptomAutoCompleteClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
	        String symptom = (String)parent.getItemAtPosition(pos);
            currentSymptom = symptom;
			TextView textView = (TextView) CurrentActivity.findViewById(R.id.which_medications);
			textView.setText("The symptom '"+symptom+"' occurs as a side effect in your medications with the following frequencies:");
			textView.setVisibility(TextView.VISIBLE);
	        //This is the new design -- get frequency list as soon as the user chooses a symptom
	        int count = medicationArray.length;
	        symptomFrequencyListView.setVisibility(ListView.VISIBLE);
	        //Close parent?
            symptomFrequencyListAdapter.clear();
	        parent.clearFocus();
	        for (int i=0; i<count; i++){
	        	GetSymptomFrequencies getSymptomFrequency = new GetSymptomFrequencies();
			    ProgressBar progressBar = (ProgressBar)findViewById(R.id.symptoms);

	        	getSymptomFrequency.getSymptomFrequenciesFromServer(Utils.urlBase, symptom, 
	        			CurrentActivity, autoCompleteTextView, medicationArray[i], symptomFrequencyListAdapter, progressBar, count,
                        symptomFrequencyListView);
  	        }
		    autoCompleteTextView.setText("");
		}

	} 
    
  
    
    final TextWatcher autoCompleteTextChecker = new TextWatcher() {  
        public void afterTextChanged(Editable s) {}  
      
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}  
      
        public void onTextChanged(CharSequence s, int start, int before, int count)  
        {  
        	if (s.length() < 3) return;
        	//allMedicationListAdapter.clear(); 
        	//Toast.makeText(CurrentActivity, "In TextWatcher.onTextChanged, CharacterSequence is: " + s, Toast.LENGTH_SHORT).show();
                  
            //Get list of medications from server 
        	GetAllSymptoms getAllSymptoms = new GetAllSymptoms();
        	String[] allSymptoms = getAllSymptoms.getSymptomsFromServer(Utils.urlBase, s, CurrentActivity, autoCompleteTextView);
                                 
        }       
    };    
}




