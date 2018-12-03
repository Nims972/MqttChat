package com.example.lak.mqttchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    Button RegisterButton;
    EditText Name,Password,Dispname,Lastname;
    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "UserPrefs" ;
    public static final String NameKey = "nameKey";
    public static final String Passkey = "passKey";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Name=(EditText) findViewById(R.id.Name);
        Password=(EditText) findViewById(R.id.Password);

        Dispname=(EditText) findViewById(R.id.dispname);
        Lastname=(EditText) findViewById(R.id.lastname);

        RegisterButton=(Button) findViewById(R.id.RegiterButton);


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Name.getText().toString().length() == 0 || Password.getText().toString().length() == 0 || Dispname.getText().toString().length() == 0 || Lastname.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please Fill up the details ", Toast.LENGTH_SHORT).notify();
                } else {
                    SharedPreferences.Editor data = sharedpreferences.edit();
                    data.putString(NameKey, Name.getText().toString());
                    data.putString(Passkey, Password.getText().toString());

                    data.commit();

                    sharedpreferences=getSharedPreferences("Display",Context.MODE_PRIVATE);

                    SharedPreferences.Editor data2=sharedpreferences.edit();
                    data2.putString("dispname",Dispname.getText().toString());
                    data2.putString("LastName",Lastname.getText().toString());

                    data2.commit();


                    Intent I=new Intent(getApplicationContext(),ChatList.class);
                    startActivity(I);
                }
            }

        });

    }
    @Override
    public void onBackPressed() {
        this.finish();
    }
}
