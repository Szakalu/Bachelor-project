package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.synchronization.objects.StateSynchronization;

public class SynchronizationActivity extends AppCompatActivity {

    private static final int ATTRACTIONS_TO_SYNCHRONIZATION = 1;
    private static final int ATTRACTIONS_NOT_TO_SYNCHRONIZATION = 0;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String DEVICE = "device";
    private static final String SD_CARD = "sd";
    private static final String PREF_WHERE_TO_SAVE = "whereToSave";
    private static final int SYNCHRONIZE_TO_FALSE = 0;
    private static final int SYNCHRONIZE_TO_TRUE = 1;
    private static final String SYNCHRONIZATION_STATUS = "synchronization_status";


    private TableLayout tableLayoutStates;
    private ArrayList<State> arrayListStates = new ArrayList<>();
    private Spinner spinnerCountry;
    private ArrayList<StateSynchronization> statesToSynchronize = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronization);
        setTextViewPickRegions();
        setSpinnerCountry();
        setTableLayoutStates();
        //setCheckBoxDeviceAndSdCard();
    }

    private void setTextViewPickRegions(){
        TextView textViewPickRegions = (TextView) findViewById(R.id.textViewPickRegions);
        textViewPickRegions.setText(getString(R.string.text_view_country_synchronization_activity));
        textViewPickRegions.setTextSize(18);
        textViewPickRegions.setTextColor(Color.BLACK);
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
                toArrayListStates(getCountryId(spinnerCountry.getSelectedItem().toString()));
                setTableRowsStates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getCountryId(String countryName){
        if(countryName.equals(getStringByLocal(SynchronizationActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))){
            countryName = getStringByLocal(SynchronizationActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(SynchronizationActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(SynchronizationActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
        }
        return countryName;
    }

    private String getStringByLocal(Activity context, int id, String locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(new Locale(locale));
        return context.createConfigurationContext(configuration).getResources().getString(id);
    }

    private void setTableLayoutStates(){
        tableLayoutStates = (TableLayout) findViewById(R.id.tableLayoutStates);
        tableLayoutStates.setOrientation(TableLayout.VERTICAL);
    }

    private void toArrayListStates(int countryId){
        arrayListStates.clear();

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);

        Cursor cursorStates = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while(cursorStates.moveToNext()){
            State state = new State(cursorStates.getInt(0), cursorStates.getString(1), cursorStates.getInt(2), cursorStates.getInt(3), 0);
            arrayListStates.add(state);
        }
        databaseTraveler.close();
    }

    private void setTableRowsStates(){
        tableLayoutStates.removeAllViews();

        for (State state: arrayListStates) {
            final TableRow tableRow = new TableRow(this);

            tableRow.setBackgroundColor(Color.WHITE);

            CheckBox checkBoxSynchronization = new CheckBox(this);
            checkBoxSynchronization.setBackgroundColor(Color.WHITE);
            checkBoxSynchronization.setText(state.getStateName());
            checkBoxSynchronization.setTextColor(Color.BLACK);
            checkBoxSynchronization.setTextSize(20);
            if(checkIfStateIsOnList(state.getId())){
                setCheckBoxSynchronizationWhenStateIsOnStatesList(checkBoxSynchronization,state.getId());
            }
            else{
                if(state.getToSynchronized() == ATTRACTIONS_TO_SYNCHRONIZATION){
                    checkBoxSynchronization.setChecked(true);
                }
            }
            tableRow.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            setCheckBoxSynchronization(checkBoxSynchronization,state);


            tableRow.addView(checkBoxSynchronization);

            tableLayoutStates.addView(tableRow);
        }
        //scrollViewStates.setMinimumHeight(tableLayoutStates.getHeight());
    }

    private void setCheckBoxSynchronizationWhenStateIsOnStatesList(CheckBox checkBoxSynchronization, int stateId){
        for (StateSynchronization stateSynchronization: statesToSynchronize) {
            if(stateSynchronization.getStateId() == stateId){
                checkBoxSynchronization.setChecked(true);
            }
        }
        checkBoxSynchronization.setChecked(false);
    }

    private void setCheckBoxSynchronization(CheckBox checkBoxSynchronization, final State state){
        checkBoxSynchronization.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(checkIfStateIsOnList(state.getId())){
                        statesToSynchronize.remove(getStateIdToDelete(state.getId()));
                    }
                    else{
                        statesToSynchronize.add(new StateSynchronization(state.getId(),ATTRACTIONS_TO_SYNCHRONIZATION));
                    }
                    Log.i(state.getStateName(),"is checked");
                }
                else{
                    if(checkIfStateIsOnList(state.getId())){
                        statesToSynchronize.remove(getStateIdToDelete(state.getId()));
                    }
                    else{
                        statesToSynchronize.add(new StateSynchronization(state.getId(),ATTRACTIONS_NOT_TO_SYNCHRONIZATION));
                    }
                    Log.i(state.getStateName(),"is not checked");
                }
            }
        });
    }

    private boolean checkIfStateIsOnList(int stateId){
        for (StateSynchronization stateSynchronization: statesToSynchronize) {
            if(stateSynchronization.getStateId() == stateId){
                return true;
            }
        }
        return false;
    }

    private int getStateIdToDelete(int stateId){
        for (StateSynchronization stateSynchronization: statesToSynchronize) {
            if(stateSynchronization.getStateId() == stateId){
                return statesToSynchronize.indexOf(stateSynchronization);
            }
        }
        return 0;
    }

    private int getDeviceWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private boolean checkIfSdCardIsInDevice(){
        boolean isSdInDevice = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSdInDevice){
            return true;
        }
        return false;
    }

    /**
    private void setCheckBoxDeviceAndSdCard(){
        checkBoxDevice = (CheckBox) findViewById(R.id.checkBoxDevice);
        checkBoxSdCard = (CheckBox) findViewById(R.id.checkBoxSdCard);

        checkBoxDevice.setText(getString(R.string.check_box_device_synchronization_activity));
        checkBoxSdCard.setText(getString(R.string.check_box_sd_card_synchronization_activity));

        if(!checkIfSdCardIsInDevice()){
            checkBoxSdCard.setEnabled(false);
        }

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_WHERE_TO_SAVE,"") == SD_CARD){
            checkBoxSdCard.setChecked(true);
        }
        else{
            checkBoxDevice.setChecked(true);
        }

        checkBoxDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBoxDevice.isChecked()){
                    checkBoxDevice.setChecked(true);
                    checkBoxSdCard.setChecked(false);
                    rememberWhereToSave(DEVICE);
                }
                else{
                    checkBoxDevice.setChecked(true);
                }
            }
        });

        checkBoxSdCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBoxSdCard.isChecked()){
                    checkBoxSdCard.setChecked(true);
                    checkBoxDevice.setChecked(false);
                    rememberWhereToSave(SD_CARD);
                }
                else{
                    checkBoxSdCard.setChecked(true);
                }
            }
        });

    }
     **/

    private void rememberWhereToSave(String whereToSave){
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_WHERE_TO_SAVE, whereToSave)
                .commit();
    }

    private void updateDatabaseStates(){
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        for (StateSynchronization stateSynchronization: statesToSynchronize) {
            databaseTraveler.updateState(stateSynchronization.getStateId(),stateSynchronization.getWhatToDo());
        }
        databaseTraveler.close();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if(statesToSynchronize.size()>0){
            intent.putExtra(SYNCHRONIZATION_STATUS,SYNCHRONIZE_TO_FALSE);
            updateDatabaseStates();
        }
        else if(statesToSynchronize.size() == 0){
            intent.putExtra(SYNCHRONIZATION_STATUS,SYNCHRONIZE_TO_TRUE);
        }
        setResult(RESULT_OK,intent);
        finish();
    }
}
