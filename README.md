# Barcode Super Scanner Sender and Super Scanner Receiver

This project consists of two parts:
* **Super Scanner Sender** - An Android app that scans barcodes and sends the to the Super Scanner Receiver through TCP.
* **Super Scanner Receiver** - A small Java application that receives data from the Super Scanner Sender.

## What am I supposed to use this for?
The idea is that the Super Scanner Sender can be installed on multiple Android phones allowing for a group of people to take inventory of a large number of products simultaneously while periodically sending data to a single computer for processing.

## How do I use this?
### Requirements
- A TCP capable device with Java installed
  - Testing was done on Windows 7 and 10 computers
- An Android 5.0+ phone
  - This app requires Internet and camera permissions

### Installation
Download the project and navigate your way to the ReleaseVersion folder.
#### Installing Super Scanner Sender (Client)
1. Get the superScannerSender.apk from the ReleaseVersion/SuperScannerSender directory and put it on your phone.  
2. Run the apk on the phone to install the apk.  
**Note:** The installation requires an internet connection as the GMS Vision library has to be downloaded (it can't be bundled in the apk).  Running the app itself doesn't require internet connectivity.

#### Installing Super Scanner Receiver (Server)
The Super Scanner Receiver is contained in the project files as a stand-alone.  Just make sure you have Java installed and run superScannerServer.jar (located in the ReleaseVersion directory).

### Usage

#### Super Scanner Sender

Open up the Super Scanner Sender app on your Android device.
**If it's your first time using it**, click on the gear in the top right corner to go to the settings view.

Here you should enter the information in to the fields given:
- The Server IP given by the Super Scanner Receiver.  You can use the "Test" button to send a message to the Super Scanner Receiver to check your connection.  Check the Troubleshooting section if the receiver isn't receiving your messages.
- The Phone ID is a three letter identifier to identify data that is sent from your device (this can be anything, your initials perhaps?)

Both fields are obligatory.

When you're done, save the configuration by pressing "Save and return". The app will restart and your settings will be saved.  Continue reading to learn how to use the app.

**If you've configured the app**, assign a "Batch ID" in the top left.
The Batch ID *should* be a unique identifier; this might not matter if you're scanning a single area in one go but it's handy if you've divided items you want to scan in to various groups - regardless it's an obligatory field and it'll be wiped every time you send a batch of barcodes.  

To scan a barcode, press "Scan barcodes".  Your camera will activate and you'll be able to scan codes, check the Troubleshooting section if it you get a toast that says "Error setting up detector".  
Each time you scan a barcode, you'll be prompted to save it or discard it.  No other codes will be scanned until you save or discard the current code.  
When you have saved all the barcodes you want, go back to the main screen using the top right arrow or the back button on your device.  

You can manually add codes to you codes list using the "Add manually" button on the main screen.  

You can erase the last barcode, or all barcodes using the respective buttons.  

To send your barcodes to the receiver or to save them locally, press "Send codes".  If it's successful, a message will appear in the Super Scanner Receiver log containing the Batch ID and Phone ID.  Check Troubleshooting if you didn't receive it.  
A copy of the .txt file containing the codes is also saved on the device in the Downloads folder.  This occurs independently of the Super Scanner Receiver receiving anything or not.  

#### Super Scanner Receiver

Open up the Super Scanner Receiver.  Once your IP address shows up, you can input it in the settings of your Super Scanner Sender app.
You can also change the directory that incoming data is saved in.

The data that comes in is saved as follows: **3 letter identifier - Batch ID.txt**
The .txt contains each barcode scanned, one per line.  

In the Super Scanner Sender settings, you can check to see if the connection is working.  

##Troubleshooting
