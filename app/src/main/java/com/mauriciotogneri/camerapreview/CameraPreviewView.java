package com.mauriciotogneri.camerapreview;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.RelativeLayout;

public class CameraPreviewView extends RelativeLayout implements PreviewCallback
{
    public CameraPreviewView(Context context)
    {
        super(context);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        setGravity(Gravity.CENTER);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        Log.i("TEST", "DATA: " + data.length);
    }
}