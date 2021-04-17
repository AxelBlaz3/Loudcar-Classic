package com.wielabs.loudcar.ui.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;

import com.wielabs.loudcar.ui.MainActivity;
import com.wielabs.loudcar.util.Constants;
import com.wielabs.loudcar.util.ProtocolData;

import java.io.IOException;

public class ConnectThread extends Thread {
    public static BluetoothDevice device = null;
    public static BluetoothSocket socket = null;

    public ConnectThread(BluetoothDevice d) {
        device = d;
    }

    public void run() {
        BluetoothSocket temp = null;
        try {
            temp = device.createRfcommSocketToServiceRecord(Constants.SPP_UUID);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        socket = temp;
        if (socket != null) {
            try {
                socket.connect();
            } catch (IOException ex2) {
                ex2.printStackTrace();
                Message mesg = new Message();
                mesg.what = 100;
                mesg.obj = ex2.toString();
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
        if (socket != null) {
            try {
                ProtocolData.inStream = socket.getInputStream();
                ProtocolData.outStream = socket.getOutputStream();
                ProtocolData.sendBitmapLog = true;
                if (MainActivity.connectFlag == 0) {
                    ProtocolData.send();
                } else {
                    ProtocolData.sendDX(MainActivity.connectFlag, MainActivity.sendVal);
                }
                ProtocolData.sendBitmapLog = false;
                ProtocolData.inStream.close();
                ProtocolData.inStream = null;
                ProtocolData.outStream.close();
                ProtocolData.outStream = null;
            } catch (IOException ex3) {
                ex3.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex4) {
                ex4.printStackTrace();
            }
            socket = null;
        }
    }
}