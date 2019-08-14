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
#### Installing Super Scanner Sender
1. Get the superScannerSender.apk from the ReleaseVersion/SuperScannerSender directory and put that file on your phone.  
2. Run the apk on the phone to install the apk.  
**Note:** The installation requires an internet connection as the GMS Vision library has to be downloaded (it can't be bundled in the apk).  Running the app itself doesn't require internet connectivity.

#### Installing Super Scanner Receiver
The Super Scanner Receiver (Sever) is contained in the project files as a stand-alone.  Just make sure you have Java installed and run superScannerServer.jar (located in the ReleaseVersion directory).

### Usage

#### Super Scanner Super


#### Super Scanner Receiver
