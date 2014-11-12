package com.example.android.camera2video.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.camera2video.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by kgalligan on 11/12/14.
 */
public class RecordTest extends Activity
{
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String TAG = "MediaProjectionDemo";
    private static final int PERMISSION_CODE = 1;
    public static final int BIT_RATE = 10000000;
    public static final int FRAME_RATE = 30;

    private int mScreenDensity;
    private int mDisplayWidth;
    private int mDisplayHeight;

    private MediaProjectionManager mProjectionManager;

    private boolean mScreenSharing;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    MediaRecorder mMediaRecorder;
    boolean mIsRecordingVideo;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_test);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mDisplayWidth = metrics.widthPixels;
        mDisplayHeight = metrics.heightPixels;

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mMediaProjection != null)
        {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != PERMISSION_CODE)
        {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK)
        {
            Toast.makeText(this,
                    "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = createVirtualDisplay();
    }

    public void onToggleScreenShare(View view)
    {
        if (((ToggleButton) view).isChecked())
        {
            shareScreen();
        }
        else
        {
            stopScreenSharing();
        }
    }

    private void shareScreen()
    {
        mScreenSharing = true;

        if (mMediaProjection == null)
        {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            return;
        }

        mVirtualDisplay = createVirtualDisplay();
    }

    private void stopScreenSharing()
    {
        mIsRecordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

        mScreenSharing = false;
        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.release();
    }


    private VirtualDisplay createVirtualDisplay()
    {
        mMediaRecorder = new MediaRecorder();
        final File file = new File(Environment.getExternalStorageDirectory(), "screenvid_" + System.currentTimeMillis() + ".mp4");

        try
        {
            // UI
            mIsRecordingVideo = true;
            // Configure the MediaRecorder
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            mMediaRecorder.setVideoEncodingBitRate(BIT_RATE);
            mMediaRecorder.setVideoFrameRate(FRAME_RATE);
            mMediaRecorder.setVideoSize(1280, 720);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation);
//            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            Surface surface = mMediaRecorder.getSurface();

            VirtualDisplay virtualDisplay = mMediaProjection.createVirtualDisplay("ScreenSharingDemo",
                    mDisplayWidth, mDisplayHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface, null /*Callbacks*/, null /*Handler*/);

            mMediaRecorder.start();
            return virtualDisplay;
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*private void resizeVirtualDisplay()
    {
        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.resize(mDisplayWidth, mDisplayHeight, mScreenDensity);
    }*/
}