package com.mauriciotogneri.camerapreview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

public class ThreadProcessor
{
    private Bitmap bitmap(byte[] data, Camera.Parameters parameters)
    {
        Camera.Size previewSize = parameters.getPreviewSize();
        int width = previewSize.width;
        int height = previewSize.height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

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
}