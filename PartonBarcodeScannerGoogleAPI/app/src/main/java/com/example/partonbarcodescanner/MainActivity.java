package com.example.partonbarcodescanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    //Socket stuff
    TextView e1;                    //Barcodes
    EditText e2;                    //Zone
    TextView tt;                    //Total text
    private static Socket s;        //Create socket
    private static PrintWriter printWriter;

    private String m_Text = "";  //For adding manual barcodes
    String message = "";         //Message to be sent
    private static String ip;    //Default server IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Bring up barcodes and zone
        displayZoneBarcodes();
    }

    @Override
    protected void onResume(){
        super.onResume();
        displayZoneBarcodes();
    }

    //Switch to the scanner activity
    public void scanBarcode(View v) {
        saveZone();
        Intent intent = new Intent(this, Scanner.class);
        startActivity(intent);
    }

    //Saves the zone to the sharedprefs
    public void saveZone(){
        //Save to zone number to bring up later
        String zoneString = e2.getText().toString();
        SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
        editor.putString("ZONE", zoneString);
        editor.apply();
    }

    //Gets and displays zone and barcode values
    public void displayZoneBarcodes(){
        e1 = (TextView) findViewById(R.id.barcodesText);
        e2 = (EditText) findViewById(R.id.zoneText);
        tt = (TextView) findViewById(R.id.totalText);
        e2.setText(getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("ZONE", "420"));
        e1.setText(getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("BARCODES", ""));
        setTotalCount(e1.getText().toString());
        if(e1.getText().toString() == "" || e1.getText().toString().isEmpty()) e1.setText("Aquí aparecerán los códigos escaneados");
    }

    //Switch to settings
    public void switchSettings(View v){
        //Save the zone before the snap
        saveZone();
        //Switch to the settings
        startActivity(new Intent(MainActivity.this, Settings.class));
    }

    /****************Socket stuff****************/
    //Set the message text and run the execute method to send data
    public void send_text(View v) {
        ip = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("SERVERIP", "").trim();   //Get IP from sharedprefs if unchanged
        final String sellerID = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("SELLERID", "");
        message = e1.getText().toString().trim();                  //Get the barcodes
        final String zone = e2.getText().toString().trim();
        //Validate data
        if(validateData(ip, sellerID, message, zone) == 0){
            return;
        }

        //Confirmation and send
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Ya hablando en serio")
                .setMessage("¿Estás seguro?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String lines[] = message.split("\\r?\\n");   //Split them by new lines
                        int numberOfThings = lines.length;
                        message = sellerID.trim() + "|";
                        for (int i = 0; i < lines.length; i++){
                            message = message + lines[i].trim() + "|";             //Join them by a | delimeter
                        }
                        message = message +  zone;   //Add the zone

                        myTask mt = new myTask(message);    //Send the task to myTask to be sent to the server
                        mt.execute();

                        //Write copy of file locally
                        try {
                            String messageFormatted = "";
                            //Format message so that the txt is in the correct format
                            String[] messageArray = message.split("\\|");	//Split codes by this delimiter
                            for(int i = 1; i < messageArray.length - 1; i++) {	//Omit the last character since it's the zone
                               messageFormatted += messageArray[i] + "00000001\n";
                            }

                            writeFileOnInternalStorage(MainActivity.this, "z" + zone + " - " + sellerID + ".txt", messageFormatted);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Clear the old barcodes from the sharedprefs and the textview
                        e1.setText("Aquí aparecerán los códigos escaneados");
                        //Get the shared prefs
                        SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
                        //And erase them
                        editor.putString("BARCODES", "");
                        editor.apply();

                        //Clear the old zone
                        e2.setText("");
                        editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
                        editor.putString("ZONE", "");
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "¡" + numberOfThings + " códigos enviados!", Toast.LENGTH_LONG).show();
                        tt.setText("Total: 0");
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //Check data is right
    public int validateData(String ip, String sellerID, String message, String zone){
        if(ip == "" || ip.isEmpty()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("No se envió")
                    .setMessage("Ingresa el IP del servidor")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
                    return 0;
        } else if (sellerID == "" || sellerID.isEmpty()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("No se envió")
                    .setMessage("Ingresa tu número de vendedor")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return 0;
        } else if (zone == "" || zone.isEmpty()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("No se envió")
                    .setMessage("Ingresa la zona")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return 0;
        } else if (message == "Aquí aparecerán los códigos escaneados" || message == "" || message.isEmpty()){
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("No se envió")
                    .setMessage("Esta aplicación es para escanear, escanea algo!")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return 0;
        }
        return 1;
    }

    //A task for sending data
    class myTask extends AsyncTask<Void, Void, Void> {
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

    /****************End of socket stuff****************/

    //Write backup file locally
    public void writeFileOnInternalStorage(Context mcoContext,String sFileName, String sBody) throws IOException {
        // Get the directory for the user's public pictures directory.
        //File file = new File(getExternalFilesDir(null), sFileName);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + sFileName);
        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(sBody);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Append to the barcode sharedpref barcode string and also returns it, also set the total
    private String setAndReturnBarcodeSharedPref(String scanResult){
        e2.setText(getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("ZONE", ""));   //Set the zone for no real reason
        String currentBarcodes = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("BARCODES", "");
        currentBarcodes = currentBarcodes + scanResult + "\n";      //Append the new barcode
        //Save the new string
        SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
        editor.putString("BARCODES", currentBarcodes);
        editor.apply();
        setTotalCount(currentBarcodes);
        return currentBarcodes;
    }

    //Set the total number of scanned codes so far
    private void setTotalCount (String currentBarcodes){
        Log.i("Yeah", currentBarcodes);
        String[] lines = currentBarcodes.split("\r\n|\r|\n");
        tt.setText("Total: " + (lines.length));
    }

    //Thanks StackOverflow
    //Add a barcode manually
    public void addManually(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingresar código");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if (m_Text.length() < 13) {
                    m_Text = "0" + m_Text;
                }
                e1.setText(setAndReturnBarcodeSharedPref(m_Text));
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //Clears all barcodes
    public void clearBarcodes (View v){
        e1.setText("Aquí aparecerán los códigos escaneados");
        //Get the shared prefs
        SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
        //And erase them
        editor.putString("BARCODES", "");
        editor.apply();
        tt.setText("Total: 0");
    }

    //Clears on barcode
    public void clearOneBarcode(View v){
        String currentBarcodes = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).getString("BARCODES", "");
        if(currentBarcodes.lastIndexOf('\n') > 0){  //If there's a new line character
            //If it's the last character and there's more than one code
            if(currentBarcodes.lastIndexOf('\n') == currentBarcodes.length() - 1){
                try {
                    currentBarcodes = currentBarcodes.substring(0, currentBarcodes.lastIndexOf('\n'));           //Erase the first new line
                    currentBarcodes = currentBarcodes.substring(0, currentBarcodes.lastIndexOf('\n')) + '\n';    //Erase a the next new line and add a new space
                } catch (Exception e){
                    clearBarcodes(v);
                }
            } else {
                currentBarcodes = currentBarcodes.substring(0,currentBarcodes.lastIndexOf('\n')) + '\n';    //Erase up to it and add a new space
            }
            e1.setText(currentBarcodes);
            //Save the new string
            SharedPreferences.Editor editor = getSharedPreferences("BARCODESPREFS", MODE_PRIVATE).edit();
            editor.putString("BARCODES", currentBarcodes);
            editor.apply();
            setTotalCount(currentBarcodes);
        } else {
            clearBarcodes(v);
        }
    }
}