/**
 * 
 */
package com.oryxtech.android.sideeffects;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author susancrayne
 * Methods used by more than one class
 *
 */
public class Utils {
	public static Activity CurrentActivity;
    public static int maxMedlistRows = 4;
	//Now using latest database (local) called sideEffects3
    //TODO: Change HTTP Error messages or add them here

    //multi:  ex. 2.5% to 8.4%, frequent, infrequent
    //single:  6.7%
    //verbal: frequent

    //This is the latest version

    static String freqFormat =  "verbal";

    //This is the test version
    //static String freqFormat =  "multi";
    public static String localHost =  "http://192.168.1.170:8888/sideEffectsNewRxnav";
    public static String serverHost = "http://www.oryxtech.net/sideEffectsNewRxnav";
    public static String nuiUrl =  "http://rxnav.nlm.nih.gov/REST/search?conceptName=";
    public static String getInteractionsPhpUrl = "/interactions.php";

    public static String urlBase =  localHost;


    private Utils(){
		
	}
	
	public static void setListViewHeight(ListView listView, int maxRows){
       ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        System.out.println("In setListViewHeight, maxRows = " + maxRows);

        int limit = listAdapter.getCount();
        //if (limit > maxRows) return;
        if (limit > maxRows) limit = maxRows;
        System.out.println("In setListViewHeight, limit = " + limit);
        int totalHeight = 0;
        for (int i = 0; i < limit; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        totalHeight += (listView.getDividerHeight() * (limit - 1));
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        System.out.println("In setListViewHeight, height before adjustment = " + params.height);
        params.height = totalHeight;
        System.out.println("In setListViewHeight, height after adjustment = " + params.height);

        listView.setLayoutParams(params);
        listView.requestLayout();
        System.out.println("In setListViewHeight, after requestLayout = " + params.height);



    }

    public static void displayToast(Activity activity, String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

    }
}
