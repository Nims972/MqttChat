package com.example.lak.mqttchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.journeyapps.barcodescanner.CaptureActivity;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatList extends AppCompatActivity {

    ListView Chats;
    SharedPreferences sharedpreferences;

    static String clientId = MqttClient.generateClientId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("msgS","chatlist");

        displayChats();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                Intent intent = new Intent(getApplicationContext(),CaptureActivity.class);
                intent.setAction("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE",  "QR_CODE_MODE");
                startActivity(intent);startActivityForResult(intent, 13);


            }
        });
    }


    public void displayChats(){
        Chats=(ListView) findViewById(R.id.Chats);

        List<String> values = new ArrayList<>();

        List<String> pass=new ArrayList<>();

        sharedpreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE);


        Map<String, ?> allEntries = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            values.add(entry.getKey().toString());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, values);

        Chats.setAdapter(arrayAdapter);


        Chats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                         public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                             String text = arrayAdapter.getItem(position);
                                             Intent I=new Intent(getApplicationContext(),MessagesActivity.class);
                                             I.putExtra("ChatName",text);
                                             startActivity(I);

                                         }
                                     }
        );

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 13) {
            if (resultCode == RESULT_OK) {
                sharedpreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
                String contents = data.getStringExtra("SCAN_RESULT");
                String s[]=contents.split("\n",-1);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(s[0],s[1]);
                Log.d("msgS","Name"+s[0]+"Lastname"+s[1]);
                editor.commit();
                System.out.println(sharedpreferences.getAll());
                displayChats();
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("Failed");
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menubutton,menu);
        getMenuInflater().inflate(R.menu.menubutton2,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.settings:
                Intent I=new Intent(this,About.class);
                startActivityForResult(I,86);
                break;
            case R.id.manual:
                Intent intent=new Intent(this,ManualAdd.class);
                startActivityForResult(intent,86);
                break;
        }
        return true;
    }

}
