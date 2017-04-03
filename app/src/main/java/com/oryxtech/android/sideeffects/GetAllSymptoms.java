package com.oryxtech.android.sideeffects;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
/*
 * Get symptoms for current screen in AutocompleteTextView in Symptom Search screen
 */
public class GetAllSymptoms {
	Activity CurrentActivity;
	AutoCompleteTextView autoCompleteTextView;

	public String[] getSymptomsFromServer(String urlBase, CharSequence searchString, Activity currentActivity,
			AutoCompleteTextView acTextView){
		CurrentActivity = currentActivity;
		autoCompleteTextView = acTextView;
		
		String searchUrl = Utils.urlBase + "/autoCompleteSymptoms.php?searchValue=";
		String params =  "" + searchString;
		String encodedParams = null;
        try {
            encodedParams = URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
		}

		String completeSearchUrl = searchUrl + encodedParams;
		new SymptomAutoCompleteRequestTask().execute(completeSearchUrl);
		return null;		
		
	}
	
	class SymptomAutoCompleteRequestTask extends HttpRequestTask{
				
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
            if (result.startsWith("Error")){
                new Alert(result);
                return;
            }
			if (result.length() == 1) return;
			if (result.endsWith("\n")){
				result = result.substring(0, result.length()-2);
			}
			result = result.toLowerCase();
	        String[] medArray = result.split(",");
	        ArrayAdapter<String> foundMedicationListAdapter =  
	        		new ArrayAdapter<String>(CurrentActivity, R.layout.symptom_autocomplete_listitem, medArray);
	        autoCompleteTextView.setAdapter(foundMedicationListAdapter);
	        foundMedicationListAdapter.notifyDataSetChanged();	        	        
	    }
	}	

}
