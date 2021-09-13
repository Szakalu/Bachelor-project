package math.uni.lodz.pl.jacek.traveler;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSignIn;
import math.uni.lodz.pl.jacek.traveler.database.DatabaseTraveler;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.SignInParameters;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    public static final int PLEASE_WAIT_DIALOG = 1;
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";
    private static final String DEVICE = "device";
    private static final String SD_CARD = "sd";
    private static final String PREF_WHERE_TO_SAVE = "whereToSave";


    private EditText editTextUserName;
    private EditText editTextPassword;

    private int howManyLongClicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setEditTextUserName();
        setEditTextPassword();
        setButtonSignIn();
        setTextViewRegistration();
        setPrefWhereToSave();
    }

    private void setPrefWhereToSave(){
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_WHERE_TO_SAVE,null) == null){
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                    .edit()
                    .putString(PREF_WHERE_TO_SAVE, DEVICE)
                    .commit();
        }
        else if(!checkIfSdCardIsInDevice() && pref.getString(PREF_WHERE_TO_SAVE,"").equals(SD_CARD)){
            getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                    .edit()
                    .putString(PREF_WHERE_TO_SAVE, DEVICE)
                    .commit();
        }
    }

    private boolean checkIfSdCardIsInDevice(){
        boolean isSdInDevice = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if(isSdInDevice){
            return true;
        }
        return false;
    }

    private void setEditTextUserName(){
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        editTextUserName.setHint(getString(R.string.username_hint_login_activity));
        editTextUserName.setTextSize(18);
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_USERNAME,null) != null){
            editTextUserName.setText(pref.getString(PREF_USERNAME,""));
        }
    }

    private void setEditTextPassword(){
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword.setHint(getString(R.string.password_hint_login_activity));
        editTextPassword.setTextSize(18);
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        if(pref.getString(PREF_PASSWORD,null) != null){
            editTextPassword.setText(pref.getString(PREF_PASSWORD,""));
        }
    }

    private void setButtonSignIn(){
        Button buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonSignIn.setText(getString(R.string.sign_in_button_login_activity));
        buttonSignIn.setTextSize(16);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkIfPermissionsAreGranted()) {
                    askForPermissions();
                }
                else{
                    if(checkEditTextFields(editTextUserName.getText().toString())){
                        Toast.makeText(LoginActivity.this, getString(R.string.to_short_edit_text_username_login_activity), Toast.LENGTH_SHORT).show();
                    }
                    else if(checkEditTextFields(editTextPassword.getText().toString())){
                        Toast.makeText(LoginActivity.this, getString(R.string.to_short_edit_text_password_login_activity), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(howManyLongClicks<3){
                            SignInParameters signInParameters = new SignInParameters(editTextUserName.getText().toString(),editTextPassword.getText().toString());
                            AsyncTaskSignIn.delegate = LoginActivity.this;
                            new AsyncTaskSignIn(LoginActivity.this).execute(signInParameters);

                        }
                        else{
                            Intent menuActivityIntent = new Intent(LoginActivity.this,MenuActivity.class);
                            startActivity(menuActivityIntent);
                            finish();
                        }
                    }
                }
            }
        });
    }

    private void setTextViewRegistration(){
        TextView textViewRegistration = (TextView) findViewById(R.id.textViewRegistration);
        textViewRegistration.setText(getString(R.string.sign_up_text_view_login_activity));
        textViewRegistration.setTextSize(18);
        textViewRegistration.setTextColor(Color.parseColor("#1565C0"));
        textViewRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegistrationActivity.class);
                startActivity(intent);
            }
        });
        textViewRegistration.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                howManyLongClicks++;
                return true;
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(getString(R.string.dialog_title_login_activity));
                dialog.setMessage(getString(R.string.dialog_message_login_activity));
                dialog.setCancelable(false);
                return dialog;

            default:
                break;
        }

        return null;
    }

    private boolean checkEditTextFields(String editTextText){
        if(editTextText.length() < 3){
            return true;
        }
        return false;
    }

    @Override
    public void processFinish(Integer output) {
        switch (output){
            case -1:
                Toast.makeText(this, "Something goes wrong !!!", Toast.LENGTH_SHORT).show();
                Log.i("Database","can't connect to database");
                break;
            case 1:
                Toast.makeText(this, "Welcome in user: " + editTextUserName.getText() + " !!!", Toast.LENGTH_SHORT).show();
                Log.i("Parameters","correct");
                rememberUserNameAndPassword();
                Intent menuActivityIntent = new Intent(this,MenuActivity.class);
                startActivity(menuActivityIntent);
                finish();
                break;
            case 2:
                Toast.makeText(this, "Incorrect username or password !!!", Toast.LENGTH_SHORT).show();
                Log.i("Username or password","incorrect");
                break;
        }
        Log.i("AsyncRespons",output + "");
    }

    private void askForPermissions() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void rememberUserNameAndPassword(){
        getSharedPreferences(PREFS_NAME,MODE_PRIVATE)
                .edit()
                .putString(PREF_USERNAME, editTextUserName.getText().toString())
                .putString(PREF_PASSWORD, editTextPassword.getText().toString())
                .commit();
    }

    private boolean checkIfPermissionsAreGranted(){
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        return true;
    }

}
