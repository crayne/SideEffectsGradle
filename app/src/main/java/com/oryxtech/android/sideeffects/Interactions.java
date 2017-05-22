package com.oryxtech.android.sideeffects;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


/*
 * Medication interactions to test with:
 *
 * saquinavir / formoterol fumarate -- interaction list item is good
 * fentanyl / saquinavir -- interaction list item is good
 * atazanavir / prilosec -- interaction list item is good
 * fluoxetine / nardil -- interaction list item is good
 */

public class Interactions extends Activity {
    String[] medicationArray;
    Activity currentActivity = this;
    ArrayList<String> interactionsArrayList = new ArrayList<String>();


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
        try {
            rowTitleString = URLEncoder.encode(rowTitleString, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
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

            //Getting severities for first and second interactions correctly
            //Works for 2 medications, not for 4
            //TODO: skipping every other element is clearly wrong -- doesn't work for more than 2 medications
            //TODO: look into a parameter in the url to eliminate second interactions of the same pair
            //Need to add "&sources=DrugBank" to query in interactions.php, and then use every element -- eliminate severity
            //
    		for (int i=0; i<jsonArray.size(); i++) {
                //jsonArray[i].severity = jsonArray[i].severity[0];

                JSONObject jObject1 = (JSONObject) jsonArray.get(i);
                JSONObject severityObject1 = (JSONObject) jObject1.get("severity");
                String severity1 = (String) severityObject1.get("0");

                String listItem = "Severity of interaction between ";
                //if (severity2.equals("N/A")) continue;
                String drugName1 = getJSONAttribute(jObject1, "originalDrugName1");
                if (drugName1 == null) drugName1 = getJSONAttribute(jObject1, "drug1");
                String drugName2 = getJSONAttribute(jObject1, "originalDrugName2");
                if (drugName2 == null) drugName2 = getJSONAttribute(jObject1, "drug2");

                listItem += drugName1 + " and " + drugName2 + " is " + severity1 + ".  ";
                JSONObject descriptionObject = (JSONObject) jObject1.get("descriptionText");
                String description = (String) descriptionObject.get("0");
                listItem += description;
                interactionsArrayList.add(listItem);



            }
            //interactionsArrayList can have size 0 - show alert
            if (interactionsArrayList.isEmpty()){
                new Alert("No interactions were found");
                return;
            }

            TextView label = (TextView) currentActivity.findViewById(R.id.interactions_title);
            label.setText("Interactions Between Your Medications");


            ArrayAdapter<String> interactionsListAdapter = new ArrayAdapter<String>(currentActivity,
                    R.layout.symptoms_for_med_listitem);
            ListView interactionsListView = (ListView) currentActivity
                    .findViewById(R.id.interactions_list);
            interactionsListView.setAdapter(interactionsListAdapter);

            /* Now need to copy rows from interactionsArrayList */
            for (int i=0; i<interactionsArrayList.size(); i++){
                String rowText = interactionsArrayList.get(i);
                interactionsListAdapter.add(rowText);

            }
            interactionsListAdapter.notifyDataSetChanged();

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
