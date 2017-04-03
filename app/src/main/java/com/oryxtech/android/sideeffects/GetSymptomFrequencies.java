package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class GetSymptomFrequencies {
	Activity CurrentActivity;
	AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> symptomFrequencyListAdapter;
    String gSymptom;
    String gMedication;
    int gNumMeds;
    ListView symptomFrequencyListView;


	
	public void getSymptomFrequenciesFromServer(String urlBase, CharSequence symptom, Activity currentActivity,
			AutoCompleteTextView acTextView, String medication, ArrayAdapter<String> symptomFreqListAdapter,
			ProgressBar progressBar, int count, ListView sFListView){
		symptomFrequencyListAdapter = symptomFreqListAdapter;
        symptomFrequencyListView = sFListView;
		gSymptom = (String)symptom;
		gMedication = medication;
        gNumMeds = count;
				
		CurrentActivity = currentActivity;
		autoCompleteTextView = acTextView;
        String serverPgm = "searchSideEffect.php";
        if (Utils.freqFormat.equals("verbal")) serverPgm = "searchSideEffectVerbal.php";
		String searchUrl = Utils.urlBase + "/" + serverPgm + "?";
		//String completeSearchUrl = searchUrl + "?searchValue=" + searchString;
		String uMedication = null;
		String uSymptom = null;
        try {
            uMedication = URLEncoder.encode(medication, "UTF-8");
            uSymptom = URLEncoder.encode((String)symptom, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
		}
		String params =  "" + "symptom=" + uSymptom + "&medication=" + uMedication;

		String completeSearchUrl = searchUrl + params;
		new SymptomFrequencyRequestTask(progressBar).execute(completeSearchUrl);

		
	}
	
	class SymptomFrequencyRequestTask extends HttpRequestTask{
		ProgressBar progressBar;
		
		SymptomFrequencyRequestTask(ProgressBar progressBarParam){
			progressBar = progressBarParam;
		}
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
	        progressBar.setVisibility(View.VISIBLE);

		}
				
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
            if (result.startsWith("Error")){
                new Alert(result);
                return;
            }
			result = result.toLowerCase();
            if (Utils.freqFormat.equals("verbal")){
                symptomFrequencyListView.setVisibility(View.INVISIBLE);
            }
			symptomFrequencyListAdapter.add(result);
			symptomFrequencyListAdapter.notifyDataSetChanged();
			progressBar.setVisibility(View.INVISIBLE);
            String itemPieces[] = new String[2];
            int count = symptomFrequencyListAdapter.getCount();
            if (count == gNumMeds && Utils.freqFormat.equals("verbal")){
                String[] freqArray = new String[count];
                //Reverse each line so that lines can be sorted by frequency
                for (int i=0; i<count; i++){
                    String item = symptomFrequencyListAdapter.getItem(i);
                    //Change from "Medication: frequency" to "Frequency: medication"
                    if (item.endsWith(": "))item += "xnotfound";
                    itemPieces= item.split(": ");
                    if (itemPieces[1].equals( "found, no frequency information available") ) {
                        itemPieces[1] = "xfound";
                    }
                    else if (itemPieces[1].equals("side effect not found") || itemPieces[1] == null){
                        itemPieces[1] = "xnotfound";
                    }
                    String itemReverse = itemPieces[1] + " " + itemPieces[0];
                    freqArray[i] = itemReverse;

                }
                Arrays.sort(freqArray);
                symptomFrequencyListAdapter.clear();
                for (int i=0; i<count; i++){
                    String item  = freqArray[i];
                    itemPieces = item.split(" ");
                    if (itemPieces[0].equals("xfound")) itemPieces[0] = "found, no frequency information available";
                    if (itemPieces[0].equals("xnotfound")) itemPieces[0] = "not found";
                    String itemReverse = itemPieces[1] + ": " + itemPieces[0];
                    symptomFrequencyListAdapter.add(itemReverse);
                    symptomFrequencyListAdapter.notifyDataSetChanged();

                }
                symptomFrequencyListView.setVisibility(View.VISIBLE);

            }

	    }
	}	

}
