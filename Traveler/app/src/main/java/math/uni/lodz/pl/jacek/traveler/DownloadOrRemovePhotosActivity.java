package math.uni.lodz.pl.jacek.traveler;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskDownloadPhotos;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationPlaces;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSynchronizationStates;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.State;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;

public class DownloadOrRemovePhotosActivity extends AppCompatActivity implements AsyncResponse {

    private static final int ATTRACTIONS_TO_SYNCHRONIZATION = 1;

    public static final int PLEASE_WAIT_DIALOG_DOWNLOAD = 1;
    public static final int PLEASE_WAIT_DIALOG_REMOVE = 2;

    private final static String DOWNLOAD_OR_REMOVE = "Download_Or_Remove";
    private final static String DOWNLOAD = "Download";
    private final static String REMOVE = "Remove";

    private TableLayout tableLayoutStates;
    private ArrayList<State> arrayListStates;
    private Spinner spinnerCountry;
    public ArrayList<Integer> statesIdsToDownloadOrRemove = new ArrayList<>();
    private Button buttonDownloadOrRemove;

    private String whatToDo;
    private int countryId;
    public int downloadedOrRemovedPhotosCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_or_remove_photos);
        setWhatToDo();
        setTextViewPickRegions();
        setArrayListStates();
        setSpinnerCountry();
        setTableLayoutStates();
        setButtonDownloadOrRemove();
    }

    private void setWhatToDo(){
        Intent intent = getIntent();
        whatToDo = intent.getStringExtra(DOWNLOAD_OR_REMOVE);
    }

    private void setTextViewPickRegions(){
        TextView textViewCountry = (TextView) findViewById(R.id.textViewPickRegions);
        if(whatToDo.equals(DOWNLOAD)){
            textViewCountry.setText(getString(R.string.text_view_title_download_download_or_remove_activity));
        }
        else if(whatToDo.equals(REMOVE)){
            textViewCountry.setText(getString(R.string.text_view_title_remove_download_or_remove_activity));
        }
        textViewCountry.setTextSize(18);
        textViewCountry.setTextColor(Color.BLACK);
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
                toArrayListStates();
                setTableRowsStates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getCountryId(String countryName){
        if(countryName.equals(getStringByLocal(DownloadOrRemovePhotosActivity.this, R.string.country_poland, Locale.getDefault().getLanguage()))){
            countryName = getStringByLocal(DownloadOrRemovePhotosActivity.this, R.string.country_poland, "en");
        }
        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);
        int countryId = databaseTraveler.getCountryIdByCountryName(countryName);
        databaseTraveler.close();
        return countryId;
    }

    private String getCountryName(DatabaseTraveler databaseTraveler){
        String countryName = databaseTraveler.getCountryById(countryId).getCountryName();
        return translateCountryName(countryName);
    }

    private String translateCountryName(String countryName){
        if(countryName.equals(getStringByLocal(DownloadOrRemovePhotosActivity.this, R.string.country_poland, "en"))){
            return getStringByLocal(DownloadOrRemovePhotosActivity.this, R.string.country_poland, Locale.getDefault().getLanguage());
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

    private void setArrayListStates(){
        arrayListStates = new ArrayList<>();
    }

    private void setButtonDownloadOrRemove(){
        buttonDownloadOrRemove = (Button) findViewById(R.id.buttonDownloadOrRemove);
        if(whatToDo.equals(DOWNLOAD)){
            buttonDownloadOrRemove.setText(getString(R.string.button_download_download_or_remove_activity));
        }
        else if(whatToDo.equals(REMOVE)){
            buttonDownloadOrRemove.setText(getString(R.string.button_remove_download_or_remove_activity));
        }
        buttonDownloadOrRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(statesIdsToDownloadOrRemove.size()>0){
                    if(whatToDo.equals(DOWNLOAD)){
                        AsyncTaskDownloadPhotos.delegate = DownloadOrRemovePhotosActivity.this;
                        new AsyncTaskDownloadPhotos(DownloadOrRemovePhotosActivity.this).execute();
                    }
                    else if(whatToDo.equals(REMOVE)){

                    }
                }
            }
        });
    }

    private void toArrayListStates(){
        arrayListStates.clear();

        DatabaseTraveler databaseTraveler = new DatabaseTraveler(this);

        Cursor cursorStates = databaseTraveler.getStatesAscWhereCountryIdEquals(countryId);
        while(cursorStates.moveToNext()){
            if(cursorStates.getInt(3) == ATTRACTIONS_TO_SYNCHRONIZATION){
                State state = new State(cursorStates.getInt(0), cursorStates.getString(1), countryId, cursorStates.getInt(3), 0);
                arrayListStates.add(state);
            }
        }
        databaseTraveler.close();
    }

    private void setTableRowsStates(){
        tableLayoutStates.removeAllViews();

        for (State state: arrayListStates) {
            final TableRow tableRow = new TableRow(this);

            tableRow.setBackgroundColor(Color.WHITE);
            tableRow.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            CheckBox checkBoxState = setCheckBoxState(state);

            tableRow.addView(checkBoxState);

            tableLayoutStates.addView(tableRow);
        }
    }

    private CheckBox setCheckBoxState(final State state){
        CheckBox checkBoxState = new CheckBox(this);
        checkBoxState.setBackgroundColor(Color.WHITE);
        checkBoxState.setText(state.getStateName());
        checkBoxState.setTextColor(Color.BLACK);
        checkBoxState.setTextSize(20);

        if(checkIfStateIsOnList(state.getId())){
            checkBoxState.setChecked(true);
        }

        checkBoxState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    statesIdsToDownloadOrRemove.add(state.getId());
                }
                else{
                    removeStateFromStatesIdsToDownloadOrRemove(state.getId());
                }
                showAllStatesList();
            }
        });
        return checkBoxState;
    }

    private void showAllStatesList(){
        for (Integer stateID: statesIdsToDownloadOrRemove) {
            Log.i("StateId",stateID + "");
        }
        Log.i("StateId","-----------------------------");
    }

    private boolean checkIfStateIsOnList(int stateId){
        for (Integer stateIdOnList: statesIdsToDownloadOrRemove) {
            if(stateIdOnList == stateId){
                return true;
            }
        }
        return false;
    }

    private void removeStateFromStatesIdsToDownloadOrRemove(int stateId){
        for(int index = 0; index < statesIdsToDownloadOrRemove.size();++index) {
            if(statesIdsToDownloadOrRemove.get(index) == stateId){
                statesIdsToDownloadOrRemove.remove(index);
                break;
            }
        }
    }

    @Override
    public void processFinish(Integer output) {
        switch (output) {
            case -2:
                Toast.makeText(this, getString(R.string.toast_something_goes_wrong_all_activity), Toast.LENGTH_SHORT).show();
                break;
            case -1:
                Toast.makeText(this, getString(R.string.toast_something_goes_wrong_all_activity), Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, getString(R.string.toast_download_download_or_remove_activity) + downloadedOrRemovedPhotosCount, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case 2:
                Toast.makeText(this, getString(R.string.toast_remove_download_or_remove_activity) + downloadedOrRemovedPhotosCount, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG_DOWNLOAD:
                ProgressDialog dialogCountry = new ProgressDialog(this);
                dialogCountry.setTitle(getString(R.string.wait_dialog_download_download_or_remove_activity));
                dialogCountry.setMessage(getString(R.string.wait_dialog_please_wait_download_or_remove_activity));
                dialogCountry.setCancelable(false);
                return dialogCountry;

            case PLEASE_WAIT_DIALOG_REMOVE:
                ProgressDialog dialogState = new ProgressDialog(this);
                dialogState.setTitle(getString(R.string.wait_dialog_remove_download_or_remove_activity));
                dialogState.setMessage(getString(R.string.wait_dialog_please_wait_download_or_remove_activity));
                dialogState.setCancelable(false);
                return dialogState;

            default:
                break;
        }

        return null;
    }
}
