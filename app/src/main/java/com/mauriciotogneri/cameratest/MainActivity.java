package com.mauriciotogneri.cameratest;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;

import com.mauriciotogneri.camerapreview.CameraPreview;
import com.mauriciotogneri.camerapreview.CameraPreview.PreviewReady;

import java.io.IOException;

// https://github.com/pikanji/CameraPreviewSample
public class MainActivity extends Activity implements PreviewCallback, PreviewReady
{
    private CameraPreview preview;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        preview = (CameraPreview) findViewById(R.id.camerapreview);
        preview.previewReady(this);
    }

    @Override
    public void onPreviewReady()
    {
        preview.previewCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        long start = System.currentTimeMillis();

        //Camera.Size previewSize = parameters.getPreviewSize();
        //Bitmap bitmap = bitmap(data, previewSize.width, previewSize.height);

        //Bitmap bitmap = bitmap(data, camera.getParameters());

        Log.i("TEST", "TIME: " + (System.currentTimeMillis() - start));

        /*try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file.bmp");
            fos.write(byteArray);
            fos.close();

            count++;

            if (count > 3)
            {
                System.exit(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            preview.resume();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        preview.pause();

        super.onPause();
    }
}