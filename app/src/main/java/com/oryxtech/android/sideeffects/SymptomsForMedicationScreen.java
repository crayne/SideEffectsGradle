package com.oryxtech.android.sideeffects;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
/*
 *  List of All Symptoms for a Single Medication, together with their frequencies
 */

public class SymptomsForMedicationScreen extends Activity {
	Activity CurrentActivity;
	ArrayList<String> symptomFrequencyListItems;
	ArrayAdapter<String> symptomFrequencyListAdapter;
	ArrayAdapter<String> symptomListAdapter;
	ListView symptomFrequencyListView;
	String currentMedication;
	static float FLOAT_UNDEFINED = -1.0f;

	String urlBase = Utils.urlBase;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		CurrentActivity = this;
		Utils.CurrentActivity = this;

		setContentView(R.layout.activity_symptoms_for_med);

		Intent intent = getIntent();
		currentMedication = intent
				.getStringExtra("com.oryx.allaboard.CurrentMedication");

		// Capitalize first letter of currentMedication
		String firstPart = currentMedication.substring(0, 1);
		firstPart = firstPart.toUpperCase();
		currentMedication = firstPart + currentMedication.substring(1);

		TextView label = (TextView) this
				.findViewById(R.id.med_side_effects_label);
		label.setText("Most Common Side Effects For " + currentMedication);

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		// Get all symptoms and frequencies for currentMedication

		symptomFrequencyListItems = new ArrayList<String>();
		symptomFrequencyListAdapter = new ArrayAdapter<String>(this,
				R.layout.symptoms_for_med_listitem);
		symptomFrequencyListView = (ListView) this
				.findViewById(R.id.symptoms_for_med_list);
		symptomFrequencyListView.setAdapter(symptomFrequencyListAdapter);
		
        Button returnButton = (Button)findViewById(R.id.back_button);
        returnButton.setOnClickListener(leavePageListener);
 
		String searchUrl = urlBase + "/medSideEffectsA.php?";
		// String completeSearchUrl = searchUrl + "?searchValue=" +
		// searchString;
		String uMedication = null;
		try {
			uMedication = URLEncoder.encode(currentMedication, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		String params = "medication=" + uMedication;

		String completeSearchUrl = searchUrl + params;
		// new ("Url for searching symptoms = " + completeSearchUrl);
		new SymptomsForMedicationRequestTask().execute(completeSearchUrl);

	}
	
	 private OnClickListener leavePageListener = new OnClickListener() {
		    public void onClick(View v) {
		      // do something when the button is clicked
		    	finish();
		    }
 	};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sideeffects, menu);

		return true;
	}

	class SideEffectsClass {
		public String verbalFrequency;
		public float numFrequency;
		public String sideEffectName;
		public float freqMin = FLOAT_UNDEFINED;
		public float freqMax = FLOAT_UNDEFINED;
		public String vFreqMin = "";
		public String vFreqMax = "";
        public float totalFrequency = FLOAT_UNDEFINED;
        public float averageFrequency = FLOAT_UNDEFINED;

		public void copy(SideEffectsClass from){
			sideEffectName = from.sideEffectName;
			freqMin = from.freqMin;
			freqMax = from.freqMax;
			vFreqMin = from.vFreqMin;
			vFreqMax = from.vFreqMax;
			numFrequency = from.numFrequency;
			verbalFrequency = from.verbalFrequency;
            totalFrequency = from.totalFrequency;
            averageFrequency = from.averageFrequency;
		}
		
	}

	class SymptomsForMedicationRequestTask extends HttpRequestTask {
		String verbalFrequencies = "rare,infrequent,frequent";
		ProgressBar progressBar;

		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
		    progressBar = (ProgressBar)findViewById(R.id.symptoms_for_medication);
	        progressBar.setVisibility(View.VISIBLE);

		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			createRows(result);
			progressBar.setVisibility(View.INVISIBLE);

		}

		/*
		 * 
		 * Combine all array objects with the same first field into one row,
		 * with frequency = value of lowest frequency followed by value of
		 * highest frequency
		 */
		private String makeFrequencyText(SideEffectsClass rowObject) {
            String allText;
            if (Utils.freqFormat.equals("single")){
                float frequency = rowObject.averageFrequency;
                frequency = Math.round(frequency*100f)/ 100f;
                allText = frequency + "%";
                return allText;
            }
            else if (Utils.freqFormat.equals("verbal")){
                float frequency = rowObject.averageFrequency;

                if (frequency > 1.0f) allText = "frequent";
                else if (frequency > .1f) allText = "infrequent";
                else if (frequency > 0.0) allText = "rare";
                else allText = "";
                return allText;
            }

            String numText = makeNumericFrequencyText(rowObject);
			String verbalText = makeVerbalFrequencyText(rowObject);
			if (numText == "" && verbalText == "")
				return "";
			allText = numText;
			if (numText != "" && verbalText != "")
				allText += ", ";
			allText += verbalText;
			return allText;
		}

		private String makeNumericFrequencyText(SideEffectsClass rowObject) {
			if (rowObject.freqMin == 0 && rowObject.freqMax == 0)
				return "";
			String frequency = rowObject.freqMin + "%";
			if (rowObject.freqMin != rowObject.freqMax) {
				frequency += " to " + rowObject.freqMax + "%";
			}
			return frequency;

		}

		private String makeVerbalFrequencyText(SideEffectsClass rowObject) {
			if (rowObject.vFreqMin == "" && rowObject.vFreqMax == "")
				return "";
			if (rowObject.vFreqMin == "")
				return rowObject.vFreqMax;
			if (rowObject.vFreqMax == "")
				return rowObject.vFreqMin;
			if (rowObject.vFreqMin == rowObject.vFreqMax)
				return rowObject.vFreqMin;
			String frequency = rowObject.vFreqMin + " to " + rowObject.vFreqMax;
			return frequency;
		}

		private void addSideEffectRow(SideEffectsClass rowObject) {
			String frequency = makeFrequencyText(rowObject);
			String rowLabelText = rowObject.sideEffectName;
			if (frequency != "" && !frequency.equals("")) {
				rowLabelText += ": " + frequency;
			}
			symptomFrequencyListAdapter.add(rowLabelText);
			symptomFrequencyListAdapter.notifyDataSetChanged();
		}

		private float numericFrequencyMinCompare(float a, float b) {
			if (a == FLOAT_UNDEFINED && b == FLOAT_UNDEFINED)
				return FLOAT_UNDEFINED;
			if (a == FLOAT_UNDEFINED)
				return b;
			if (b == FLOAT_UNDEFINED)
				return a;
			if (a == 0.0f) return b;
			if (b == 0.0f) return a;
			return Math.min(a, b);
		}

		private String verbalFrequencyMinCompare(String a, String b) {
			if (a == "" || a.equals(""))
				return b;
			if (b == "" || b.equals(""))
				return a;
			int aIndex = verbalFrequencies.indexOf(a);
			if (aIndex == -1)
				return b;
			int bIndex = verbalFrequencies.indexOf(b);
			if (bIndex == -1)
				return a;
			if (aIndex < bIndex)
				return a;
			else
				return b;
		}

		private String verbalFrequencyMaxCompare(String a, String b) {
			if (a == "" || a.equals(""))
				return b;
			if (b == "" || b.equals(""))
				return a;
			int aIndex = verbalFrequencies.indexOf(a);
			if (aIndex == -1)
				return b;
			int bIndex = verbalFrequencies.indexOf(b);
			if (bIndex == -1)
				return a;
			if (aIndex >= bIndex)
				return a;
			else
				return b;
		}

        //Frequency for a given side effect expressed as a range
        private SideEffectsClass[] collapseRows(SideEffectsClass[] origArray) {

            Comparator<SideEffectsClass> compareFuncForSort = new Comparator<SideEffectsClass>() {
                public int compare(SideEffectsClass a, SideEffectsClass b) {
                    return a.sideEffectName.compareTo(b.sideEffectName);
                }
            };
            Arrays.sort(origArray, compareFuncForSort);
            SideEffectsClass accumulator = new SideEffectsClass();
            accumulator.copy(origArray[0]);
            SideEffectsClass[] newArray = new SideEffectsClass[origArray.length];
            int numRepeats = 0;
            int i = 1;
            int j = 0;
            while (i < origArray.length) {
                if (accumulator.sideEffectName
                        .compareTo(origArray[i].sideEffectName) == 0) {
                    numRepeats++;
                    if (accumulator.freqMin == FLOAT_UNDEFINED) {
                        accumulator.freqMin = numericFrequencyMinCompare(
                                accumulator.numFrequency,
                                origArray[i].numFrequency);

                    } else
                        accumulator.freqMin = numericFrequencyMinCompare(
                                accumulator.freqMin,
                                origArray[i].numFrequency);

                    if (accumulator.freqMax == FLOAT_UNDEFINED) {
                        accumulator.freqMax = Math.max(
                                accumulator.numFrequency,
                                origArray[i].numFrequency);
                    } else
                        accumulator.freqMax = Math.max(
                                accumulator.freqMax,
                                origArray[i].numFrequency);
                    // Compare verbal frequencies. Ignore blanks
                    if (accumulator.vFreqMin.equals("")) {
                        accumulator.vFreqMin = verbalFrequencyMinCompare(
                                accumulator.verbalFrequency,
                                origArray[i].verbalFrequency);

                    } else {
                        accumulator.vFreqMin = verbalFrequencyMinCompare(
                                accumulator.vFreqMin,
                                origArray[i].verbalFrequency);
                    }
                    if (accumulator.vFreqMax.equals("")) {
                        accumulator.vFreqMax = verbalFrequencyMaxCompare(
                                accumulator.verbalFrequency,
                                origArray[i].verbalFrequency);
                    } else {
                        accumulator.vFreqMax = verbalFrequencyMaxCompare(
                                accumulator.vFreqMax,
                                origArray[i].verbalFrequency);
                    }
                    // origArray.splice(i, 1); //Remove origArray[i] because it
                    // is a duplicate
                    //This doesn't work -- Try marking duplicates, then eliminating with another loop
                    i+=1;

                } else {
                    newArray[j] = new SideEffectsClass();
                    if (numRepeats != 0){
                        newArray[j].copy(accumulator);
                        numRepeats = 0;
                    }
                    //This is for cases where there is only one occurrence of a side effect
                    else {
                        newArray[j].copy(origArray[i-1]);
                        newArray[j].freqMin = newArray[j].freqMax = newArray[j].numFrequency;
                        newArray[j].vFreqMin = newArray[j].vFreqMax = newArray[j].verbalFrequency;

                    }
                    accumulator.copy(origArray[i]);
                    j++;

                    i++;
                }
            }
            //Pick up the last symptom
            newArray[j] = new SideEffectsClass();
            if (numRepeats != 0){
                newArray[j].copy(accumulator);
            }
            //This is for cases where there is only one occurrence of a side effect
            else {
                newArray[j].copy(origArray[i-1]);
                newArray[j].freqMin = newArray[j].freqMax = newArray[j].numFrequency;
            }

            return newArray;

        }

        //Convert a verbal frequency to a number

        private float convertToNumeric(String vFreq){
            if (vFreq.equals("frequent")) return 50;
            else if (vFreq.equals("infrequent")) return .5f;
            else if (vFreq.equals("rare")) return .05f;
            else new Alert("in convertToNumeric, verbal frequency is out of range: " + vFreq);
            return 100f;

        }



        /*
		 * make one frequency from all numeric and verbal frequencies
		 * save all frequencies from server (not just ranges)
		 * ex: 5%, 2%, 14%, frequent
		 *
		 * convert rare to .05% (less than 1%)
		 * convert infrequent to .5% (.1% to 1%)
		 * convert frequent to 50% (1%  to 100%)
		 * list is now 5%, 2%, 14%, 50% -- average these
		 * This happens in collapseRows
		 */

        /*
        If there is numeric information given, one can ignore verbal information?
         */

        //Frequency for a given side effect expressed as a single number
        private SideEffectsClass[] collapseRowsSingleFrequency(SideEffectsClass[] origArray) {

			Comparator<SideEffectsClass> compareFuncForSort = new Comparator<SideEffectsClass>() {
				public int compare(SideEffectsClass a, SideEffectsClass b) {
					return a.sideEffectName.compareTo(b.sideEffectName);
				}
			};
			Arrays.sort(origArray, compareFuncForSort);
			SideEffectsClass accumulator = new SideEffectsClass();
			accumulator.copy(origArray[0]);
			SideEffectsClass[] newArray = new SideEffectsClass[origArray.length];
			int numRepeats = 1;
			int i = 1;
			int j = 0;

            accumulator.averageFrequency = 0;
            if (origArray[0].numFrequency != FLOAT_UNDEFINED) accumulator.totalFrequency = origArray[0].numFrequency;
            else if (origArray[0].verbalFrequency != "") accumulator.totalFrequency = convertToNumeric(origArray[0].verbalFrequency);
            else accumulator.totalFrequency = 0;

            while (i < origArray.length) {
				if (accumulator.sideEffectName.compareTo(origArray[i].sideEffectName) == 0) {
					numRepeats++;
				    if (origArray[i].numFrequency != FLOAT_UNDEFINED){
                        accumulator.totalFrequency += origArray[i].numFrequency;
                    }
                    else if (origArray[i].verbalFrequency != ""){
                        accumulator.totalFrequency += convertToNumeric(origArray[i].verbalFrequency);
                    }

					i+=1;


				} else {
					newArray[j] = new SideEffectsClass();
					if (numRepeats != 0){
						newArray[j].copy(accumulator);
                        newArray[j].averageFrequency = newArray[j].totalFrequency/numRepeats;
						numRepeats = 0;
                        newArray[j].totalFrequency = 0;
					}
					//This is for cases where there is only one occurrence of a side effect
					else {
						newArray[j].copy(origArray[i-1]);
                        if (origArray[i-1].numFrequency != FLOAT_UNDEFINED)  newArray[j].averageFrequency = origArray[i-1].numFrequency;
                        else newArray[j].averageFrequency =  convertToNumeric(origArray[i-1].verbalFrequency);


                    }
					accumulator.copy(origArray[i]);
                    if (origArray[i].numFrequency != FLOAT_UNDEFINED) accumulator.totalFrequency = origArray[i].numFrequency;
                    else if (origArray[i].verbalFrequency != "") accumulator.totalFrequency = convertToNumeric(origArray[i].verbalFrequency);
                    else accumulator.totalFrequency = 0;
                    numRepeats++;
                    j++;

					i++;
				}
			}
			//Pick up the last symptom
			newArray[j] = new SideEffectsClass();
			if (numRepeats != 0){
				newArray[j].copy(accumulator);
                newArray[j].averageFrequency = newArray[j].totalFrequency/numRepeats;

            }
			//This is for cases where there is only one occurrence of a side effect
			else {						
				newArray[j].copy(origArray[i-1]);
                if (origArray[i-1].numFrequency != FLOAT_UNDEFINED)  newArray[j].averageFrequency = origArray[i-1].numFrequency;
                else newArray[j].averageFrequency =  convertToNumeric(origArray[i-1].verbalFrequency);

            }

			return newArray;

		}

		private SideEffectsClass[] addFrequencyField(JSONArray origArray) {
			SideEffectsClass[] newArray = new SideEffectsClass[origArray
					.length()];
			int a = 1;

			for (int i = 0; i < origArray.length(); i++) {
				try {
					newArray[i] = new SideEffectsClass();
					// newArray[i].numFrequency = newArray[i].verbalFrequency =
					// "";

					newArray[i].verbalFrequency = "";
					newArray[i].numFrequency = FLOAT_UNDEFINED;

					String freq1 = origArray.getJSONObject(i).getString(
							"frequency");
					int pcentLoc = freq1.indexOf("%");
					if (pcentLoc == -1) {
						// alert("frequency is: " + freq1);

						if (verbalFrequencies.indexOf(freq1) != -1) {
							newArray[i].verbalFrequency = freq1;
						}
					} else {
						String freq2 = freq1.substring(0, pcentLoc);
						float intFreq2 = Float.parseFloat(freq2);
						newArray[i].numFrequency = intFreq2;
					}
					// Capitalize first letter of sideEffectName
					String seName = origArray.getJSONObject(i).getString(
							"sideEffectName");
					//String firstPart = seName.substring(0, 1);
					//firstPart = firstPart.toUpperCase();
					//seName = firstPart + seName.substring(1);

					//newArray[i].sideEffectName = seName;
					newArray[i].sideEffectName = seName.toLowerCase();
				} catch (JSONException e) {
					new Alert("JSONException in addFrequencyField = "
							+ e.getMessage());
				}

			}
			return newArray;

		}

        //This assumes that side effects with zero frequencies are actually from placebo test results
        private SideEffectsClass[] removeZeroFrequencies(SideEffectsClass[] origArray) {

            List<SideEffectsClass> list = new ArrayList<SideEffectsClass>();
            //list.removeAll(Arrays.asList("a"));
            //origArray = list.toArray(origArray);

            for (int i = 0; i < origArray.length; i++) {
                if (origArray[i].numFrequency == 0 && origArray[i].verbalFrequency == ""){
                    continue;
                }
                list.add(origArray[i]);
            }
            SideEffectsClass[] newArray = list.toArray(new SideEffectsClass[list.size()]);
            return newArray;

        }




        protected void createRows(String sideEffectsReturnData) {
            Comparator<SideEffectsClass> compareFrequenciesForSort = new Comparator<SideEffectsClass>() {
                public int compare(SideEffectsClass a, SideEffectsClass b) {
                    if (a == null || b == null) return 0;
                    /*
                    System.out.println("a.sideEffectName, b.sideEffectName = " +
                        a.sideEffectName + ", " + b.sideEffectName);
                    System.out.println("a.averageFrequency, b.averageFrequency = " +
                            a.averageFrequency + ", " + b.averageFrequency);
                    */
                    Float aFloat = new Float (a.averageFrequency);
                    Float bFloat = new Float (b.averageFrequency);
                    return bFloat.compareTo(aFloat);
                }
            };
            if (sideEffectsReturnData.indexOf("no side effects found") != -1 || sideEffectsReturnData.indexOf("not found") != -1
                    || sideEffectsReturnData.indexOf("no match") != -1) {
                new Alert("No side effects found for " + currentMedication);
                return;
            }
            JSONArray sideEffectsInfo = null;
			try {
				sideEffectsInfo = new JSONArray(sideEffectsReturnData);
			} catch (JSONException e) {
				new Alert("Error parsing returned data");
				return;
			}
			//System.out.println("JSON parsing done");

			SideEffectsClass[] sideEffectsInfoArray = addFrequencyField(sideEffectsInfo);
			//System.out.println("After addFrequencyField");
            if (Utils.freqFormat.equals("multi")){
			    sideEffectsInfoArray = collapseRows(sideEffectsInfoArray);
            }

            else {
                //sideEffectsInfoArray = removeZeroFrequencies(sideEffectsInfoArray);
                sideEffectsInfoArray = collapseRowsSingleFrequency(sideEffectsInfoArray);
                //Sort the side effects by frequency
                Arrays.sort(sideEffectsInfoArray, compareFrequenciesForSort);
            }

            //TODO: test verbal frequencies
            //TODO: round off frequencies to 2 digits

            symptomFrequencyListView.setVisibility(View.VISIBLE);
			int numSideEffects = sideEffectsInfoArray.length;
			for (int i=0; i<numSideEffects; i++){
				if (sideEffectsInfoArray[i] != null){

					addSideEffectRow(sideEffectsInfoArray[i]);
				}	
			}
		}

	}

}
