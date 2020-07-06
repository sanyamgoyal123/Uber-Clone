package com.app.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        if (edtDriverOrPassenger.getText().toString().equals("Driver") || edtDriverOrPassenger.getText().toString().equals("Passenger")) {
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            Toast.makeText(MainActivity.this, "We have an Anonymous user", Toast.LENGTH_SHORT).show();

                            user.put("as", edtDriverOrPassenger.getText().toString());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null) {
                                        transitionToPassengerActivity();
                                        transistionToDriverRequestListActivity();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Toast.makeText(MainActivity.this, "Specify the details", Toast.LENGTH_LONG).show();
        }
    }

    enum State {
        SIGNUP , LOGIN
    }

    private State state;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;
    private RadioButton rdbPassenger, rdbDriver;
    private Button btnSignUpLogIn, btnOneTimeLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(ParseUser.getCurrentUser() != null) {
            transitionToPassengerActivity();
            transistionToDriverRequestListActivity();
        }

        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDriverOrPassenger);

        rdbPassenger = findViewById(R.id.rdbPassenger);
        rdbDriver = findViewById(R.id.rdbDriver);

        btnSignUpLogIn = findViewById(R.id.btnSignUpLogIn);
        btnOneTimeLogIn = findViewById(R.id.btnOneTimeLogIn);

        state = State.SIGNUP;

        btnOneTimeLogIn.setOnClickListener(this);
        btnSignUpLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.SIGNUP) {
                    if(rdbDriver.isChecked() == false && rdbPassenger.isChecked() == false) {
                        Toast.makeText(MainActivity.this, "Check either of the CheckBoxs", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if(rdbDriver.isChecked()) {
                        appUser.put("as", "Driver");
                    } else if(rdbPassenger.isChecked()) {
                        appUser.put("as", "Passenger");
                    }
                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Toast.makeText(MainActivity.this, "SignedUp Successfully", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transistionToDriverRequestListActivity();
                            }
                        }
                    });
                } else if (state == State.LOGIN) {
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                Toast.makeText(MainActivity.this, "User Logged In Sucessfully", Toast.LENGTH_SHORT).show();
                                transitionToPassengerActivity();
                                transistionToDriverRequestListActivity();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.loginItem:
                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogIn.setText("Log In");
                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogIn.setText("Sign Up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity() {
        if(ParseUser.getCurrentUser() != null) {
            if( ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void transistionToDriverRequestListActivity() {

        if (ParseUser.getCurrentUser() != null) {
            if( ParseUser.getCurrentUser().get("as").equals("Driver")) {
                Intent intent = new Intent(MainActivity.this, DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }
}