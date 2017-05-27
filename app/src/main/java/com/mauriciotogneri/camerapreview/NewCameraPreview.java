package com.mauriciotogneri.camerapreview;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class NewCameraPreview extends RelativeLayout implements PreviewCallback, SurfaceHolder.Callback
{
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LayoutMode layoutMode;

    private int surfaceWidth = -1;
    private int surfaceHeight = -1;

    public enum LayoutMode
    {
        FIT_TO_PARENT, // Scale to the size that no side is larger than the parent
        NO_BLANK // Scale to the size that no side is smaller than the parent
    }

    public NewCameraPreview(Context context)
    {
        super(context);
        init(context);
    }

    public NewCameraPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public NewCameraPreview(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context)
    {
        setGravity(Gravity.CENTER);

        surfaceView = new SurfaceView(context);
        addView(surfaceView);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        layoutMode = LayoutMode.FIT_TO_PARENT;
    }

    public void resume() throws IOException
    {
        camera = openCamera(0);
        startPreview();
    }

    public void pause()
    {
        stopCamera();
    }

    private Camera openCamera(int id)
    {
        stopCamera();

        return Camera.open(id);
    }

    private void stopCamera()
    {
        if (camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startPreview() throws IOException
    {
        if ((camera != null) && (surfaceWidth != -1) && (surfaceHeight != -1))
        {
            initPreview(camera, surfaceWidth, surfaceHeight);
        }
    }

    private Camera.Size previewSize(int width, int height, Camera.Parameters parameters)
    {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            if (size.width <= width && size.height <= height)
            {
                if (result == null)
                {
                    result = size;
                }
                else
                {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea)
                    {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    private void initPreview(Camera camera, int width, int height) throws IOException
    {
        if (camera != null)
        {
            camera.setPreviewDisplay(surfaceHolder);

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = previewSize(width, height, parameters);

            if (size != null)
            {
                parameters.setPreviewSize(size.width, size.height);
                requestLayout();
                camera.setParameters(parameters);
                camera.startPreview();
            }
        }
    }

    //---------------------

    /*public void setCamera(Camera camera)
    {
        if (this.camera == camera)
        {
            return;
        }

        stopCamera();

        this.camera = camera;

        if (camera != null)
        {
            List<Size> localSizes = camera.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
            requestLayout();

            try
            {
                camera.setPreviewDisplay(surfaceHolder);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            camera.startPreview();
        }
    }*/

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        surfaceWidth = width;
        surfaceHeight = height;

        try
        {
            startPreview();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        stopCamera();

        surfaceWidth = -1;
        surfaceHeight = -1;
    }
}