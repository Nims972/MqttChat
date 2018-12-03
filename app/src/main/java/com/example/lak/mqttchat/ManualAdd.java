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

public class ManualAdd extends AppCompatActivity {


    EditText Name,Password;
    SharedPreferences sharedpreferences;
    Button Submit;

    final String MyPREFERENCES = "ChatPrefs" ; ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);

        Name=(EditText) findViewById(R.id.manuname);
        Password=(EditText)findViewById(R.id.manlastname);
        Submit=(Button) findViewById(R.id.mansub);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    if (Name.getText().toString().length() == 0 || Password.getText().toString().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Please Fill up the details ", Toast.LENGTH_SHORT).notify();
                    } else {
                        SharedPreferences.Editor data = sharedpreferences.edit();
                        data.putString(Name.getText().toString(), Password.getText().toString());

                        data.commit();

                        Intent I=new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(I);
                    }

                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Enter UserName and Password",Toast.LENGTH_SHORT).show();
                }

            }
        });



    }
}
