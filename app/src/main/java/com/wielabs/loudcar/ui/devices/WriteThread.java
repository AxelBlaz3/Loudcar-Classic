package com.wielabs.loudcar.ui.devices;

import android.util.Log;

import androidx.core.view.MotionEventCompat;

import com.wielabs.loudcar.util.ProtocolData;

import java.io.IOException;
import java.io.OutputStream;

public class WriteThread extends Thread {
    public static byte[] data;
    private OutputStream outStream;
    private ReadThread readThread;

    public WriteThread(OutputStream out, ReadThread rThread, byte[] d) {
        this.readThread = rThread;
        this.outStream = out;
        data = new byte[(d.length + 1)];
        int checkSum = 0;
        String log = "Item ";
        for (int i = 0; i < d.length; i++) {
            data[i] = d[i];
            checkSum += d[i];
            log = String.valueOf(log) + d[i] + " ";
        }
        data[d.length] = (byte) (checkSum & MotionEventCompat.ACTION_MASK);
        Log.v("DATA", log);
        Log.v("CHECKSUM", String.valueOf(data[0]) + " " + data[d.length] + " ");
        ProtocolData.response_check = true;
    }

    public void run() {
        ProtocolData.response_check = false;
        int triedTimes = 0;
        while (!ProtocolData.response_check) {
            try {
                int triedTimes2 = triedTimes + 1;
                if (triedTimes < 3) {
                    this.readThread.reset();
                    this.outStream.write(data);
                    this.outStream.flush();
                    try {
                        synchronized (this.readThread) {
                            long start = System.currentTimeMillis();
                            this.readThread.wait(500);
                            if (System.currentTimeMillis() - start < 500) {
                                ProtocolData.response_check = true;
                            }
                        }
                        triedTimes = triedTimes2;
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        triedTimes = triedTimes2;
                    }
                } else {
                    return;
                }
            } catch (IOException e) {
                int i = triedTimes;
                e.printStackTrace();
            }
        }
    }
}
