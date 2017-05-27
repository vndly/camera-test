package com.mauriciotogneri.cameratest;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;

import com.mauriciotogneri.camerapreview.CameraPreview;
import com.mauriciotogneri.camerapreview.CameraPreview.PreviewReady;
import com.mauriciotogneri.camerapreview.ThreadProcessor;

import java.io.IOException;

// https://github.com/pikanji/CameraPreviewSample
public class MainActivity extends Activity implements PreviewCallback, PreviewReady
{
    private CameraPreview preview;
    private ThreadProcessor threadProcessor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        threadProcessor = new ThreadProcessor();
        threadProcessor.start();

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
        Camera.Size previewSize = camera.getParameters().getPreviewSize();

        threadProcessor.request(data, previewSize.width, previewSize.height);
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

    @Override
    protected void onDestroy()
    {
        threadProcessor.stopProcess();

        super.onDestroy();
    }
}