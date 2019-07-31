/*

Code by Eric Parton

 */

package com.example.partonbarcodescanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Settings extends AppCompatActivity {
    EditText e1;
    EditText e2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        e1 = (EditText) findViewById(R.id.ipEditText);
        e2 = (EditText) findViewById(R.id.sellerEditText);

        //Bring up the old settings if available
        e1.setText(getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("SERVERIP", ""));
        e2.setText(getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("SELLERID", ""));

        //Save
        Button clickButton = (Button) findViewById(R.id.saveBtn);
        clickButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {

                SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
                editor.putString("SERVERIP", e1.getText().toString());
                editor.putString("SELLERID", e2.getText().toString().toUpperCase());
                editor.apply();

                try {
                    Toast toast = Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_SHORT);
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(Settings.this, MainActivity.class));
                //setContentView(R.layout.activity_main);
                //Big bug was happening, this code basically restarts the whole app lol
                Intent mStartActivity = new Intent(Settings.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(Settings.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)Settings.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });

        //Cancel
        Button clickButton2 = (Button) findViewById(R.id.cancelBtn);    //Descriptive variable names
        clickButton2.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                //Big bug was happening, this code basically restarts the whole app lol
                Intent mStartActivity = new Intent(Settings.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(Settings.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)Settings.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });
    }

    public String ip;
    //A task for sending data
    class myTask extends AsyncTask<Void, Void, Void> {
        //Socket stuff
        private Socket s;        //Create socket
        private PrintWriter printWriter;
        private String stringToSend;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                s = new Socket(ip, 5000);                      //Set up a matching port and IP as server
                printWriter = new PrintWriter(s.getOutputStream()); //Set up a data stream
                printWriter.write(stringToSend);                    //Send the message
                printWriter.flush();                                //flush the printer
                printWriter.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        public myTask(String m){
            stringToSend = m;
        }
    }

    public void performTest(View v){
        EditText ipET = (EditText) findViewById(R.id.ipEditText);
        EditText guyObj = (EditText) findViewById(R.id.sellerEditText);
        String guy = guyObj.getText().toString();
        ip = ipET.getText().toString();
        Settings.myTask mt = new Settings.myTask(guy);    //Send the task to myTask to be sent to the server
        mt.execute();
    }
}
