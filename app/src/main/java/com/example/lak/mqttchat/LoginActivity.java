package com.example.lak.mqttchat;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.InvalidKeyException;

public class LoginActivity extends AppCompatActivity {

    Button LoginButton;
    EditText Name, Password;
    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "UserPrefs";
    public static final String NameKey = "nameKey";
    public static final String Passkey = "passKey";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginButton = (Button) findViewById(R.id.Loginbutton);
        Name = (EditText) findViewById(R.id.Loginname);
        Password = (EditText) findViewById(R.id.Loginpassword);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedpreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String name = sharedpreferences.getString("nameKey", "");
                String pass = sharedpreferences.getString("passKey", "");

                if (Name.getText().toString().length() == 0 && Password.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Enter User Name and Password", Toast.LENGTH_SHORT).show();
                } else {

                    if (Name.getText().toString().equals(name) && Password.getText().toString().equals(pass)) {
                        Intent intent = new Intent(getApplicationContext(), ChatList.class);
                        kill();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }

    public void kill()
    {
        this.finish();
    }

}
