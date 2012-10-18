/**

 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 * All rights reserved.
 *
 * Copyright (C) 2012 LISA team
 */
package project.cs.lisa.bluetooth.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import project.cs.lisa.bluetooth.TransmissionStatus;
import project.cs.lisa.bluetooth.provider.BluetoothConnectionHandler;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * The ConnectedBluetoothThread handles the transmission between two devices. It can
 * be used for reading and writing to a stream.
 * 
 * @author ktran
 *
 */
public class ClientBluetoothThread extends Thread {

    /** Debug Tag. */
    private static final String TAG = "ConnectedClientBluetoothThread";

    /** Bluetooth Socket enabling the connection to the remote device. */
    private final BluetoothSocket mBtSocket;

    /** Input Stream used for reading in requests. */
    private final DataInputStream mInStream;

    /** Output Stream used for writing data. */
    private final DataOutputStream mOutStream;

    /** The Handler we communicate the results to. */
    private Handler mHandler;

    /** Representation of the content to retrieve (usually name of a file) */
    private String mHash;

    /**
     * Creates a new thread dealing with reading and writing data.
     * 
     * @param myHandler     A handler.
     * @param myBtSocket    The Bluetooth socket used for the data transfer.
     * @param hash          The hash identifying the BO we want to retrieve.
     */
    public ClientBluetoothThread(Handler myHandler, BluetoothSocket myBtSocket, String hash) {
        mHandler = myHandler;
        mBtSocket = myBtSocket;
        mHash = hash;

        DataInputStream tmpIn = null;
        DataOutputStream tmpOut = null;         

        try {
            tmpIn = new DataInputStream(mBtSocket.getInputStream());
            tmpOut = new DataOutputStream(mBtSocket.getOutputStream());
        } catch (IOException e) {
            Log.d(TAG, "Couldn't extract streams for Bluetooth transmission.");
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }


    /** Starts the thread for managing a connection with a remote device through Bluetooth.*/
    @Override
    public void run() {
        Log.d(TAG, "Starting to receive the incoming message");

        try {
            Log.d(TAG, "Writing the hash...");
            write(mHash.getBytes());
            Log.d(TAG, "Finished writing the hash.");

            int fileSize = mInStream.readInt();
            byte[] buffer = new byte[fileSize];
            mInStream.readFully(buffer);
            mHandler.obtainMessage(BluetoothConnectionHandler.FILE_READ, -1, -1, buffer)
            .sendToTarget();
        } catch (IOException e) {
            Log.d(TAG, "Error while receiving incoming file.");
            mHandler.obtainMessage(BluetoothConnectionHandler.FILE_READ, 
                    TransmissionStatus.FAILED.ordinal(), -1, null)
                    .sendToTarget();
        }           
    }  

    /**
     * Writes the passed byte array to the Bluetooth socket.
     * 
     * @param buffer The byte array to send to the remote device.
     */
    public void write(byte[] buffer) {
        Log.d(TAG, "Sending hash bytes..."); 

        try {
            mOutStream.writeInt(buffer.length);
            mOutStream.write(buffer, 0, buffer.length);
            mOutStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Exception occured during writing", e);
        }           
    }

    /** Shuts down the current server client connection.*/
    public void cancel() {
        try {
            mOutStream.close();
            mInStream.close();
            mBtSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "Error while closing Bluetooth socket");
        }
    }
}
