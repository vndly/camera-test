package com.mauriciotogneri.camerapreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraPreview extends RelativeLayout implements PreviewCallback, SurfaceHolder.Callback
{
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Rect surfaceSize = null;

    public CameraPreview(Context context)
    {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr)
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
        Camera.Parameters cameraParameters = camera.getParameters();

        Camera.Size previewSize = previewSizeByAspectRatio(cameraParameters, surfaceSize.width(), surfaceSize.height());
        boolean layoutSizeChanged = adjustSurfaceLayoutSize(previewSize, surfaceSize.width(), surfaceSize.height());

        if (!layoutSizeChanged)
        {
            if (previewSize != null)
            {
                cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.setParameters(cameraParameters);
            camera.startPreview();
        }
    }

    /*private Camera.Size previewSizeByMaxSize(int width, int height, Camera.Parameters parameters)
    {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
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
    }*/

    // adjust surface size with the closest aspect-ratio
    private Camera.Size previewSizeByAspectRatio(Camera.Parameters parameters, int reqWidth, int reqHeight)
    {
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

    private boolean adjustSurfaceLayoutSize(Camera.Size previewSize, int surfaceWidth, int surfaceHeight)
    {
        float tmpLayoutHeight = previewSize.height;
        float tmpLayoutWidth = previewSize.width;

        float factH = surfaceHeight / tmpLayoutHeight;
        float factW = surfaceWidth / tmpLayoutWidth;
        float fact = (factH < factW) ? factH : factW;

        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);

        if ((layoutWidth != surfaceWidth) || (layoutHeight != surfaceHeight))
        {
            ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
            layoutParams.height = layoutHeight;
            layoutParams.width = layoutWidth;
            surfaceView.setLayoutParams(layoutParams); // this will trigger another surfaceChanged call

            return true;
        }
        else
        {
            return false;
        }
    }

    private int count = 0;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        long start = System.currentTimeMillis();

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewSize = parameters.getPreviewSize();
        Bitmap bitmap = bitmap(data, previewSize.width, previewSize.height);

        //Bitmap bitmap = bitmap(data, camera.getParameters());

        Log.i("TEST", "TIME: " + (System.currentTimeMillis() - start));

        try
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
        }
    }

    private Bitmap bitmap(byte[] data, Camera.Parameters parameters)
    {
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

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