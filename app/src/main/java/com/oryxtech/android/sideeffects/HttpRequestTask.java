package com.oryxtech.android.sideeffects;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpRequestTask extends AsyncTask<String, String, String>{
    @Override
	protected String doInBackground(String... uri){
		//Do "AJAX" here
 		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;

		try {
			HttpUriRequest request = new HttpGet(uri[0]);
			response = httpclient.execute(request);
		} catch (Exception e) {
			//e.printStackTrace();
            return "Error connecting to back end is: " + e.getMessage();
		}
	    StatusLine statusLine = response.getStatusLine();
	    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        try {
				response.getEntity().writeTo(out);
			} catch (IOException e) {
                return "Error writing response is " + e.getMessage();
            }
	        try {
				out.close();
			} catch (IOException e) {
                return "Error closing output stream is " + e.getMessage();

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
                return "Error gettin reason phrase is " + e.getMessage();
			}
	    }
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
	

}
