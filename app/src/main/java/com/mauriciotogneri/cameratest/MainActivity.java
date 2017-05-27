package com.mauriciotogneri.cameratest;

import android.app.Activity;
import android.os.Bundle;

import com.mauriciotogneri.camerapreview.NewCameraPreview;

import java.io.IOException;

// https://github.com/pikanji/CameraPreviewSample
public class MainActivity extends Activity
{
    private NewCameraPreview preview;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        preview = (NewCameraPreview) findViewById(R.id.camerapreview);
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