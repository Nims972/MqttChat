package com.example.lak.mqttchat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.example.lak.mqttchat.MsgReceiver2.client;

public class MessagesActivity extends AppCompatActivity {

    String clientId = MqttClient.generateClientId();

    SharedPreferences sharedPreferences;

    Button publish;
    EditText payload;
    ListView msgList;
    boolean flag = true;
    ArrayList<String> msgArray = new ArrayList<>();
    ArrayAdapter adapter;

    static boolean check;

    String ourname;

    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_messages);


            check=false;

            payload = (EditText) findViewById(R.id.payload);
            publish = (Button) findViewById(R.id.publish);
            publish.setText("Connect");

            sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

            final String temp = sharedPreferences.getString("nameKey", "");

            ourname=temp;

            String pubstopic = temp;

            Bundle extras = getIntent().getExtras();
            String Uname = extras.getString("ChatName").toString();

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            toolbar.setTitle(Uname);

            String substopic = Uname;

            substopic += temp;

            pubstopic += Uname;

            final String topic = substopic;

            filename=topic;

            final String subs = topic;
            final String topic2 = pubstopic;

            try {

                if (check == false) {
                    check(topic2,getApplicationContext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            // Toast.makeText(getApplicationContext(),topic,Toast.LENGTH_SHORT).show();
            setTitle(Uname);
            String ret;
            try {
                InputStream inputStream = getApplicationContext().openFileInput(topic + ".txt");

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        msgArray.add(receiveString);
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    ret = stringBuilder.toString();
                    Log.d("msgS", "Read in Activity " + ret);
                }
            } catch (FileNotFoundException e) {
                Log.e("login activity", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
            adapter = new ArrayAdapter<String>(this, R.layout.msg_listview, msgArray);

            final ListView listView = (ListView) findViewById(R.id.chatlist);
            listView.setAdapter(adapter);

            try {

                final MqttAndroidClient client =
                        new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", ChatList.clientId);
                try {
                    IMqttToken token = client.connect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            int qos = 2;
                            try {
                                IMqttToken subToken;
                                subToken = client.subscribe(topic, qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        Log.i("conn", "subscribed");
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        Log.i("conn", "failed to subscribe");
                                    }
                                });
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                            Log.d("msg", "onSuccess");
                            //Toast.makeText(getApplicationContext(),"Connection Succesfull",Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Log.d("msg", "onFailure");


                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                    System.out.println("Here");
                }


                publish.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (check == true) {
                            if(payload.getText().toString().length()!=0) {
                                String msg = payload.getText().toString();
                                // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                flag = false;
                                byte[] encodedPayload = new byte[0];
                                try {
                                    encodedPayload = msg.getBytes("UTF-8");
                                    MqttMessage message = new MqttMessage(encodedPayload);
                                    client.publish(topic2, message);
                                    payload.setText("");
                                    // Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
                                    try {
                                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(topic + ".txt", Context.MODE_APPEND));
                                        outputStreamWriter.append("Send:" + message.toString() + "\n");
                                        outputStreamWriter.close();
                                    } catch (IOException e) {
                                        Log.e("Exception", "File write failed: " + e.toString());
                                    }
                                    msgArray.add("Send:" + message.toString());
                                    adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.msg_listview, msgArray);
                                    listView.setAdapter(adapter);


                                } catch (UnsupportedEncodingException | MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            check(topic2,getApplicationContext());
                            Toast.makeText(getApplicationContext(), "Opposite Side Not Connected Yet Retrying....", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.i("error", "connection loast");
                    }

                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //Toast.makeText(getApplicationContext(),message.toString(),Toast.LENGTH_SHORT).show();
                        String dmsg=message.toString();
                        //check=true;
                        if (topic.equals(subs)) {
                            if (dmsg.equals("ACKFORTESTONLINE")) {
                                reply(topic, temp,getApplicationContext());
                            } else if (dmsg.equals("REPLYFORTESTONLINE")) {
                                check = true;
                                publish.setText("Send");
                                //Toast.makeText(getApplicationContext(),"Connected Press Send To deliver message",Toast.LENGTH_SHORT).show();
                            } else {
                                msgArray.add("Rcv:" + dmsg);
                                //Toast.makeText(getApplicationContext(),message.toString()+"Screen",Toast.LENGTH_SHORT).show();
                                adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.msg_listview, msgArray);
                                listView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                                listView.setAdapter(adapter);


                                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "SMILL")
                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                        .setContentTitle("MQTT CHAT")
                                        .setContentText("New Messages")
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .setVibrate(new long[]{1000, 1000});


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    CharSequence name = "SmartIll";
                                    String description = "Notification for smart illumination";
                                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                                    NotificationChannel channel = new NotificationChannel("SMILL", name, importance);
                                    channel.setDescription(description);
                                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                                    notificationManager.createNotificationChannel(channel);
                                    notificationManager.notify(13, mBuilder.build());

                                } else {

                                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    notificationManager.notify(13, mBuilder.build());


                                }
                            }
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.i("notify", "msg reached");
                    }
                });
            } catch (Exception e) {
                Log.i("error", "connection failed");
                e.printStackTrace();
                System.out.println("Here Outside");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void check(String subs,Context context) {
        String msg = "ACKFORTESTONLINE";
        Log.d("msgS","Check Message"+msg);
        try {
            byte[] encodedPayload = msg.getBytes("UTF-8");
            MqttMessage ack = new MqttMessage(encodedPayload);
            Log.d("msgS","Checking"+ack);
            client.publish(subs, ack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reply(String topic,String temp,Context context) {
        String msg = "REPLYFORTESTONLINE";
        Log.d("msgS", "Reply Method"+topic+" "+temp);
        String pubtopic=temp+topic.replace(temp,"");
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = msg.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(pubtopic, message);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.clear,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.clear:
                try {
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(filename + ".txt",Context.MODE_PRIVATE));
                    outputStreamWriter.append("");
                    outputStreamWriter.close();
                    ArrayList<String> msg=new ArrayList<>();
                    msg.add("");
                    adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.msg_listview, msg);
                    ListView listView = (ListView) findViewById(R.id.chatlist);
                    listView.setAdapter(adapter);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                break;
        }
        return true;
    }
}

