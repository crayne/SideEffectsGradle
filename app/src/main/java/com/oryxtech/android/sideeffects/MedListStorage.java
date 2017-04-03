package com.oryxtech.android.sideeffects;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MedListStorage {
    public static final String PREFS_NAME = "SideEffectsMedList";
    public static final String PREFS_NUM = "SideEffectsNumPrefs";
    public static final String NUM_MEDS = "SideEffectsNumMeds";
    SharedPreferences Meds;
    SharedPreferences NumMeds;
    Activity CurrentActivity;

    public MedListStorage(Activity activity) {
        CurrentActivity = activity;
        NumMeds = activity.getSharedPreferences(PREFS_NUM, 0);
        Meds = activity.getSharedPreferences(
                "com.oryxtech.android.sideeffects", Context.MODE_PRIVATE);

    }

    public void addMed(String selection) {
        SharedPreferences.Editor editor = Meds.edit();
        int numMeds = NumMeds.getInt(NUM_MEDS, 0);
        //Check for duplicates
        for (int i = 0; i < numMeds; i++) {
            Integer medNum = (Integer) i;
            String medName = Meds.getString(medNum.toString(), "null string");
            if (medName.equals(selection)) {
                new Alert("This medication is already in the list.");
                return;
            }

        }
        Integer integerNumMeds = (Integer) numMeds;
        editor.putString(integerNumMeds.toString(), selection);
        editor.commit();
        //Increment number of meds
        numMeds++;
        editor = NumMeds.edit();
        editor.putInt(NUM_MEDS, numMeds);
        editor.commit();
    }

    public void deleteMed(MedListArrayAdapter arrayAdapter, ListView listView) {
        SharedPreferences.Editor editor = Meds.edit();
        editor.clear();
        int count = arrayAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Integer integerIndex = (Integer) i;
            String item = arrayAdapter.getItem(i);
            editor.putString(integerIndex.toString(), item);
        }
        editor.commit();
        //Decrement number of meds
        editor = NumMeds.edit();
        int numMeds = NumMeds.getInt(NUM_MEDS, 0);
        numMeds--;
        editor.putInt(NUM_MEDS, numMeds);
        editor.commit();
        if (numMeds < Utils.maxMedlistRows) {
            Utils.setListViewHeight(listView, numMeds);
        }

    }

    //Get strings from SharedPreferences and put them into the medication list
    public void restoreMedList(ArrayList<String> myMedicationListItems, ArrayAdapter<String> myMedicationListAdapter, ListView myMedicationListView) {
        //Get number of medications in list
        int numMeds = NumMeds.getInt(NUM_MEDS, 0);
        if (numMeds == 0) return;
        //TODO: Get med names from SharedPreference and add to list
        for (int i = 0; i < numMeds; i++) {
            Integer integerNumMeds = (Integer) i;
            String medName = Meds.getString(integerNumMeds.toString(), "null string");
            myMedicationListItems.add(medName);
        }


        myMedicationListAdapter.notifyDataSetChanged();
        Context context = CurrentActivity.getApplicationContext();
        int maxRows = Utils.maxMedlistRows;
        System.out.println("In restoreMedList, max medlist rows = " + maxRows);
        Utils.setListViewHeight(myMedicationListView, maxRows);
    }
}
