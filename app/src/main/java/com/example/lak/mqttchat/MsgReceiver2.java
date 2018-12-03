package com.example.lak.mqttchat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Surface;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MsgReceiver2 extends IntentService {


    static boolean instance;

    SharedPreferences sharedPreferences, sharedPreferences2;

    static final List<String> topics = new ArrayList<>();

    public static MqttAndroidClient client;

    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private ImageReader mImageReader;
    private HandlerThread mCheckThread;
    private Handler mCheckHandler;


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.lak.barcodetest.action.FOO";
    private static final String ACTION_BAZ = "com.example.lak.barcodetest.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.lak.barcodetest.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.lak.barcodetest.extra.PARAM2";

    public MsgReceiver2() {

        super("MsgReceiver2");
        instance=true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("msgS","Service Started");
        startForeground(1,new Notification());


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.setPriority(100);
        NetworkChangeReciever networkChangeReceiver= new NetworkChangeReciever();
        registerReceiver(networkChangeReceiver,intentFilter);
        sharedPreferences = getSharedPreferences("ChatPrefs", MODE_PRIVATE);
        sharedPreferences2 = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String Uname = sharedPreferences2.getString("nameKey", "");

        List<String> values = new ArrayList<>();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            values.add(entry.getKey().toString());
            topics.add(entry.getKey().toString() + Uname);
            System.out.println(entry.getKey().toString() + Uname);
        }
        try {
            client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", ChatList.clientId);
            //connectt();
            //Log.d("msgS", "Outside ");
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //client = new MqttAndroidClient(getApplication().getApplicationContext(), "tcp://broker.hivemq.com:1883", MainActivity.clientId);
                    Log.d("msgS", "Connection Lost");
                    connectt();
                }

                //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    //Toast.makeText(getApplicationContext(), message + "Service".toString(), Toast.LENGTH_SHORT).show();

                    Log.d("msgS", "Recieved");
                    //Log.d("msgS", "Calling Reply");

                    String dmsg=message.toString();
                    if(dmsg.equals("ACKFORTESTONLINE"))
                    {
                        Log.d("msgS", "Calling Reply");
                        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

                        String temp = sharedPreferences.getString("nameKey", "");

                        MessagesActivity.reply(topic,temp,getApplicationContext());
                    }
                    else if(dmsg.equals("REPLYFORTESTONLINE")) {

                        //Do nothing
                    }
                    else{

                        try {
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(topic + ".txt", Context.MODE_APPEND));
                            outputStreamWriter.append("Rcv:" + message.toString() + "\n");
                            outputStreamWriter.close();
                        } catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                        }
                        String ret = "";

                        try {
                            InputStream inputStream = getApplicationContext().openFileInput(topic + ".txt");

                            if (inputStream != null) {
                                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String receiveString = "";
                                StringBuilder stringBuilder = new StringBuilder();

                                while ((receiveString = bufferedReader.readLine()) != null) {
                                    stringBuilder.append(receiveString + "\n");
                                }

                                inputStream.close();
                                ret = stringBuilder.toString();
                                Log.d("msgS", "Read " + ret);
                            }
                        } catch (FileNotFoundException e) {
                            Log.e("login activity", "File not found: " + e.toString());
                        } catch (IOException e) {
                            Log.e("login activity", "Can not read file: " + e.toString());
                        }

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
                            mBuilder.setVibrate(new long[]{1000, 1000});
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

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("notify", "msg reached");
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        onHandleIntent(intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.d("msgS","Service Stopped");
        instance=false;
        super.onDestroy();

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
    static  public void connectt(){

        Log.d("msgS","Connectt method");
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("msgS","Connected"+this.toString());

                    int qos = 1;
                    try {
                        for(int i=0;i<topics.size();i++) {
                            IMqttToken subToken;
                            subToken = client.subscribe(topics.get(i), qos);
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
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.d("msg", "onSuccess HEllo");

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("msgS", "onFailure");
                }
            });
        }
        catch (MqttException e) {
            e.printStackTrace();
            System.out.println("Here");
        }

    }
}
