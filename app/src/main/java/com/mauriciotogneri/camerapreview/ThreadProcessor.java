package com.mauriciotogneri.camerapreview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

public class ThreadProcessor extends Thread
{
    private boolean running = true;
    private boolean processing = false;
    private final Object lock = new Object();

    private byte[] inputData = null;
    private int inputWidth = -1;
    private int inputHeight = -1;
    private int inputFormat = -1;

    public ThreadProcessor()
    {
    }

    public void request(byte[] data, int width, int height, int format)
    {
        if (running)
        {
            synchronized (lock)
            {
                if (!processing)
                {
                    inputData = data;
                    inputWidth = width;
                    inputHeight = height;
                    inputFormat = format;

                    lock.notify();
                }
            }
        }
    }

    public void stopProcess()
    {
        running = false;

        synchronized (lock)
        {
            lock.notify();
        }
    }

    @Override
    public void run()
    {
        while (running)
        {
            boolean doWork = false;

            synchronized (lock)
            {
                try
                {
                    lock.wait();
                    processing = true;
                    doWork = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (doWork && running)
            {
                if ((inputData != null) && (inputWidth != -1) && (inputHeight != -1))
                {
                    process(inputData, inputWidth, inputHeight, inputFormat);
                }

                inputData = null;
                inputWidth = -1;
                inputHeight = -1;
                inputFormat = -1;

                synchronized (lock)
                {
                    processing = false;
                }
            }
        }
    }

    private void process(byte[] data, int width, int height, int format)
    {
        long start = System.currentTimeMillis();

        Bitmap bitmap = bitmap(data, width, height);
        //save(bitmap);

        Log.i("TEST", "TIME: " + (System.currentTimeMillis() - start));
    }

    private Bitmap bitmap(byte[] data, int width, int height, int format)
    {
        YuvImage yuv = new YuvImage(data, format, width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

        byte[] bytes = out.toByteArray();

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private Bitmap bitmap(byte[] data, int width, int height)
    {
        int[] pixels = new int[width * height];
        int inputOffset = 0;

        for (int y = 0; y < height; y++)
        {
            int outputOffset = y * width;

            for (int x = 0; x < width; x++)
            {
                int grey = data[inputOffset + x] & 0xff;
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }

            inputOffset += width;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    private void save(Bitmap bitmap)
    {
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file.jpg");
            fos.write(byteArray);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}