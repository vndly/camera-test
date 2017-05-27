package com.mauriciotogneri.cameratest;

import android.app.Activity;
import android.os.Bundle;

import com.mauriciotogneri.camerapreview.CameraPreview;
import com.mauriciotogneri.camerapreview.CameraPreviewView;

// https://github.com/pikanji/CameraPreviewSample
public class MainActivity extends Activity
{
    private CameraPreview preview;
    private CameraPreviewView view;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        view = (CameraPreviewView) findViewById(R.id.camerapreview);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        preview = new CameraPreview(this, 0, CameraPreview.LayoutMode.FitToParent);
        view.addView(preview);

        preview.setPreviewCallback(view);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        preview.stop();
        view.removeView(preview);
        preview = null;
    }
}