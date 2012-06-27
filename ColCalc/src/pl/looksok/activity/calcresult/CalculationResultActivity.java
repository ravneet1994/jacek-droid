package pl.looksok.activity.calcresult;

import java.util.List;

import pl.looksok.R;
import pl.looksok.activity.ColCalcActivity;
import pl.looksok.activity.WelcomeActivity;
import pl.looksok.activity.addperson.AddNewPerson;
import pl.looksok.logic.CalculationLogic;
import pl.looksok.logic.PersonData;
import pl.looksok.utils.CalcPersistence;
import pl.looksok.utils.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class CalculationResultActivity extends ColCalcActivity {
	protected static final String LOG_TAG = CalculationResultActivity.class.getSimpleName();
	
	private CalculationLogic calc = null;
	private ListView resultList;
	private List<PersonData> listArray;
	private ResultsListAdapter adapter;
	
	private CalcResultUtils utils = new CalcResultUtils();
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculation);
        
        readInputBundle();
        resultList = (ListView)findViewById(R.id.calc_listView_list);
        populateListArray();
        initButtons();
    }

	private void initButtons() {
		((Button)findViewById(R.id.calc_button_saveCalculation)).setOnClickListener(saveCalculationButtonClickListener);
        ((Button)findViewById(R.id.calc_button_sendCalculation)).setOnClickListener(shareCalculationButtonClickListener);
        ((Button)findViewById(R.id.calc_button_addPerson)).setOnClickListener(addPersonButtonClickListener);
	}

	private void populateListArray() {
		listArray = utils.readCalcPeopleToListArray(calc);
		adapter = new ResultsListAdapter(CalculationResultActivity.this, R.layout.calculation_list_item, listArray);
		resultList.setAdapter(adapter);
	}

	private void readInputBundle() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			calc = (CalculationLogic)extras.getSerializable(Constants.BUNDLE_CALCULATION_OBJECT);
		}
	}
	
	OnClickListener saveCalculationButtonClickListener = new OnClickListener() {
        public void onClick(View v) {
        	CalcPersistence.saveCalculation(getApplicationContext(), "installedapplist.txt", calc);
        	Toast.makeText(getApplicationContext(), R.string.calculation_saved_text, Toast.LENGTH_SHORT).show();
        }
    };
    
    OnClickListener shareCalculationButtonClickListener = new OnClickListener() {
    	public void onClick(View v) {
    		try{
    			Intent emailIntent = utils.prepareEmailIntent(getApplicationContext(), calc);
    			startActivity(Intent.createChooser(emailIntent, getString(R.string.email_utils_chooseEmailClient)));
    		}catch(NullPointerException e){
    			Log.e(LOG_TAG, "Error while preparing email. there is no email Addresses probably: " + e.getMessage());
    		}
    	}
    };

    OnClickListener addPersonButtonClickListener = new OnClickListener() {
    	public void onClick(View v) {
        	Intent intent = new Intent(getApplicationContext(), AddNewPerson.class) ;
        	intent.putExtra(Constants.BUNDLE_CALCULATION_OBJECT, calc);
        	startActivity(intent);
        	overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        	finish();
    	}
    };

	public void editPerson(View v) {
		PersonData pd = calc.findPersonInList((PersonData)v.getTag());
		calc.getInputPaysList().remove(pd);
		
    	Intent intent = new Intent(getApplicationContext(), AddNewPerson.class) ;
    	intent.putExtra(Constants.BUNDLE_CALCULATION_OBJECT, calc);
		intent.putExtra(Constants.BUNDLE_PERSON_TO_EDIT, pd);
    	startActivity(intent);
    	finish();
	}

	public void removePerson(View v){
		PersonData pd = calc.findPersonInList((PersonData)v.getTag());
		calc.removePerson(pd);
		calc.recalculate();
		populateListArray();
	}
	
	@Override
	public void onBackPressed() {
    	Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class) ;
    	startActivity(intent);
    	finish();
	}
}