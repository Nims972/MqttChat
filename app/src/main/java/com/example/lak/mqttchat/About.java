package com.example.lak.mqttchat;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class About extends AppCompatActivity {

    SharedPreferences sharedpreferences;

    public static final String MyPREFERENCES = "Display" ;
    public static final String NameKey = "dispname";
    public static final String Passkey = "LastName";

    TextView disp;
    ImageView barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        disp=(TextView) findViewById(R.id.disp);
        barcode=(ImageView)findViewById(R.id.imageView2);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        if(sharedpreferences.contains(NameKey)){

            String s1=sharedpreferences.getString(NameKey,"");
            String s2=sharedpreferences.getString(Passkey,"");

            String s3=s1+"\n"+s2;

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(s3, BarcodeFormat.QR_CODE,900 ,900);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                barcode.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            disp.setText(s3);

        }
        else
        {
            disp.setText("Not registered");
        }
    }
    @Override
    public void onBackPressed() {
        this.finish();
    }
}
