/*

Code by Eric Parton

 */

package com.example.partonbarcodescanner;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

//Most of this code is adapted from the Google Vision API example
public class Scanner extends AppCompatActivity {
    SurfaceView cameraView;
    BarcodeDetector barcode;
    CameraSource cameraSource;
    SurfaceHolder holder;
    TextView scannedCode;
    MediaPlayer mp;
    Button save;
    Button erase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        //Get the back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Disable the save button until something is scanned
        save = (Button) findViewById(R.id.saveCode);
        erase = (Button) findViewById(R.id.eraseCode);
        save.setBackgroundColor(Color.LTGRAY);
        erase.setBackgroundColor(Color.LTGRAY);
        save.setEnabled(false);
        erase.setEnabled(false);

        //Get the beep sound
        mp = MediaPlayer.create(Scanner.this, R.raw.beep);

        scannedCode = (TextView) findViewById(R.id.scannedThingy);

        //A bunch of code that is from the tutorial
        cameraView = (SurfaceView) findViewById(R.id.camera_preview);
        cameraView.setZOrderMediaOverlay(true);
        holder = cameraView.getHolder();
        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.UPC_A | Barcode.UPC_E | Barcode.EAN_8 | Barcode.EAN_13 | Barcode.CODE_128)   //Limit the scanner to only scan these codes
                .build();
        if (!barcode.isOperational()) {
            Toast.makeText(getApplicationContext(), "Error setting up detector", Toast.LENGTH_LONG).show();        //Pray this never happens
        }


        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setRequestedPreviewSize(500, 500)  //Not sure what this does
                .setAutoFocusEnabled(true)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {      //Check the permission on creating the camera surface
                try {
                    if (ActivityCompat.checkSelfPermission(Scanner.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Not sure in what context this is used
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            //When the camera is closed
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();    //Important, without this line the camera wouldn't start again
            }
        });

        //This gets fancy with threads
        barcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }
            String barcodeString;

            //When a barcode is scanned
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0) {
                    barcodeString = barcodes.valueAt(0).displayValue;   //Get the scanned barcode
                    scannedCode.post(new Runnable(){                          //Basically telling the program to run this on the main thread
                        @Override
                        public void run(){
                            //Do some UX stuff
                            if(save.isEnabled()){

                            } else {
                                scannedCode.setText(barcodeString);
                                save.setBackgroundColor(Color.GREEN);
                                save.setEnabled(true);  //Enable the button
                                erase.setEnabled(true);
                                mp.start();             //Play the beep sound
                            }
                        }
                    });
                    //Wait a second between scans
                    try {
                        TimeUnit.MILLISECONDS.sleep(2000);  //Originally a second, slightly more to give the beep.mp3 time some leeway and to give users some time to find a new code
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //This makes the back button work
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    //Take a guess
    public void ignoreBarcode(View v){
        save.setEnabled(false);     //Disable the button until the next scan
        erase.setEnabled(false);
        save.setBackgroundColor(Color.LTGRAY);
        scannedCode.setText("The barcode will appear here");
    }

    //When the save button is pressed, save the barcode to shared prefs and get ready for a new scan
    public void saveBarcode(View v) throws IOException {
        save.setEnabled(false);     //Disable the button until the next scan
        erase.setEnabled(false);
        String scanPreResult = scannedCode.getText().toString();


        //Add leading zero if ignored
        //if (scanPreResult.length() < 13) {
        //    scanPreResult = "0" + scanPreResult;
        //}
        //Add scan result to shared pref
        final String scanResult = scanPreResult;
        setAndReturnBarcodeSharedPref(scanResult);

        save.setBackgroundColor(Color.LTGRAY);
        scannedCode.setText("The barcode will appear here");
    }

    //Append to the barcode sharedpref barcode string and also returns it
    private String setAndReturnBarcodeSharedPref(String scanResult){
        String currentBarcodes = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("BARCODES", "");
        currentBarcodes = currentBarcodes + scanResult;      //Append the new barcode
        currentBarcodes = currentBarcodes.replaceAll("(?m)^[ \t]*\r?\n", "");   //Get rid of any blank new lines
        currentBarcodes = currentBarcodes + "\n";       //Add a new line at the end
        //Save the new string
        SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
        editor.putString("BARCODES", currentBarcodes);
        editor.apply();
        Log.i("Yeah2", currentBarcodes);
        return currentBarcodes;
    }
}
