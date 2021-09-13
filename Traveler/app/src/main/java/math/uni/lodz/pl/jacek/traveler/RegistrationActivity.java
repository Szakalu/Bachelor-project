package math.uni.lodz.pl.jacek.traveler;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import math.uni.lodz.pl.jacek.traveler.interfaces.AsyncResponse;
import math.uni.lodz.pl.jacek.traveler.AsyncTasks.AsyncTaskSignUp;
import math.uni.lodz.pl.jacek.traveler.ObjectsInDatabase.SignUpParameters;

public class RegistrationActivity extends AppCompatActivity implements AsyncResponse{

    public static final int PLEASE_WAIT_DIALOG = 1;

    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextPasswordAgain;
    EditText editTextMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setTextViewUsername();
        setTextViewPassword();
        setTextViewPasswordAgain();
        setTextViewMail();
        setEditTextFields();
        setButtonRegistration();
    }

    private void setTextViewUsername(){
        TextView textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        textViewUsername.setText(getString(R.string.username_text_view_registration_activity));
        textViewUsername.setTextSize(18);
    }

    private void setTextViewPassword(){
        TextView textViewPassword = (TextView) findViewById(R.id.textViewPassword);
        textViewPassword.setText(getString(R.string.password_text_view_registration_activity));
        textViewPassword.setTextSize(18);
    }

    private void setTextViewPasswordAgain(){
        TextView textViewPasswordAgain = (TextView) findViewById(R.id.textViewPasswordAgain);
        textViewPasswordAgain.setText(R.string.password_again_text_view_registration_activity);
        textViewPasswordAgain.setTextSize(18);
    }

    private void setTextViewMail(){
        TextView textViewMail = (TextView) findViewById(R.id.textViewMail);
        textViewMail.setText(getString(R.string.mail_text_view_registration_activity));
        textViewMail.setTextSize(18);
    }

    private void setEditTextFields(){
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPasswordAgain = (EditText) findViewById(R.id.editTextPasswordAgain);
        editTextMail = (EditText) findViewById(R.id.editTextMail);
    }

    private void setButtonRegistration(){
        Button buttonRegistration = (Button) findViewById(R.id.buttonRegistration);
        buttonRegistration.setText(getString(R.string.registration_button_registration_activity));

        buttonRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkIfDataAreValid()){
                    SignUpParameters signUpParameters = new SignUpParameters(editTextUsername.getText().toString()
                            ,editTextPassword.getText().toString(),editTextMail.getText().toString());
                    AsyncTaskSignUp.delegate = RegistrationActivity.this;
                    new AsyncTaskSignUp(RegistrationActivity.this).execute(signUpParameters);
                }
            }
        });
    }

    private boolean checkIfDataAreValid(){
        if(!validUsername(editTextUsername.getText().toString())){
            Toast.makeText(RegistrationActivity.this, "Not valid username !!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!validPassword(editTextPassword.getText().toString())){
            Toast.makeText(RegistrationActivity.this, "Not valid password !!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!editTextPassword.getText().toString().equals(editTextPasswordAgain.getText().toString())){
            Toast.makeText(RegistrationActivity.this, "Passwords are different !!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!validMail(editTextMail.getText().toString())){
            Toast.makeText(RegistrationActivity.this, "Not valid mail !!!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validMail(String email){
        if(email.length()>0){
            Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
            Matcher mat = pattern.matcher(email);

            if(mat.matches()){
                return true;
            }else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    private boolean validUsername(String username){
        if(username.length()>2){
            Pattern pattern = Pattern.compile("[A-Za-z0-9_]+");
            Matcher mat = pattern.matcher(username);

            if(mat.matches()){
                return true;
            }else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    private boolean validPassword(String password){
        if(password.length() > 2) {
            Pattern pattern = Pattern.compile("[A-Za-z0-9_]+");
            Matcher mat = pattern.matcher(password);

            if (mat.matches()) {
                return true;
            } else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PLEASE_WAIT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(getString(R.string.dialog_title_login_activity));
                dialog.setMessage(getString(R.string.dialog_message_login_activity));
                dialog.setCancelable(true);
                return dialog;

            default:
                break;
        }

        return null;
    }

    @Override
    public void processFinish(Integer output) {
        switch (output){
            case -1:
                Toast.makeText(this, "Something goes wrong !!!", Toast.LENGTH_SHORT).show();
                Log.i("Database","can't connect to database");
                break;
            case 1:
                Toast.makeText(this, "Registration is done, try to sign in !!!", Toast.LENGTH_SHORT).show();
                Log.i("Registration","good");
                finish();
                break;
            case 2:
                Toast.makeText(this, "Username exist, use other !!!", Toast.LENGTH_SHORT).show();
                Log.i("Username","exist");
                break;

            case 3:
                Toast.makeText(this, "Mail exist, use other !!!", Toast.LENGTH_SHORT).show();
                Log.i("Mail","exist");
                break;
        }
        Log.i("result",output + "");
    }
}
