package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/*
 * Get all medications beginning with the string that the user has typed in on the 
 * Medication String
 */
public class GetAllMedications {
	Activity CurrentActivity;
	AutoCompleteTextView autoCompleteTextView;
	String params = null;
	
	public String[] getMedsFromServer(String urlBase, CharSequence searchString, Activity currentActivity,
			AutoCompleteTextView acTextView){
		CurrentActivity = currentActivity;
		autoCompleteTextView = acTextView;
		
		String searchUrl = Utils.urlBase + "/autoComplete.php?searchValue=";
		//String completeSearchUrl = searchUrl + "?searchValue=" + searchString;
		String params =  "" + searchString;
		String encodedParams = null;
        try {
            encodedParams = URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
		}

		String completeSearchUrl = searchUrl + encodedParams;
		new RequestTask().execute(completeSearchUrl);
		return null;		
		
	}
	
	class RequestTask extends AsyncTask<String, String, String>{
		
		@Override
		protected String doInBackground(String... uri){
			//Do "AJAX" here
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = null;
			
			try {
				HttpUriRequest request = (HttpUriRequest) new HttpGet(uri[0]);
				response = httpclient.execute(request);
			} catch (ClientProtocolException e) {
                return "Error in Client protocol executing request is " + e.getMessage();
			} catch (IOException e) {
                return "Error in IO executing request is " + e.getMessage();
			}
		    StatusLine statusLine = response.getStatusLine();
		    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        try {
					response.getEntity().writeTo(out);
				} catch (IOException e) {
                    return "Error in IO writing response " + e.getMessage();
				}
		        try {
					out.close();
				} catch (IOException e) {
                    return "Error closing output stream: " + e.getMessage();
				}
		        String responseString = out.toString();
		        return responseString;
		        
		    	} else{
		        //Closes the connection.
		        try {
					response.getEntity().getContent().close();
				} catch (Exception e) {
                    return "Error closing connection is " + e.getMessage();
				}
		        try {
					throw new IOException(statusLine.getReasonPhrase());
				} catch (IOException e) {
                    return "Error closing connection is " + e.getMessage();
				}
		    }
		}
		
	    @Override
	    protected void onPostExecute(String result) {
	        super.onPostExecute(result);
            if (result.startsWith("Error")){
                new Alert(result);
                return;
            }
			int i = 1;
			if (result.length() == 1) return;
			if (result.endsWith("\n")){
				result = result.substring(0, result.length()-2);
			}
			result = result.toLowerCase();
	        String[] medArray = result.split(",");
	        ArrayAdapter<String> foundMedicationListAdapter =  
	        		new ArrayAdapter<String>(CurrentActivity, R.layout.med_autocomplete_listitem, medArray);
	        autoCompleteTextView.setAdapter(foundMedicationListAdapter);
	        foundMedicationListAdapter.notifyDataSetChanged();
	        
	        


	    }
	}	

}
