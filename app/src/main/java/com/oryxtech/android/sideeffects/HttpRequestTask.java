package com.oryxtech.android.sideeffects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;

public class HttpRequestTask extends AsyncTask<String, String, String>{
    @Override
	protected String doInBackground(String... uri){
		//Do "AJAX" here
 		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;

		try {
			HttpUriRequest request = new HttpGet(uri[0]);
			response = httpclient.execute(request);
		} catch (ClientProtocolException e) {
			//e.printStackTrace();
            return "Error connecting to database";
		} catch (IOException e) {
			e.printStackTrace();
            return "Error connecting to database";
        }
	    StatusLine statusLine = response.getStatusLine();
	    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        try {
				response.getEntity().writeTo(out);
			} catch (IOException e) {
                return "Error connecting to database";
            }
	        try {
				out.close();
			} catch (IOException e) {
                return "Error closing the database";

            }
	        String responseString = out.toString();
	        return responseString;
	        
	    	} else{
	        //Closes the connection.
	        try {
				response.getEntity().getContent().close();
			} catch (IllegalStateException e) {
                return "Error closing database connection";
			} catch (IOException e) {
                return "Error closing database connection";
			}
	        try {
				throw new IOException(statusLine.getReasonPhrase());
			} catch (IOException e) {
                return "Error closing database connection";
			}
	    }
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
	

}
