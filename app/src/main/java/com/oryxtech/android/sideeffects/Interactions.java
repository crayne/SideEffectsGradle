package com.oryxtech.android.sideeffects;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;


public class Interactions extends Activity {
    String[] medicationArray;
    Activity currentActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactions);
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        Button returnButton = (Button)findViewById(R.id.back_button);
        returnButton.setOnClickListener(leavePageListener);
        Intent intent = getIntent();
        medicationArray = intent.getStringArrayExtra("com.oryxtech.android.sideeffects.MedicationArray");

        String rowTitleString = "";
        int numRowTitles = medicationArray.length;
        for (int i = 0; i < numRowTitles; i++){
            rowTitleString += medicationArray[i];
            if (i != numRowTitles-1) rowTitleString += ",";
        }
        getInteractions(rowTitleString);
    }

    protected void getInteractions(String rowTitleString){
        String completeSearchUrl = Utils.localHost + Utils.getInteractionsPhpUrl;
        completeSearchUrl = completeSearchUrl + "?medNames=" + rowTitleString;
        new InteractionRequestTask().execute(completeSearchUrl);
    }

    /*
    Two array elements are returned for each interaction -- the second one contains the severity
    This should be fixed in the PHP code so that it works with the IOS version
     */



    class InteractionRequestTask extends HttpRequestTask{

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

            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = null;
            try{
                jsonArray = (JSONArray)jsonParser.parse(result);
            }
            catch(ParseException e){
                System.out.println("Error parsing interactions result " + e.getMessage());
            }

            ArrayList<String> severityArrayList = new ArrayList<String>();
            //Getting severities for first and second interactions correctly
            //Works for 2 medications, not for 4
    		for (int i=0; i<jsonArray.size(); i+=2) {
                //jsonArray[i].severity = jsonArray[i].severity[0];

                JSONObject jObject1 = (JSONObject) jsonArray.get(i);
                JSONObject severityObject1 = (JSONObject) jObject1.get("severity");
                String severity1 = (String) severityObject1.get("0");

                JSONObject jObject2 = (JSONObject) jsonArray.get(i + 1);
                JSONObject severityObject2 = (JSONObject) jObject2.get("severity");
                String severity2 = (String) severityObject2.get("0");

                String listItem = "Severity of interaction between ";
                if (severity2.equals("N/A")) continue;
                String originalDrugName1 = getJSONAttribute(jObject1, "originalDrugName1");
                String originalDrugName2 = getJSONAttribute(jObject1, "originalDrugName2");
                listItem += originalDrugName1 + " and " + originalDrugName2 + " is " + severity2;
                severityArrayList.add(listItem);



            }


        }

/*
            result = result.toLowerCase();

            String[] medArray = result.split(",");

            ArrayAdapter<String> foundMedicationListAdapter =
                    new ArrayAdapter<String>(currentActivity, R.layout.symptom_autocomplete_listitem, medArray);
            autoCompleteTextView.setAdapter(foundMedicationListAdapter);
            foundMedicationListAdapter.notifyDataSetChanged();
*/
        private String getJSONAttribute(JSONObject jsonObject, String attribute) {
            String attrValue = (String) jsonObject.get(attribute);
            return attrValue;
        }
    }

    private View.OnClickListener leavePageListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

}
