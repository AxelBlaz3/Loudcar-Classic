package com.wielabs.loudcar.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import androidx.core.view.MotionEventCompat;

import com.wielabs.loudcar.ui.MainActivity;
import com.wielabs.loudcar.ui.devices.ReadThread;
import com.wielabs.loudcar.ui.devices.WriteThread;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ProtocolData {
    public static byte[] data1 = {1, 5, 2, 85, -86, 90, -91};
    public static byte[] data2;
    public static byte[] data3;
    public static byte[] data5 = {5, 3, (byte) MainActivity.pix, 5, 1};
    public static byte[] data6;
    public static byte[] data7;
    public static InputStream inStream;
    public static OutputStream outStream;
    public static boolean response_check;
    public static boolean sendBitmapLog = false;

    static {
        byte[] bArr = new byte[10];
        bArr[0] = 2;
        bArr[1] = 8;
        data2 = bArr;
        byte[] bArr2 = new byte[18];
        bArr2[0] = 3;
        bArr2[1] = 16;
        data3 = bArr2;
        byte[] bArr3 = new byte[10];
        bArr3[0] = 6;
        bArr3[1] = 8;
        data6 = bArr3;
        byte[] bArr4 = new byte[4];
        bArr4[0] = 7;
        bArr4[1] = 2;
        data7 = bArr4;
    }

    public static void sendDX(int flag, int val) {
        ReadThread readThread = new ReadThread(inStream);
        readThread.setCurrentCmd((byte) 1);
        readThread.start();
        WriteThread writeThread = null;
        if (MainActivity.connectFlag == 1) {
            writeThread = new WriteThread(outStream, readThread, new byte[]{10, 1, (byte) MainActivity.sendVal});
        } else if (MainActivity.connectFlag == 2) {
            writeThread = new WriteThread(outStream, readThread, new byte[]{11, 1, (byte) MainActivity.sendVal});
        } else if (MainActivity.connectFlag == 3) {
            byte[] b = str2Bcd(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
            ByteBuffer sendData = ByteBuffer.allocate(9);
            sendData.put((byte) 9);
            sendData.put((byte) 7);
            sendData.put(b);
            writeThread = new WriteThread(outStream, readThread, sendData.array());
        } else if (MainActivity.connectFlag == 4) {
            writeThread = new WriteThread(outStream, readThread, new byte[]{13, 4, (byte) ((MainActivity.sendVal / 1000) + 48), (byte) (((MainActivity.sendVal / 100) % 10) + 48), (byte) (((MainActivity.sendVal / 10) % 10) + 48), (byte) ((MainActivity.sendVal % 10) + 48)});
        }
        writeThread.start();
        try {
            writeThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        //MainActivity.handler.sendEmptyMessage(20);
//        //MainActivity.handler.sendEmptyMessage(20);
//        //MainActivity.handler.sendEmptyMessage(20);
//        //MainActivity.handler.sendEmptyMessage(20);
//        //MainActivity.handler.sendEmptyMessage(20);
//        //MainActivity.handler.sendEmptyMessage(10);
    }

    public static void send() {
        ReadThread readThread = new ReadThread(inStream);
        readThread.setCurrentCmd((byte) 1);
        readThread.start();
        WriteThread writeThread = new WriteThread(outStream, readThread, data1);
        writeThread.start();
        try {
            writeThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
       // //MainActivity.handler.sendEmptyMessage(20);
        readThread.setCurrentCmd((byte) 2);
        if (response_check) {
            parseShowXG();
            WriteThread writeThread2 = new WriteThread(outStream, readThread, data2);
            for(int i = 0 ; i < data2.length ; i++){
                System.out.println("data2data : "+ data2[i]+"");
            }
            try {
                writeThread2.start();
                writeThread2.join();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            //MainActivity.handler.sendEmptyMessage(20);
            readThread.setCurrentCmd((byte) 3);
            if (response_check) {
                parseShow();
                WriteThread writeThread3 = new WriteThread(outStream, readThread, data3);
                for(int i = 0 ; i < data3.length ; i++){
                    System.out.println("data3data : "+ data3[i]+"");
                }
                try {
                    writeThread3.start();
                    writeThread3.join();
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
               // //MainActivity.handler.sendEmptyMessage(20);
                readThread.setCurrentCmd((byte) 4);
                if (response_check) {
                    int num = 0;
                    ByteBuffer data = null;
                    for (int j = 0; j < MainActivity.pix; j++) {
                        for (int i = 0; i < 8; i++) {
                            if (MainActivity.data.get(i).get("check").equals("true")) {
                                num += MainActivity.bytedata[i][0].array().length;
                                Log.d("MainActivity", num + "");
                                ByteBuffer d = ByteBuffer.allocate(num);
                                if (data != null) {
                                    d.put(data.array());
                                }
                                System.out.println("sss:" + i + "  j:" + j);
                                d.put(MainActivity.bytedata[i][j].array());
                                if (data != null) {
                                    data.clear();
                                }
                                data = ByteBuffer.allocate(num);
                                data.put(d.array());
                            }
                        }
                    }
                    Log.v("aaa", " " + num);
                    int size = num / 32;
                    for (int x = 0; x < size; x++) {
                        if (response_check) {
                            ByteBuffer sendData = ByteBuffer.allocate(35);
                            sendData.put((byte) 4);
                            sendData.put((byte) 33);
                            sendData.put((byte) x);
                            sendData.put(data.array(), x * 32, 32);
                            WriteThread writeThread4 = new WriteThread(outStream, readThread, sendData.array());
                            try {
                                writeThread4.start();
                                writeThread4.join();
                            } catch (Exception e4) {
                                e4.printStackTrace();
                            }
                        }
                    }
                    if (num % 32 > 0 && response_check) {
                        ByteBuffer sendData2 = ByteBuffer.allocate((num % 32) + 3);
                        sendData2.put((byte) 4);
                        sendData2.put((byte) ((num % 32) + 1));
                        sendData2.put((byte) size);
                        sendData2.put(data.array(), size * 32, num % 32);
                        WriteThread writeThread5 = new WriteThread(outStream, readThread, sendData2.array());
                        try {
                            writeThread5.start();
                            writeThread5.join();
                        } catch (Exception e5) {
                            e5.printStackTrace();
                        }
                    }
                    //MainActivity.handler.sendEmptyMessage(20);
                    readThread.setCurrentCmd((byte) 5);
                    if (response_check) {
                        data5[2] = (byte) MainActivity.pix;
                        data5[3] = (byte) MainActivity.brig;
                        WriteThread writeThread6 = new WriteThread(outStream, readThread, data5);
                        try {
                            writeThread6.start();
                            writeThread6.join();
                        } catch (Exception e6) {
                            e6.printStackTrace();
                        }
                        //MainActivity.handler.sendEmptyMessage(20);
                        readThread.setCurrentCmd((byte) 6);
                        if (response_check) {
                            WriteThread writeThread7 = new WriteThread(outStream, readThread, data6);
                            try {
                                writeThread7.start();
                                writeThread7.join();
                            } catch (Exception e7) {
                                e7.printStackTrace();
                            }
                            //MainActivity.handler.sendEmptyMessage(20);
                            if (response_check) {
                                if (MainActivity.logContext.length() > 0) {
                                    ByteBuffer data4 = null;
                                    int num2 = 0;
                                    int hpx = 0;
                                    if (MainActivity.logFlag.equals("0")) {
                                        Typeface f = Typeface.DEFAULT;
                                        if (MainActivity.logFont != null && MainActivity.logFont.length() > 0) {
                                            f = Typeface.createFromFile(MainActivity.logFont);
                                        }
                                        Paint paint = new Paint();
                                        paint.setColor(-65536);
                                        paint.setTextSize((float) MainActivity.logFontSize);
                                        if (MainActivity.logFontStyle == 1) {
                                            paint.setFakeBoldText(true);
                                        } else if (MainActivity.logFontStyle == 2) {
                                            paint.setTextSkewX(-0.5f);
                                        } else if (MainActivity.logFontStyle == 3) {
                                            paint.setFakeBoldText(true);
                                            paint.setTextSkewX(-0.5f);
                                        }
                                        paint.setTypeface(f);
                                        Paint.FontMetrics fm = paint.getFontMetrics();
                                        Bitmap b = Bitmap.createBitmap(getFontWidth(paint, MainActivity.logContext), Math.round(fm.bottom - fm.ascent), Bitmap.Config.ARGB_8888);
                                        Canvas c = new Canvas(b);
                                        c.drawARGB(0, 0, 0, 0);
                                        c.drawText(MainActivity.logContext, 0.0f, getFontTop(paint) - 1.0f, paint);
                                        int width = b.getWidth() > MainActivity.pix * 4 ? MainActivity.pix * 4 : b.getWidth();
                                        int height = b.getHeight() > MainActivity.pix ? MainActivity.pix : b.getHeight();
                                        new Paint().setColor(-65536);
                                        int i2 = 0;
                                        Byte z = new Byte((byte) 0);
                                        data4 = ByteBuffer.allocate((width % 8 == 0 ? width / 8 : (width / 8) + 1) * height);
                                        num2 = width % 8 == 0 ? width / 8 : (width / 8) + 1;
                                        hpx = height;
                                        for (int h = 1; h <= height; h++) {
                                            for (int w = 1; w <= width; w++) {
                                                if (b.getPixel(w - 1, h - 1) == 0) {
                                                    z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                                                    Log.d("Matrix", w + " " + h + "1");
                                                } else {
                                                    z = Byte.valueOf((byte) (z.byteValue() << 1));
                                                    Log.d("Matrix", w + " " + h + "0");
                                                }
                                                i2++;
                                                if (i2 == 8) {
                                                    Log.v("count", String.valueOf(data4.array().length) + "======");
                                                    data4.put(z.byteValue());
                                                    i2 = 0;
                                                }
                                            }
                                            if (i2 < 8 && i2 != 0) {
                                                while (i2 <= 7) {
                                                    z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                                                    i2++;
                                                }
                                                data4.put(z.byteValue());
                                                i2 = 0;
                                            }
                                        }
                                        b.recycle();
                                    } else {
                                        try {
                                            Bitmap bitmap = BitmapFactory.decodeFile(MainActivity.logContext);
                                            int width2 = bitmap.getWidth() > MainActivity.pix * 4 ? MainActivity.pix * 4 : bitmap.getWidth();
                                            int height2 = bitmap.getHeight() > MainActivity.pix ? MainActivity.pix : bitmap.getHeight();
                                            new Paint().setColor(-65536);
                                            int i3 = 0;
                                            Byte z2 = new Byte((byte) 0);
                                            data4 = ByteBuffer.allocate((width2 % 8 == 0 ? width2 / 8 : (width2 / 8) + 1) * height2);
                                            num2 = width2 % 8 == 0 ? width2 / 8 : (width2 / 8) + 1;
                                            hpx = height2;
                                            for (int h2 = 1; h2 <= height2; h2++) {
                                                for (int w2 = 1; w2 <= width2; w2++) {
                                                    if (getColorLD(bitmap.getPixel(w2 - 1, h2 - 1)) <= 127000) {
                                                        z2 = Byte.valueOf((byte) ((z2.byteValue() << 1) | 1));
                                                        Log.d("Matrix", w2 + " " + h2 + "1");
                                                    } else {
                                                        z2 = Byte.valueOf((byte) (z2.byteValue() << 1));
                                                        Log.d("Matrix", w2 + " " + h2 + "1");
                                                    }
                                                    i3++;
                                                    if (i3 == 8) {
                                                        data4.put(z2.byteValue());
                                                        i3 = 0;
                                                    }
                                                }
                                                if (i3 < 8 && i3 != 0) {
                                                    while (i3 <= 7) {
                                                        z2 = Byte.valueOf((byte) ((z2.byteValue() << 1) | 1));
                                                        i3++;
                                                    }
                                                    data4.put(z2.byteValue());
                                                    i3 = 0;
                                                }
                                            }
                                            bitmap.recycle();
                                        } catch (Exception e8) {
                                            e8.printStackTrace();
                                        }
                                    }
                                    if (num2 > 255) {
                                        data7[2] = (byte) ((num2 >> 8) & MotionEventCompat.ACTION_MASK);
                                        data7[3] = (byte) (num2 & MotionEventCompat.ACTION_MASK);
                                    } else {
                                        data7[2] = 0;
                                        data7[3] = (byte) (num2 & MotionEventCompat.ACTION_MASK);
                                    }
                                    int num3 = num2 * hpx;
                                    readThread.setCurrentCmd((byte) 7);
                                    WriteThread writeThread8 = new WriteThread(outStream, readThread, data7);
                                    try {
                                        writeThread8.start();
                                        writeThread8.join();
                                    } catch (Exception e9) {
                                        e9.printStackTrace();
                                    }
                                    int size2 = num3 / 32;
                                    readThread.setCurrentCmd((byte) 8);
                                    for (int x2 = 0; x2 < size2; x2++) {
                                        if (response_check) {
                                            ByteBuffer sendData3 = ByteBuffer.allocate(35);
                                            sendData3.put((byte) 8);
                                            sendData3.put((byte) 33);
                                            sendData3.put((byte) x2);
                                            sendData3.put(data4.array(), x2 * 32, 32);
                                            WriteThread writeThread9 = new WriteThread(outStream, readThread, sendData3.array());
                                            try {
                                                writeThread9.start();
                                                writeThread9.join();
                                            } catch (Exception e10) {
                                                e10.printStackTrace();
                                            }
                                        }
                                    }
                                    if (num3 % 32 > 0 && response_check) {
                                        ByteBuffer sendData4 = ByteBuffer.allocate((num3 % 32) + 3);
                                        sendData4.put((byte) 8);
                                        sendData4.put((byte) ((num3 % 32) + 1));
                                        sendData4.put((byte) size2);
                                        sendData4.put(data4.array(), size2 * 32, num3 % 32);
                                        WriteThread writeThread10 = new WriteThread(outStream, readThread, sendData4.array());
                                        try {
                                            writeThread10.start();
                                            writeThread10.join();
                                        } catch (Exception e11) {
                                            e11.printStackTrace();
                                        }
                                    }
                                } else {
                                    data7[2] = 0;
                                    data7[3] = 0;
                                    readThread.setCurrentCmd((byte) 7);
                                    WriteThread writeThread11 = new WriteThread(outStream, readThread, data7);
                                    try {
                                        writeThread11.start();
                                        writeThread11.join();
                                    } catch (Exception e12) {
                                        e12.printStackTrace();
                                    }
                                }
                                //MainActivity.handler.sendEmptyMessage(20);
                                readThread.setCurrentCmd((byte) 9);
                                if (response_check) {
                                    byte[] b2 = str2Bcd(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
                                    ByteBuffer sendData5 = ByteBuffer.allocate(9);
                                    sendData5.put((byte) 9);
                                    sendData5.put((byte) 7);
                                    sendData5.put(b2);
                                    WriteThread writeThread12 = new WriteThread(outStream, readThread, sendData5.array());
                                    try {
                                        writeThread12.start();
                                        writeThread12.join();
                                    } catch (Exception e13) {
                                        e13.printStackTrace();
                                    }
                                    //MainActivity.handler.sendEmptyMessage(20);
                                    if (response_check) {
                                        //MainActivity.handler.sendEmptyMessage(10);
                                        return;
                                    }
                                    return;
                                }
                                //MainActivity.handler.sendEmptyMessage(4);
                                return;
                            }
                            //MainActivity.handler.sendEmptyMessage(6);
                            return;
                        }
                        //MainActivity.handler.sendEmptyMessage(4);
                        return;
                    }
                    //MainActivity.handler.sendEmptyMessage(3);
                    return;
                }
                //MainActivity.handler.sendEmptyMessage(2);
                return;
            }
            //MainActivity.handler.sendEmptyMessage(1);
            return;
        }
        //MainActivity.handler.sendEmptyMessage(5);
    }

    public static void parseShow() {
        for (int i = 0; i < 8; i++) {
            Map<String, String> map = MainActivity.data.get(i);
            if (map.get("check").equals("true")) {
                String context = map.get("context");
                String font = map.get("fontFile");
                String fontSize = map.get("fontSize");
                String fontStyle = map.get("fontStyle");
                if (map.get("flag").equals("0")) {
                    parseText(context, font, i, Integer.parseInt(fontSize), Integer.parseInt(fontStyle), map.get("reverse"));
                } else {
                    parseBitmap(context, i);
                }

                int num = MainActivity.bytedata[i][0].array().length;
                Log.d("Number", num+"");
                Log.v("xs", new StringBuilder().append(num).toString());
                if (num > 255) {
                    data3[(i * 2) + 2] = (byte) ((num >> 8) & MotionEventCompat.ACTION_MASK);
                    data3[(i * 2) + 3] = (byte) (num & MotionEventCompat.ACTION_MASK);
                } else {
                    data3[(i * 2) + 2] = 0;
                    data3[(i * 2) + 3] = (byte) (num);
                }
            } else {
                data3[(i * 2) + 2] = 0;
                data3[(i * 2) + 3] = 0;
            }
        }
    }

    public static void parseShowXG() {
        for (int i = 0; i < 8; i++) {
            Map<String, String> map = MainActivity.data.get(i);
            if (map.get("check").equals("true")) {
                data6[i + 2] = (byte) Integer.parseInt(map.get("qianru"));
                byte b = 0;
                String time = map.get("time");
                String twinkle = map.get("twinkle");
                String action = map.get("action");
                if (map.get("border").equals("true")) {
                    b = (byte) 128;
                }
                byte b2 = (byte) (((Integer.parseInt(time) - 1) << 4) | b);
                if (twinkle.equals("true")) {
                    b2 = (byte) (b2 | 8);
                }
                byte b3 = (byte) ((Integer.parseInt(action) - 1) | b2);
                Log.v("data2", "item:" + i + ",val:" + b3);
                data2[i + 2] = b3;
            } else {
                data6[i + 2] = 0;
                data2[i + 2] = 0;
            }
        }
    }

    public static void parseText(String text, String font, int selectRow, int fontSize, int fontStyle, String isReverse) {
        if (((String) MainActivity.data.get(selectRow).get("check")).equals("false") || text == null || text.length() == 0) {
            for (int i = 0; i < MainActivity.pix; i++) {
                MainActivity.bytedata[selectRow][i] = ByteBuffer.allocate(0);
            }
            return;
        }
        Typeface f = Typeface.DEFAULT;
        if (font != null && font.length() > 0) {
            f = Typeface.createFromFile(font);
        }
        Paint paint = new Paint();
        paint.setColor(-1);
        paint.setTextSize((float) fontSize);
        paint.setTypeface(f);
        Paint.FontMetrics fm = paint.getFontMetrics();
        if (fontStyle == 1) {
            paint.setFakeBoldText(true);
        } else if (fontStyle == 2) {
            paint.setTextSkewX(-0.5f);
        } else if (fontStyle == 3) {
            paint.setFakeBoldText(true);
            paint.setTextSkewX(-0.5f);
        }
        Bitmap b = Bitmap.createBitmap(getFontWidth(paint, text), Math.round(fm.bottom - fm.ascent), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawARGB(0, 0, 0, 0);
        c.drawText(text, 0.0f, getFontTop(paint) - 1.0f, paint);
        int width = b.getWidth();
        int height = MainActivity.pix > b.getHeight() ? b.getHeight() : MainActivity.pix;
        int i2 = 0;
        Byte z = new Byte((byte) 0);
        for (int h = 1; h <= height; h++) {
            MainActivity.bytedata[selectRow][h - 1] = ByteBuffer.allocate(width % 8 > 0 ? (width / 8) + 1 : width / 8);
            if (isReverse == "true") {
                for (int w = width; w >= 1; w--) {
                    if (b.getPixel(w - 1, h - 1) == 0) {
                        z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                        System.out.print(" ");
                    } else {
                        z = Byte.valueOf((byte) (z.byteValue() << 1));
                        System.out.print("*");
                    }
                    i2++;
                    if (i2 == 8) {
                        MainActivity.bytedata[selectRow][h - 1].put(z.byteValue());
                        i2 = 0;
                    }
                }
            } else {
                for (int w = 1; w <= width; w++) {
                    if (b.getPixel(w - 1, h - 1) == 0) {
                        z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                        System.out.print(" ");
                    } else {
                        z = Byte.valueOf((byte) (z.byteValue() << 1));
                        System.out.print("*");
                    }
                    i2++;
                    if (i2 == 8) {
                        MainActivity.bytedata[selectRow][h - 1].put(z.byteValue());
                        i2 = 0;
                    }
                }
            }
            System.out.println();
            if (i2 < 8 && i2 != 0) {
                while (i2 <= 7) {
                    z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                    i2++;
                }
                MainActivity.bytedata[selectRow][h - 1].put(z.byteValue());
                i2 = 0;
            }
        }
    }

    public static float getFontTop(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        Log.v("fontSize", new StringBuilder().append((0.0f - fm.top) - ((0.0f - fm.top) + fm.ascent)).toString());
        return fm.leading - fm.ascent;
    }

    public static void parseBitmap(String pathName, int selectRow) {
        try {
            if (((String) MainActivity.data.get(selectRow).get("check")).equals("false") || pathName == null || pathName.length() == 0) {
                for (int i = 0; i < MainActivity.pix; i++) {
                    MainActivity.bytedata[selectRow][i] = ByteBuffer.allocate(0);
                }
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(pathName);
            int width = bitmap.getWidth();
            int height = MainActivity.pix;
            new Paint().setColor(-65536);
            int i2 = 0;
            Byte z = new Byte((byte) 0);
            for (int h = 1; h <= height; h++) {
                MainActivity.bytedata[selectRow][h - 1] = ByteBuffer.allocate(width % 8 > 0 ? (width / 8) + 1 : width / 8);
                for (int w = 1; w <= width; w++) {
                    if (getColorLD(bitmap.getPixel(w - 1, h - 1)) < 127000) {
                        z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                    } else {
                        z = Byte.valueOf((byte) (z.byteValue() << 1));
                    }
                    i2++;
                    if (i2 == 8) {
                        MainActivity.bytedata[selectRow][h - 1].put(z.byteValue());
                        i2 = 0;
                    }
                }
                if (!(i2 == 8 || i2 == 0)) {
                    while (i2 <= 7) {
                        z = Byte.valueOf((byte) ((z.byteValue() << 1) | 1));
                        i2++;
                    }
                    MainActivity.bytedata[selectRow][h - 1].put(z.byteValue());
                    i2 = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getColorLD(int rgb) {
        return (Color.red(rgb) * 299) + (Color.green(rgb) * 587) + (Color.blue(rgb) * 114);
    }

    public static int getFontWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil((double) widths[j]);
            }
        }
        return iRet;
    }

    public static byte[] str2Bcd(String asc) {
        int j;
        int k;
        int len = asc.length();
        if (len % 2 != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte[] bArr = new byte[len];
        if (len >= 2) {
            len /= 2;
        }
        byte[] bbt = new byte[len];
        byte[] abt = asc.getBytes();
        for (int p = 0; p < asc.length() / 2; p++) {
            if (abt[p * 2] >= 48 && abt[p * 2] <= 57) {
                j = abt[p * 2] - 48;
            } else if (abt[p * 2] < 97 || abt[p * 2] > 122) {
                j = (abt[p * 2] - 65) + 10;
            } else {
                j = (abt[p * 2] - 97) + 10;
            }
            if (abt[(p * 2) + 1] >= 48 && abt[(p * 2) + 1] <= 57) {
                k = abt[(p * 2) + 1] - 48;
            } else if (abt[(p * 2) + 1] < 97 || abt[(p * 2) + 1] > 122) {
                k = (abt[(p * 2) + 1] - 65) + 10;
            } else {
                k = (abt[(p * 2) + 1] - 97) + 10;
            }
            bbt[p] = (byte) ((j << 4) + k);
        }
        return bbt;
    }
}
