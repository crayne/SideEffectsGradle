package com.oryxtech.android.sideeffects;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Interactions extends Activity {
    String[] medicationArray;

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

            result = result.toLowerCase();
            String[] medArray = result.split(",");
            /*
            ArrayAdapter<String> foundMedicationListAdapter =
                    new ArrayAdapter<String>(CurrentActivity, R.layout.symptom_autocomplete_listitem, medArray);
            autoCompleteTextView.setAdapter(foundMedicationListAdapter);
            foundMedicationListAdapter.notifyDataSetChanged();
            */
        }
    }

    private View.OnClickListener leavePageListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };

}
