package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Alert {
	public Alert (String msg){
		AlertDialog alertDialog = new AlertDialog.Builder(Utils.CurrentActivity).create();
		alertDialog.setMessage(msg);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int id) {
		        return;
		    } }); 
		alertDialog.show();
		return;

	}

}
