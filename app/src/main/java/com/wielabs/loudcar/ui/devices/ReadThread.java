package com.wielabs.loudcar.ui.devices;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class ReadThread extends Thread {
    private static final int RESP_LENGTH = 4;
    private byte currentCmd = -1;
    private InputStream inputStream;
    private int position = 0;
    private byte[] responseData = new byte[4];

    public ReadThread(InputStream stream) {
        this.inputStream = stream;
    }

    public void setCurrentCmd(byte currentCmd2) {
        this.currentCmd = currentCmd2;
    }

    public synchronized void reset() {
        this.position = 0;
    }

    public void run() {
        try {
            byte[] buffer = new byte[4];
            while (true) {
                int len = this.inputStream.read(buffer, 0, 4);
                if (len > 0) {
                    synchronized (this) {
                        Log.v("test", "read " + len + " " + this.position);
                        if (this.position + len > 4) {
                            this.position = 0;
                        }
                        for (int i = 0; i < len; i++) {
                            this.responseData[this.position + i] = buffer[i];
                        }
                        this.position += len;
                        if (this.position == 4) {
                            Log.v("test", new StringBuilder().append(this.responseData[0]).append(this.responseData[1]).append(this.responseData[2]).append(this.responseData[3]).toString());
                            notifyAll();
                            if (this.responseData[0] == this.currentCmd && this.responseData[2] == 0 && this.responseData[0] + this.responseData[1] + this.responseData[2] == this.responseData[3]) {
                                Log.v("test", "ok");
                                notifyAll();
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
