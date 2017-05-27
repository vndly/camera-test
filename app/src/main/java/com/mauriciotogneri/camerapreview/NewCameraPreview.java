package com.mauriciotogneri.camerapreview;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
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
    private Rect surfaceSize = null;

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
        stopCamera();
        camera = Camera.open(0);
        startPreview();
    }

    public void pause()
    {
        stopCamera();
    }

    private void stopCamera()
    {
        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void startPreview() throws IOException
    {
        if ((camera != null) && (surfaceSize != null))
        {
            initPreview(camera, surfaceSize);
        }
    }

    private void initPreview(Camera camera, Rect surfaceSize) throws IOException
    {
        camera.setPreviewDisplay(surfaceHolder);

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = previewSize(surfaceSize.width(), surfaceSize.height(), parameters);
        adjustSurfaceLayoutSize(size, surfaceSize.width(), surfaceSize.height());

        if (size != null)
        {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
            camera.startPreview();
            camera.setPreviewCallback(this);
        }
    }

    private Camera.Size previewSize(int width, int height, Camera.Parameters parameters)
    {
        Camera.Size result = null;

        Log.i("SIZE1", width + " - " + height);

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            Log.i("SIZE2", size.width + " - " + size.height);

            if ((size.width <= width) && (size.height <= height))
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

    private Camera.Size previewSize2(int reqWidth, int reqHeight, Camera.Parameters parameters)
    {
        // adjust surface size with the closest aspect-ratio
        float reqRatio = ((float) reqWidth) / reqHeight;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            float curRatio = ((float) size.width) / size.height;
            float deltaRatio = Math.abs(reqRatio - curRatio);

            if (deltaRatio < deltaRatioMin)
            {
                deltaRatioMin = deltaRatio;
                result = size;
            }
        }

        return result;
    }

    private boolean adjustSurfaceLayoutSize(Camera.Size previewSize, int availableWidth, int availableHeight)
    {
        float tmpLayoutHeight = previewSize.height;
        float tmpLayoutWidth = previewSize.width;

        float factH, factW, fact;
        factH = availableHeight / tmpLayoutHeight;
        factW = availableWidth / tmpLayoutWidth;

        if (layoutMode == LayoutMode.FIT_TO_PARENT)
        {
            // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
            if (factH < factW)
            {
                fact = factH;
            }
            else
            {
                fact = factW;
            }
        }
        else
        {
            if (factH < factW)
            {
                fact = factW;
            }
            else
            {
                fact = factH;
            }
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);

        boolean layoutChanged;

        if ((layoutWidth != surfaceView.getWidth()) || (layoutHeight != surfaceView.getHeight()))
        {
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            surfaceView.setLayoutParams(layoutParams); // this will trigger another surfaceChanged invocation.
            layoutChanged = true;
        }
        else
        {
            layoutChanged = false;
        }

        return layoutChanged;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        Log.i("TEST", "DATA: " + data.length);

        /*try
        {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file.bmp");
            fos.write(data);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }*/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        surfaceSize = new Rect(0, 0, width, height);

        try
        {
            startPreview();
        }
        catch (Exception e)
        {
            stopCamera();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        stopCamera();
        surfaceSize = null;
    }
}