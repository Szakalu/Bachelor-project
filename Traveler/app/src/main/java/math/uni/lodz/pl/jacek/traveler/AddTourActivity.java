package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.Tour;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.serializable.SerializableTour;

public class AddTourActivity extends AppCompatActivity implements AsyncResponse {

    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";

    private final static int MY_REQUEST_ID = 1;
    public static final int PLEASE_WAIT_DIALOG = 1;
    private static final String TOUR = "Tour";
    private static final String ATTRACTIONS_LIST = "Attractions_List";


    private EditText editTextName;
    private EditText editTextDescription;
    private Spinner spinnerCountry;
    private Spinner spinnerState;

    private ArrayList<Integer> tourAttractionsIds = new ArrayList<>();
    private boolean isNameGood = false;
    private String tourName = "";

    int countryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tour);
        setTextViewTourName();
        setTextViewTourDescription();
        setTextViewTourCountry();
        setTextViewTourState();
        setEditTextName();
        setEditTextDescription();
        setButtonPickAttractions();
        setSpinnerCountry();
        setSpinnerState();
    }

    private void setTextViewTourName(){
        TextView textViewTourName = (TextView) findViewById(R.id.textViewTourName);
        textViewTourName.setText(getString(R.string.text_view_tour_name_add_tour_activity));
        textViewTourName.setTextSize(16);
        textViewTourName.setTextColor(Color.BLACK);
    }

    private void  setTextViewTourDescription(){
        TextView textViewTourDescription = (TextView) findViewById(R.id.textViewTourDescription);
        textViewTourDescription.setText(getString(R.string.text_view_tour_description_add_tour_activity));
        textViewTourDescription.setTextSize(16);
        textViewTourDescription.setTextColor(Color.BLACK);
    }

    private void setButtonPickAttractions(){
        Button buttonPickAttractions = (Button) findViewById(R.id.buttonPickAttractions);
        buttonPickAttractions.setText(getString(R.string.button_pick_attractions_add_tour_activity));

        buttonPickAttractions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkIfTourIsCorrect()){
                    SerializableTour serializableTour = new SerializableTour(editTextName.getText().toString(), editTextDescription.getText().toString(),
                            getStateId(spinnerState.getSelectedItem().toString()), getUserName(), tourAttractionsIds.size());

                    Intent pickAttractionsForTourIntent = new Intent(AddTourActivity.this, PickAttractionsForTourActivity.class);
                    pickAttractionsForTourIntent.putExtra(TOUR,serializableTour);
                    pickAttractionsForTourIntent.putExtra(ATTRACTIONS_LIST,tourAttractionsIds);
                    startActivityForResult(pickAttractionsForTourIntent,MY_REQUEST_ID);
                }
            }
        });
    }

    private String getCountryName(DatabaseTraveler databaseTraveler){
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(AddTourActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(AddTourActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }


    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private int getStateId(String stateName){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int stateId = databaseTraveler.getStateIdWhereStateName(stateName);
        databaseTraveler.close();
        return stateId;
    }

    private int getCountryId(String countryName){
        if(countryName.equals(getStringByLocal(AddTourActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))){
            countryName = getStringByLocal(AddTourActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private String getUserName(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_USERNAME,null) != null){
            return pref.getString(PREF_USERNAME,null);
        }
        return "NaN";
    }

    private void setTextViewTourCountry(){
        TextView textViewTourCountry = (TextView) findViewById(R.id.textViewCountry);
        textViewTourCountry.setText(getString(R.string.text_view_tour_country_add_tour_activity));
        textViewTourCountry.setTextSize(16);
        textViewTourCountry.setTextColor(Color.BLACK);
    }

    private void setTextViewTourState(){
        TextView textViewTourState = (TextView) findViewById(R.id.textViewState);
        textViewTourState.setText(getString(R.string.text_view_tour_state_add_tour_activity));
        textViewTourState.setTextSize(16);
        textViewTourState.setTextColor(Color.BLACK);
    }

    private void setEditTextName(){
        editTextName = (EditText) findViewById(R.id.editTextTourName);
    }

    private void setEditTextDescription(){
        editTextDescription = (EditText) findViewById(R.id.editTextTourDescription);
    }

    private void setSpinnerCountry() {
        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        List<String> list = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getRowCountry();
        while (cursor.moveToNext()) {
            list.add(translateCountryName(cursor.getString(1)));
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerCountry.setAdapter(adapter);

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countryId = getCountryId(spinnerCountry.getSelectedItem().toString());
                changeStates(spinnerCountry.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void changeStates(String country) {
        List<String> listOfStates = new ArrayList<>();
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        Cursor cursor = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while (cursor.moveToNext()) {
            listOfStates.add(cursor.getString(1));
        }
        databaseTraveler.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfStates);
        adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item);
        spinnerState.setAdapter(adapter);
    }


    private void setSpinnerState() {
        spinnerState = (Spinner) findViewById(R.id.spinnerState);
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tourAttractionsIds.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private boolean checkTourName(){
        if(editTextName.getText().toString().length()<3 || editTextName.getText().toString().length()>50){
            return false;
        }
        return true;
    }

    private boolean checkTourDescription(){
        if(editTextDescription.getText().toString().length()<20 || editTextDescription.getText().toString().length()>1000){
            return false;
        }
        return true;
    }

    private boolean checkIfTourIsCorrect(){
        if(!checkTourName()){
            Toast.makeText(AddTourActivity.this, "Wrong tour name", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!checkTourDescription()){
            Toast.makeText(AddTourActivity.this, "Wrong tour description", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void processFinish(Integer output) {
        switch (output){
            case 1:
                isNameGood = true;
                SerializableTour serializableTour = new SerializableTour(editTextName.getText().toString(), editTextDescription.getText().toString(),
                        getStateId(spinnerState.getSelectedItem().toString()), getUserName(), tourAttractionsIds.size());

                Intent pickAttractionsForTourIntent = new Intent(AddTourActivity.this, PickAttractionsForTourActivity.class);
                pickAttractionsForTourIntent.putExtra(TOUR,serializableTour);
                pickAttractionsForTourIntent.putExtra(ATTRACTIONS_LIST,tourAttractionsIds);
                startActivityForResult(pickAttractionsForTourIntent,MY_REQUEST_ID);
                break;
            case 2:
                Toast.makeText(this, "Please change tour name", Toast.LENGTH_SHORT).show();
                isNameGood = false;
                break;

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Tour name checking");
                dialog.setMessage("Wait !!!");
                dialog.setCancelable(false);
                return dialog;

            default:
                break;
        }

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_ID && resultCode == RESULT_OK) {
            finish();
        }
        else if (requestCode == MY_REQUEST_ID && resultCode == RESULT_CANCELED) {
            tourAttractionsIds = data.getIntegerArrayListExtra(ATTRACTIONS_LIST);
            DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
            for (Integer idAttraction: tourAttractionsIds) {
                Log.i("AttractionName",databaseTraveler.getOneRowAttractions(idAttraction).getName());
            }
            databaseTraveler.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
