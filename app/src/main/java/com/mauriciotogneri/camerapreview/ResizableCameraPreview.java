package com.mauriciotogneri.camerapreview;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.List;

/**
 * CameraPreview class that is extended only for the purpose of testing CameraPreview class.
 * This class is added functionality to set arbitrary preview size, and removed automated retry function to start preview on exception.
 */
public class ResizableCameraPreview extends CameraPreview
{
    public ResizableCameraPreview(Activity activity, int cameraId, LayoutMode mode, boolean addReversedSizes)
    {
        super(activity, cameraId, mode);
        if (addReversedSizes)
        {
            List<Camera.Size> sizes = mPreviewSizeList;
            int length = sizes.size();
            for (int i = 0; i < length; i++)
            {
                Camera.Size size = sizes.get(i);
                Camera.Size revSize = mCamera.new Size(size.height, size.width);
                sizes.add(revSize);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        mCamera.stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring)
        {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);

            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring)
            {
                return;
            }
        }

        configureCameraParameters(cameraParams);
        mSurfaceConfiguring = false;

        try
        {
            mCamera.startPreview();
        }
        catch (Exception e)
        {
            Toast.makeText(mActivity, "Failed to start preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @param index  selects preview size from the list returned by CameraPreview.getSupportedPreivewSizes().
     * @param width  is the width of the available area for this view
     * @param height is the height of the available area for this view
     */
    public void setPreviewSize(int index, int width, int height)
    {
        mCamera.stopPreview();

        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);

        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged)
        {
            mSurfaceConfiguring = true;
            return;
        }

        configureCameraParameters(cameraParams);
        try
        {
            mCamera.startPreview();
        }
        catch (Exception e)
        {
            Toast.makeText(mActivity, "Failed to satart preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mSurfaceConfiguring = false;
    }

    public List<Camera.Size> getSupportedPreivewSizes()
    {
        return mPreviewSizeList;
    }
}
