package com.oryxtech.android.sideeffects;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

public class Interactions extends Activity {
    String[] medicationArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactions);
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        Intent intent = getIntent();
        medicationArray = intent.getStringArrayExtra("com.oryxtech.android.sideeffects.MedicationArray");

    }
}
